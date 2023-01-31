package com.solanteq.solar.plugin.move

import com.intellij.json.psi.JsonFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFilesOrDirectoriesProcessor
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.solanteq.solar.plugin.base.createDirectory
import com.solanteq.solar.plugin.util.asArray

object MoveTestUtils {

    /**
     * Moves the root [form] to the specified [module]
     */
    fun moveRootForm(fixture: CodeInsightTestFixture, form: JsonFile, module: String) {
        processMove(fixture, form, "src/main/resources/config/forms/$module")
    }

    /**
     * Moves the included [form] to the directory by the specified [relativePath].
     * The path is relative to `includes/forms`.
     */
    fun moveIncludedForm(fixture: CodeInsightTestFixture, form: JsonFile, relativePath: String) {
        processMove(fixture, form, "src/main/resources/config/includes/forms/$relativePath")
    }

    /**
     * Moves [directory] to the specified [fullPath] location. This path is relative to `src` root.
     */
    fun moveDirectory(fixture: CodeInsightTestFixture, directory: PsiDirectory, fullPath: String) {
        processMove(fixture, directory, fullPath)
    }

    private fun processMove(fixture: CodeInsightTestFixture, file: PsiElement, path: String): PsiElement {
        val targetDirectory = fixture.createDirectory(path)
        MoveFilesOrDirectoriesProcessor(
            fixture.project,
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