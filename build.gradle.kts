import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    kotlin("jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.1.0"
}

val jUnitVersion = "1.10.0"
val jupiterVersion = "5.11.0"

val remoteRobotVersion = "0.11.23"
val loggingInterceptorVersion = "4.12.0"
val mockkVersion = "1.13.9"

group = "com.solanteq.solar.plugin"
version = "1.0.0-SNAPSHOT"

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.2.2")
        testFramework(TestFrameworkType.Plugin.Java)
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.JUnit5)

        plugins(
            listOf(
                "com.google.ide-perf:1.3.1"
            )
        )

        bundledPlugins(
            listOf(
                "com.intellij.java",
                "org.jetbrains.kotlin",
                "org.intellij.plugins.markdown"
            )
        )

        instrumentationTools()
        pluginVerifier()
        zipSigner()
    }
    testImplementation("junit:junit:4.13.2")

    testImplementation("org.junit.platform:junit-platform-launcher:$jUnitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$jupiterVersion")
    testImplementation("org.junit.vintage:junit-vintage-engine:$jupiterVersion")

    testImplementation("com.intellij.remoterobot:remote-robot:$remoteRobotVersion")
    testImplementation("com.intellij.remoterobot:remote-fixtures:$remoteRobotVersion")
    testImplementation("com.intellij.remoterobot:ide-launcher:$remoteRobotVersion")
    testImplementation("com.squareup.okhttp3:logging-interceptor:$loggingInterceptorVersion")
    testImplementation("io.mockk:mockk:$mockkVersion") {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core-jvm")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-bom")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242"
            untilBuild = "242.*"
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

tasks.register("cleanAndRunIdeForUiTests") {
    finalizedBy("runIdeForUiTests")
    delete(layout.buildDirectory.dir("idea-sandbox/config-uiTest"))
    delete(layout.buildDirectory.dir("idea-sandbox/plugins-uiTest"))
}

val pluginFileName = "${rootProject.name}-$version.jar"

tasks {
    val runIdeForUiTests by intellijPlatformTesting.runIde.registering {
        task {
            jvmArgumentProviders += CommandLineArgumentProvider {
                listOf(
                    "-Drobot-server.port=8082",
                    "-Dide.mac.file.chooser.native=false",
                    "-Didea.trust.all.projects=true",
                    "-Dide.mac.message.dialogs.as.sheets=false",
                    "-Djb.privacy.policy.text=<!--999.999-->",
                    "-Djb.consents.confirmation.enabled=false",
                    "-Dide.show.tips.on.startup.default.value=false",
                    "-DjbScreenMenuBar.enabled=false",
                    "-Dapple.laf.useScreenMenuBar=false"
                )
            }
        }

        plugins {
            robotServerPlugin()
        }
    }

    test {
        if (System.getProperty("test.ui.exclude") == "true") {
            exclude("**/ui/**")
        }

        fun buildDirPath(relativePath: String) = layout.buildDirectory.dir(relativePath).get().asFile.absolutePath

        systemProperty("idea.home.path", "/Users/nbundin/Projects/intellij")

        systemProperty("test.plugin.path", buildDirPath("libs/$pluginFileName"))
        systemProperty("test.idea.path", buildDirPath("ui-test/idea"))
        systemProperty("test.idea.sandbox.path", buildDirPath("ui-test/idea-sandbox"))
        systemProperty("test.project.path", buildDirPath("ui-test/project"))

        systemProperty("remote.robot.version", remoteRobotVersion)
        useJUnitPlatform()
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    buildSearchableOptions {
        enabled = false
    }

}