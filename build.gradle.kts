plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.17.0"
}

val jUnitVersion = "1.10.0"
val jupiterVersion = "5.10.0"

val remoteRobotVersion = "0.11.21"
val loggingInterceptorVersion = "4.12.0"
val mockkVersion = "1.13.9"

group = "com.solanteq.solar.plugin"
version = "1.0.0-SNAPSHOT"

intellij {
    version.set("2023.3.2")
    type.set("IC")

    plugins.set(
        listOf(
            "com.intellij.java",
            "org.jetbrains.kotlin",
            "org.intellij.plugins.markdown",
            "com.google.ide-perf:1.3.1"
        )
    )
}

repositories {
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
}

dependencies {
    testImplementation("org.junit.platform:junit-platform-launcher:$jUnitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$jupiterVersion")

    testImplementation("com.intellij.remoterobot:remote-robot:$remoteRobotVersion")
    testImplementation("com.intellij.remoterobot:remote-fixtures:$remoteRobotVersion")
    testImplementation("com.intellij.remoterobot:ide-launcher:$remoteRobotVersion")
    testImplementation("com.squareup.okhttp3:logging-interceptor:$loggingInterceptorVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

tasks.register("cleanAndRunIdeForUiTests") {
    finalizedBy("runIdeForUiTests")
    delete(layout.buildDirectory.dir("idea-sandbox/config-uiTest"))
    delete(layout.buildDirectory.dir("idea-sandbox/plugins-uiTest"))
}

val pluginFileName = "${rootProject.name}-$version.jar"

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    downloadRobotServerPlugin {
        version.set(remoteRobotVersion)
    }

    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.file.chooser.native", false)
        systemProperty("idea.trust.all.projects", true)
        systemProperty("ide.show.tips.on.startup.default.value", false)
        systemProperty("jbScreenMenuBar.enabled", false)
        systemProperty("apple.laf.useScreenMenuBar", false)
    }

    test {
        if (System.getProperty("test.ui.exclude") == "true") {
            exclude("**/ui/**")
        }
        systemProperty("idea.home.path", "/Users/nbundin/Projects/intellij")
        systemProperty("test.plugin.path", buildDir.resolve("libs/$pluginFileName").absolutePath)
        systemProperty("test.idea.path", buildDir.resolve("ui-test/idea"))
        systemProperty("test.idea.sandbox.path", buildDir.resolve("ui-test/idea-sandbox"))
        systemProperty("test.project.path", buildDir.resolve("ui-test/project"))
        systemProperty("remote.robot.version", remoteRobotVersion)
        useJUnitPlatform()
    }

    patchPluginXml {
        sinceBuild.set("233") //2023.3.*
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