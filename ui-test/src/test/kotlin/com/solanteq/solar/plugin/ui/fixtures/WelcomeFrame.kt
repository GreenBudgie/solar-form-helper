package com.solanteq.solar.plugin.ui.fixtures

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.*
import com.intellij.remoterobot.search.locators.byXpath
import java.time.Duration

fun RemoteRobot.welcomeFrame(function: WelcomeFrame.() -> Unit) {
    find(WelcomeFrame::class.java, Duration.ofSeconds(10)).apply(function)
}

@FixtureName("Welcome Frame")
@DefaultXpath("type", "//div[@class='FlatWelcomeFrame']")
class WelcomeFrame(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) :
    CommonContainerFixture(remoteRobot, remoteComponent) {

    val openProjectLink = actionLink(byXpath("Open", "//div[@defaulticon='open.svg']"))

}