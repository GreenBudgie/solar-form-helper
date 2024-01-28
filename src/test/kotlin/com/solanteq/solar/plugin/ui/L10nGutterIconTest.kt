package com.solanteq.solar.plugin.ui

import com.intellij.remoterobot.fixtures.GutterIcon
import com.intellij.remoterobot.fixtures.JListFixture
import com.intellij.remoterobot.fixtures.TextEditorFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.repeatInTime
import com.solanteq.solar.plugin.ui.fixtures.idea
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Duration.ofMinutes
import java.time.Duration.ofSeconds

class L10nGutterIconTest : UITestBase() {

    @Test
    fun `test gutter icons for l10ns`() {
        remoteRobot.idea {
            openFile("src/main/resources/config/forms/gutterIcon/gutterIconTestForm.json")
        }
        val textEditor = remoteRobot.find<TextEditorFixture>(TextEditorFixture.locator, ofSeconds(10))
        var gutterIcons: List<GutterIcon> = emptyList()
        repeatInTime(ofSeconds(30)) {
            gutterIcons = textEditor.gutter.getIcons()
            gutterIcons.isNotEmpty()
        }

        assertEquals(3, gutterIcons.size, "Incorrect number of gutter icons is visible")

        gutterIcons = gutterIcons.sortedBy { it.lineNumber }
        val formGutterIcon = gutterIcons[0]
        val groupGutterIcon = gutterIcons[1]
        val fieldGutterIcon = gutterIcons[2]

        formGutterIcon.click()
        val formL10nsList = remoteRobot.find<JListFixture>(byXpath("//div[@class='JBList']"))
        val formL10nsItems: List<String> = formL10nsList.collectItems()
        assertTrue(
            formL10nsItems containsElementStartingWith "Форма",
            "Ru l10n for form is not visible"
        )
        assertTrue(
            formL10nsItems containsElementStartingWith "Form",
            "En l10n for form is not visible"
        )

        remoteRobot.keyboard { escape() }

        groupGutterIcon.click()
        val groupL10nsList = remoteRobot.find<JListFixture>(byXpath("//div[@class='JBList']"))
        val groupL10nsItems = groupL10nsList.collectItems()
        assertTrue(
            groupL10nsItems containsElementStartingWith "Группа",
            "Ru l10n for group is not visible"
        )
        assertTrue(
            groupL10nsItems containsElementStartingWith "Group",
            "En l10n for group is not visible"
        )

        remoteRobot.keyboard { escape() }

        fieldGutterIcon.click()

        val openedL10nFile = remoteRobot.find<TextEditorFixture>(TextEditorFixture.locator, ofSeconds(10))
        assertTrue(
            openedL10nFile.editor.filePath.endsWith("ru-RU/form.json"),
            "L10n file was not opened"
        )
    }

    private infix fun List<String>.containsElementStartingWith(prefix: String): Boolean {
        return find { it.startsWith(prefix) } != null
    }

}