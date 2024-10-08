package com.solanteq.solar.plugin.ui.base

import com.google.gson.JsonParser
import com.intellij.openapi.diagnostic.logger
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.launcher.Ide
import com.intellij.remoterobot.launcher.IdeDownloader
import com.intellij.remoterobot.launcher.IdeLauncher.launchIde
import com.intellij.remoterobot.launcher.Os
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.waitFor
import com.intellij.remoterobot.utils.waitForIgnoringError
import com.intellij.util.io.createDirectories
import com.solanteq.solar.plugin.ui.fixtures.idea
import com.solanteq.solar.plugin.ui.fixtures.welcomeFrame
import io.mockk.every
import io.mockk.spyk
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration.ofMinutes
import java.time.Duration.ofSeconds


object UITestManager {

    private const val TEST_PROJECT_PATH = "src/test/resources/testProject"
    private const val IDEA_APP_DIRECTORY_NAME = "Intellij IDEA CE.app"

    private val LOG = logger<UITestManager>()

    private val httpClient = OkHttpClient()

    private var uiTestsStarted = false
    private var isIdeLaunchedFromGradleTask = false

    private val pluginJarPath = Paths.get(System.getProperty("test.plugin.path"))
    private val ideaAppPath = Paths.get(System.getProperty("test.idea.path"))
    private val ideaSandboxPath = Paths.get(System.getProperty("test.idea.sandbox.path"))
    private val tempProject = File(System.getProperty("test.project.path"))
    private val remoteRobotVersion = System.getProperty("remote.robot.version")
    private val ideaAppFile = ideaAppPath.resolve(IDEA_APP_DIRECTORY_NAME).toFile()
    private val robotServicePluginName = "robot-server-plugin-$remoteRobotVersion"
    private val robotServerPluginPath = ideaAppPath.resolve(robotServicePluginName)

    private val testProjectDirectory = File(TEST_PROJECT_PATH)

    private var ideaProcess: Process? = null

    fun initializeIfNeeded() {
        if (uiTestsStarted) {
            return
        }
        uiTestsStarted = true
        LOG.info("Initializing UI tests")
        if (!isIntellijRunning()) {
            launchIntellij()
        } else {
            isIdeLaunchedFromGradleTask = true
        }
        if (!isTestProjectCreated()) {
            createTestProject()
        }
        if (!isTestProjectOpened()) {
            openTestProject()
        }
        Runtime.getRuntime().addShutdownHook(
            Thread { tearDown() }
        )
    }

    private fun isIntellijRunning(): Boolean {
        try {
            remoteRobot.runJs("true")
            return true
        } catch (e: Exception){
            return false
        }
    }

    private fun isTestProjectCreated(): Boolean {
        return tempProject.exists()
    }

    private fun isTestProjectOpened(): Boolean {
        try {
            remoteRobot.find<ContainerFixture>(byXpath("//div[@class='ProjectViewTree']"))
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun isIdeaInstalled(): Boolean {
        return ideaAppFile.exists()
    }

    private fun launchIntellij() {
        if (!isIdeaInstalled()) {
            downloadIntellij()
        }
        removeIdeaSandbox()

        ideaSandboxPath.createDirectories()
        ideaProcess = launchIde(
            ideaAppFile.toPath(),
            mapOf(
                "robot-server.port" to REMOTE_ROBOT_PORT,
                "ide.mac.file.chooser.native" to false,
                "idea.trust.all.projects" to true,
                "ide.show.tips.on.startup.default.value" to false,
                "jbScreenMenuBar.enabled" to false,
                "apple.laf.useScreenMenuBar" to false,
            ),
            emptyList(),
            listOf(
                robotServerPluginPath,
                pluginJarPath
            ),
            ideaSandboxPath
        )
        waitForIgnoringError(ofMinutes(1)) {
            remoteRobot.callJs("true")
        }
    }

    private fun downloadIntellij() {
        val ideDownloaderSpy = spyk(IdeDownloader(httpClient), recordPrivateCalls = true)

        every {
            ideDownloaderSpy invoke "getIdeDownloadUrl" withArguments listOf(
                any<Ide>(),
                any<Ide.BuildType>(),
                any<String>(),
                any<String>()
            )
        } answers {
            val ide = arg<Ide>(0)
            val buildType = arg<Ide.BuildType>(1)
            val version = arg<String?>(2)
            val buildNumber = arg<String?>(3)
            getCorrectedIdeDownloadUrl(ide, buildType, version, buildNumber)
        }

        LOG.info("Downloading IDE")
        Files.createDirectories(ideaAppPath)

        ideDownloaderSpy.downloadAndExtract(
            Ide.IDEA_COMMUNITY,
            ideaAppPath,
            Ide.BuildType.RELEASE,
            version = "2024.2.2" //TODO set from system property
        )
        ideDownloaderSpy.downloadRobotPlugin(ideaAppPath, remoteRobotVersion)
    }

    private fun tearDown() {
        if (isIdeLaunchedFromGradleTask) {
            return
        }
        stopIntellijProcess()
        removeTempProject()
    }

    private fun removeIdeaSandbox() {
        ideaSandboxPath.toFile().deleteRecursively()
    }

    private fun createTestProject() {
        val successful = testProjectDirectory.copyRecursively(tempProject)
        if (!successful) {
            throw RuntimeException("Temp project cannot be created")
        }
    }

    private fun openTestProject() {
        remoteRobot.welcomeFrame {
            openProjectLink.click()
            // It lags before inserting the text for some reason, need to wait
            Thread.sleep(1000)
            val projectDirectoryInput = textField(byXpath("//div[@class='BorderlessTextField']"))
            projectDirectoryInput.text = tempProject.absolutePath
            button("OK").click()
        }
        remoteRobot.idea {
            waitForSmartMode()
        }
        // Delay to allow idea to start indexing
        Thread.sleep(7000)
        remoteRobot.idea {
            waitForSmartMode()
        }
    }

    private fun stopIntellijProcess() {
        // If it returns, it means that intellij was launched using gradle task
        ideaProcess?.destroy() ?: return
        waitFor(ofSeconds(20)) {
            !isIntellijRunning()
        }
        Thread.sleep(500)
        removeIdeaSandbox()
    }

    private fun removeTempProject() {
        tempProject.deleteRecursively()
    }

    private fun getCorrectedFeedsOsPropertyName(): String {
        if (Os.hostOS() == Os.WINDOWS) {
            return "windowsZip"
        }
        if (Os.hostOS() == Os.LINUX) {
            return "linux"
        }
        val processBuilder = ProcessBuilder("sysctl", "-n", "machdep.cpu.brand_string")
        val process = processBuilder.start()
        val reader = process.inputReader()
        val output = reader.readLine()
        val status = process.waitFor()
        if (status != 0) {
            return "mac"
        }
        if (output == "Apple M1") {
            return "macM1"
        }
        return "mac"
    }

    /**
     * A very aggressive hack to be able to download IDEA for Apple M1
     */
    private fun getCorrectedIdeDownloadUrl(
        ide: Ide,
        buildType: Ide.BuildType,
        version: String?,
        buildNumber: String?,
    ): String {
        LOG.info("Correcting IDE download URL to use Apple M1 if needed")
        val osPropertyName = getCorrectedFeedsOsPropertyName()
        LOG.info("Selected OS property: $osPropertyName")
        return httpClient.newCall(
            Request.Builder().url(
                "https://data.services.jetbrains.com/products/releases".toHttpUrl()
                    .newBuilder()
                    .addQueryParameter("code", ide.feedsCode)
                    .addQueryParameter("type", buildType.title)
                    .addQueryParameter("platform", osPropertyName)
                    .build()
            ).build()
        ).execute().use { response ->
            check(response.isSuccessful) { "failed to get $ide feeds" }
            JsonParser.parseReader(response.body!!.charStream())
                .asJsonObject[ide.feedsCode]
                .asJsonArray
                .firstOrNull {
                    val entry = it.asJsonObject
                    (entry["downloads"]?.asJsonObject?.keySet()?.isNotEmpty() ?: false)
                        && (version == null || entry["version"]?.asString == version)
                        && (buildNumber == null || entry["build"]?.asString == buildNumber)
                }
                ?.asJsonObject?.get("downloads")
                ?.asJsonObject?.get(osPropertyName)
                ?.asJsonObject?.get("link")
                ?.asString ?: error("no suitable ide found")
        }
    }

}