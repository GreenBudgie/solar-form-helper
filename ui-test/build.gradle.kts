repositories {
    maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
}

val remoteRobotVersion = "0.11.21"
val loggingInterceptorVersion = "4.12.0"
val mockkVersion = "1.13.9"

dependencies {
    testImplementation("com.intellij.remoterobot:remote-robot:$remoteRobotVersion")
    testImplementation("com.intellij.remoterobot:remote-fixtures:$remoteRobotVersion")
    testImplementation("com.intellij.remoterobot:ide-launcher:$remoteRobotVersion")

    testImplementation("com.squareup.okhttp3:logging-interceptor:$loggingInterceptorVersion")

    testImplementation("io.mockk:mockk:$mockkVersion")
}

tasks.register("cleanAndRunIdeForUiTests") {
    delete(layout.buildDirectory.dir("idea-sandbox/config-uiTest"))
    delete(layout.buildDirectory.dir("idea-sandbox/plugins-uiTest"))
    finalizedBy("runIdeForUiTests")
}

val pluginFileName = "${rootProject.name}-$version.jar"

tasks {

    downloadRobotServerPlugin {
        version.set(remoteRobotVersion)
    }

    test {
        systemProperty(
            "test.plugin.path",
            rootProject.buildDir.resolve("libs/$pluginFileName").absolutePath
        )
        systemProperty("test.idea.path", buildDir.resolve("ui-test/idea"))
        systemProperty("test.idea.sandbox.path", buildDir.resolve("ui-test/idea-sandbox"))
        systemProperty("test.project.path", buildDir.resolve("ui-test/project"))
        systemProperty("remote.robot.version", remoteRobotVersion)
    }

}
