package com.solanteq.solar.plugin.move

import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.base.createForm
import com.solanteq.solar.plugin.base.createIncludedForm
import org.jetbrains.kotlin.idea.core.util.toPsiDirectory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class IncludedFormMoveTest : LightPluginTestBase() {

    @Test
    fun `test update references to directory on included form move`() = with(fixture) {
        val textBefore = """
            {"groups": "json://includes/forms/dir1/includedForm.json"}
        """.trimIndent()
        val textAfter = """
            {"groups": "json://includes/forms/dir1_moved/includedForm.json"}
        """.trimIndent()

        val form = createForm("references", "test", textBefore)
        val formToMove = createIncludedForm("includedForm", "dir1", "{}")
        MoveTestUtils.moveIncludedForm(fixture, formToMove, "dir1_moved")

        Assertions.assertEquals(textAfter, form.text)
    }

    @Test
    fun `test update references to multiple directories on included form move`() = with(fixture) {
        val textBefore = """
            {"groups": "json://includes/forms/dir1/dir2/dir3/includedForm.json"}
        """.trimIndent()
        val textAfter = """
            {"groups": "json://includes/forms/dir1/dir2_moved/dir3_moved/includedForm.json"}
        """.trimIndent()

        val form = createForm("references", "test", textBefore)
        val formToMove = createIncludedForm("includedForm", "dir1/dir2/dir3", "{}")
        MoveTestUtils.moveIncludedForm(fixture, formToMove, "dir1/dir2_moved/dir3_moved")

        Assertions.assertEquals(textAfter, form.text)
    }

    @Test
    fun `test update references to directories on included form move with different nesting level`() = with(fixture) {
        val textBefore = """
            {"groups": "json://includes/forms/dir1/includedForm.json"}
        """.trimIndent()
        val textAfter = """
            {"groups": "json://includes/forms/dir1/dir2/dir3/includedForm.json"}
        """.trimIndent()

        val form = createForm("references", "test", textBefore)
        val formToMove = createIncludedForm("includedForm", "dir1", "{}")
        MoveTestUtils.moveIncludedForm(fixture, formToMove, "dir1/dir2/dir3")

        Assertions.assertEquals(textAfter, form.text)
    }

    @Test
    fun `test move directory with inner included forms`() = with(fixture) {
        val textBefore = """
            {
              "groupRows": "json://includes/forms/dir1/dir2/includedForm1.json",
              "groups": "json://includes/forms/dir1/dir2/includedForm2.json",
              "rows": "json://includes/forms/dir1/dir2/includedForm3.json"
            }
        """.trimIndent()
        val textAfter = """
            {
              "groupRows": "json://includes/forms/move1/move2/dir2/includedForm1.json",
              "groups": "json://includes/forms/move1/move2/dir2/includedForm2.json",
              "rows": "json://includes/forms/move1/move2/dir2/includedForm3.json"
            }
        """.trimIndent()

        val form = createForm("references", "test", textBefore)
        createIncludedForm("includedForm1", "dir1/dir2", "{}")
        createIncludedForm("includedForm2", "dir1/dir2", "{}")
        createIncludedForm("includedForm3", "dir1/dir2", "{}")

        val directory = findFileInTempDir("main/resources/config/includes/forms/dir1/dir2")
            .toPsiDirectory(project)!!

        MoveTestUtils.moveDirectory(fixture, directory, "main/resources/config/includes/forms/move1/move2")

       Assertions.assertEquals(textAfter, form.text)
    }

    @Test
    fun `test move directory to incorrect location does not throw and no changes are made`() = with(fixture) {
        val text = """
            {
              "groupRows": "json://includes/forms/dir1/dir2/includedForm1.json",
              "groups": "json://includes/forms/dir1/dir2/includedForm2.json",
              "rows": "json://includes/forms/dir1/dir2/includedForm3.json"
            }
        """.trimIndent()

        val form = createForm("references", "test", text)
        createIncludedForm("includedForm1", "dir1/dir2", "{}")
        createIncludedForm("includedForm2", "dir1/dir2", "{}")
        createIncludedForm("includedForm3", "dir1/dir2", "{}")

        val directory = findFileInTempDir("main/resources/config/includes/forms/dir1/dir2")
            .toPsiDirectory(project)!!

        assertDoesNotThrow {
            MoveTestUtils.moveDirectory(fixture, directory, "main/resources/config/incorrect/forms")
        }

        Assertions.assertEquals(text, form.text)
    }

}