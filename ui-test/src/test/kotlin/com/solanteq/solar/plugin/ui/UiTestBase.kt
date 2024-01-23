package com.solanteq.solar.plugin.ui

import com.intellij.remoterobot.RemoteRobot
import com.solanteq.solar.plugin.ui.fixtures.dialog
import com.solanteq.solar.plugin.ui.fixtures.welcomeFrame
import org.junit.jupiter.api.Test

class UiTestBase {

    protected val remoteRobot = RemoteRobot(REMOTE_ROBOT_URL)

    @Test
    fun test() {

    }

    companion object {

        const val REMOTE_ROBOT_URL = "http://127.0.0.1:8082"

    }

}