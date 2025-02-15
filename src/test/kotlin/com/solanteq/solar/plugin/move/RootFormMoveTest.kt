package com.solanteq.solar.plugin.move

import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.base.createForm
import com.solanteq.solar.plugin.l10n.createL10nFile
import com.solanteq.solar.plugin.l10n.generateL10nFileText
import org.jetbrains.kotlin.idea.core.util.toPsiDirectory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class RootFormMoveTest : LightPluginTestBase() {

    @Test
    fun `test rename top level module property on move`() = with(fixture) {
        val textBefore = """
            {"module": "test"}
        """.trimIndent()
        val textAfter = """
            {"module": "test2"}
        """.trimIndent()

        val form = createForm("test", "test", textBefore)
        MoveTestUtils.moveRootForm(fixture, form, "test2")

        Assertions.assertEquals(textAfter, form.text)
    }

    @Test
    fun `test update references to module from other forms on root form move`() = with(fixture) {
        val textBefore = """
            {"form": "test.testForm"}
        """.trimIndent()
        val textAfter = """
            {"form": "test2.testForm"}
        """.trimIndent()

        val form = createForm("references", "test", textBefore)
        val formToMove = createForm("testForm", "test", "{}")
        MoveTestUtils.moveRootForm(fixture, formToMove, "test2")

        Assertions.assertEquals(textAfter, form.text)
    }

    @Test
    fun `test update references to module from l10n files on root form move`() = with(fixture) {
        val textAfter = generateL10nFileText(
            "test2.form.testForm.randomText" to "Test l10n"
        )

        val l10nFile = createL10nFile("l10n",
            "test.form.testForm.randomText" to "Test l10n"
        )
        val formToMove = createForm("testForm", "test", "{}")
        MoveTestUtils.moveRootForm(fixture, formToMove, "test2")

        Assertions.assertEquals(textAfter, l10nFile.text)
    }

    @Test
    fun `test move module to incorrect location does not throw and no changes are made`() = with(fixture) {
        val text = """
            {
              "form": "test.form1",
              "parentForm": "test.form2",
              "parametersForm": "test.form3",
            }
        """.trimIndent()

        val form = createForm("testForm", "test3", text)
        createForm("form1", "test", "{}")
        createForm("form2", "test", "{}")
        createForm("form3", "test", "{}")

        val directory = findFileInTempDir("main/resources/config/forms/test")
            .toPsiDirectory(project)!!

        assertDoesNotThrow {
            MoveTestUtils.moveDirectory(fixture, directory, "main/resources/config/forms/test2")
        }

        Assertions.assertEquals(text, form.text)
    }

}