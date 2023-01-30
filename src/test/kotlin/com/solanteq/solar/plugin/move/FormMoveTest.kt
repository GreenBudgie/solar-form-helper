package com.solanteq.solar.plugin.move

import com.intellij.json.psi.JsonFile
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFilesOrDirectoriesProcessor
import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.base.createDirectory
import com.solanteq.solar.plugin.base.createForm
import com.solanteq.solar.plugin.base.createIncludedForm
import com.solanteq.solar.plugin.l10n.L10nTestUtils
import com.solanteq.solar.plugin.util.asArray
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FormMoveTest : LightPluginTestBase() {

    @Test
    fun `test rename top level module property on move`() {
        val textBefore = """
            {"module": "test"}
        """.trimIndent()
        val textAfter = """
            {"module": "test2"}
        """.trimIndent()

        val form = fixture.createForm("test", textBefore, "test").move("test2")

        Assertions.assertEquals(textAfter, form.text)
    }

    @Test
    fun `test update references to module from other forms on root form move`() {
        val textBefore = """
            {"form": "test.testForm"}
        """.trimIndent()
        val textAfter = """
            {"form": "test2.testForm"}
        """.trimIndent()

        val form = fixture.createForm("references", textBefore, "test")
        fixture.createForm("testForm", "{}", "test").move("test2")

        Assertions.assertEquals(textAfter, form.text)
    }

    @Test
    fun `test update references to module from l10n files on root form move`() {
        val textAfter = L10nTestUtils.generateL10nFileText(
            "test2.form.testForm.randomText" to "Test l10n"
        )

        val l10nFile = L10nTestUtils.createL10nFile(fixture, "l10n",
            "test.form.testForm.randomText" to "Test l10n"
        )
        fixture.createForm("testForm", "{}", "test").move("test2")

        Assertions.assertEquals(textAfter, l10nFile.text)
    }

    @Test
    fun `test update references to directory on included form move`() {
        val textBefore = """
            {"groups": "json://includes/forms/dir1/includedForm.json"}
        """.trimIndent()
        val textAfter = """
            {"groups": "json://includes/forms/dir1_moved/includedForm.json"}
        """.trimIndent()

        val form = fixture.createForm("references", textBefore, "test")
        fixture.createIncludedForm("includedForm", "dir1", "{}")
            .moveIncluded("dir1_moved")

        Assertions.assertEquals(textAfter, form.text)
    }

    @Test
    fun `test update references to multiple directories on included form move`() {
        val textBefore = """
            {"groups": "json://includes/forms/dir1/dir2/dir3/includedForm.json"}
        """.trimIndent()
        val textAfter = """
            {"groups": "json://includes/forms/dir1/dir2_moved/dir3_moved/includedForm.json"}
        """.trimIndent()

        val form = fixture.createForm("references", textBefore, "test")
        fixture.createIncludedForm("includedForm", "dir1/dir2/dir3", "{}")
            .moveIncluded("dir1/dir2_moved/dir3_moved")

        Assertions.assertEquals(textAfter, form.text)
    }

    @Test
    fun `test update references to directories on included form move with different nesting level`() {
        val textBefore = """
            {"groups": "json://includes/forms/dir1/includedForm.json"}
        """.trimIndent()
        val textAfter = """
            {"groups": "json://includes/forms/dir1/dir2/dir3/includedForm.json"}
        """.trimIndent()

        val form = fixture.createForm("references", textBefore, "test")
        fixture.createIncludedForm("includedForm", "dir1", "{}")
            .moveIncluded("dir1/dir2/dir3")

        Assertions.assertEquals(textAfter, form.text)
    }

    private fun JsonFile.move(module: String) =
        processMove(this, "src/main/resources/config/forms/$module")

    private fun JsonFile.moveIncluded(path: String) =
        processMove(this, "src/main/resources/config/includes/forms/$path")

    private fun processMove(file: JsonFile, path: String): JsonFile {
        val targetDirectory = fixture.createDirectory(path)
        MoveFilesOrDirectoriesProcessor(
            file.project,
            file.asArray(),
            targetDirectory,
            false,
            true,
            null,
            null
        ).run()
        return file
    }

}