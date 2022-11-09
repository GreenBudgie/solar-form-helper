package com.solanteq.solar.plugin

import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture
import com.solanteq.solar.plugin.file.FormFileType

fun testDataPathWithSuffix(suffix: String) = "src/test/testData/$suffix/"

/**
 * Copies forms form testData directory to the proper location and opens the first form in editor
 */
fun JavaCodeInsightTestFixture.configureByForms(vararg formPaths: String): PsiFile? {
    val virtualFiles = formPaths.map {
        copyFileToProject(it, "main/resources/config/forms/$it")
    }
    virtualFiles.firstOrNull()?.let { configureFromExistingVirtualFile(it) }
    return file
}

fun JavaCodeInsightTestFixture.configureByFormText(formText: String): PsiFile = this.configureByText(FormFileType, formText)