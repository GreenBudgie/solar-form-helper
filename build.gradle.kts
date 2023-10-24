plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.15.0"
}

group = "com.solanteq.solar.plugin"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://karjala.solanteq.com/content/repositories/releases/")
}

dependencies {
    testImplementation("org.junit.platform:junit-platform-launcher:1.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("2023.2.3")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf(
        "com.intellij.java",
        "org.jetbrains.kotlin",
        "org.intellij.plugins.markdown",
        "com.google.ide-perf:1.3.1",
        "com.jetbrains.hackathon.indices.viewer:1.24"
    ))
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

    patchPluginXml {
        sinceBuild.set("232") //2023.2.*
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
