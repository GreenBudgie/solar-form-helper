import java.net.URI

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.17.0"
}

group = "com.solanteq.solar.plugin"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = URI("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies") }
}

val jUnitVersion = "1.10.0"
val jupiterVersion = "5.10.0"
val remoteRobotVersion = "0.11.21"

dependencies {
    testImplementation("org.junit.platform:junit-platform-launcher:$jUnitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$jupiterVersion")

    testImplementation("com.intellij.remoterobot:remote-robot:$remoteRobotVersion")
    testImplementation("com.intellij.remoterobot:remote-fixtures:$remoteRobotVersion")

    testImplementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}

intellij {
    version.set("2023.3.2")
    type.set("IC")

    plugins.set(listOf(
        "com.intellij.java",
        "org.jetbrains.kotlin",
        "org.intellij.plugins.markdown",
        "com.google.ide-perf:1.3.1"
    ))
}

tasks.register("cleanAndRunIdeForUiTests") {
    delete(layout.buildDirectory.dir("idea-sandbox/config-uiTest"))
    delete(layout.buildDirectory.dir("idea-sandbox/plugins-uiTest"))
    finalizedBy("runIdeForUiTests")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    test {
        systemProperty("idea.home.path", "/Users/nbundin/Projects/intellij")
        useJUnitPlatform()
    }

    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
    }

    downloadRobotServerPlugin {
        version.set(remoteRobotVersion)
    }

    patchPluginXml {
        sinceBuild.set("233") //2023.2.*
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
