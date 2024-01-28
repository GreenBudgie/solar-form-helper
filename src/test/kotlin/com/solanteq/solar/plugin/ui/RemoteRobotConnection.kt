package com.solanteq.solar.plugin.ui

import com.intellij.remoterobot.RemoteRobot

const val REMOTE_ROBOT_PORT = "8082"
const val REMOTE_ROBOT_URL = "http://127.0.0.1:$REMOTE_ROBOT_PORT"

val remoteRobot = RemoteRobot(REMOTE_ROBOT_URL)

