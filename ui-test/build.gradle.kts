repositories {
    maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
}

val remoteRobotVersion = "0.11.21"
val loggingInterceptorVersion = "4.12.0"

dependencies {
    testImplementation("com.intellij.remoterobot:remote-robot:$remoteRobotVersion")
    testImplementation("com.intellij.remoterobot:remote-fixtures:$remoteRobotVersion")

    testImplementation("com.squareup.okhttp3:logging-interceptor:$loggingInterceptorVersion")
}

tasks.register("cleanAndRunIdeForUiTests") {
    delete(layout.buildDirectory.dir("idea-sandbox/config-uiTest"))
    delete(layout.buildDirectory.dir("idea-sandbox/plugins-uiTest"))
    finalizedBy("runIdeForUiTests")
}

tasks {

    downloadRobotServerPlugin {
        version.set(remoteRobotVersion)
    }

    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
    }

}
