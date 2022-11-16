package com.solanteq.solar.plugin

import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture
import com.solanteq.solar.plugin.file.FormFileType

/**
 * Creates new form in the correct directory with specified text
 *
 * @param formName Form file name (without .json extension)
 * @param text Text to be placed in a form
 * @param group Form group. The file will be placed inside this directory
 * @param isIncluded Whether this form must be placed in `/includes` directory
 *
 * @return Created form virtual file
 */
fun JavaCodeInsightTestFixture.createForm(
    formName: String,
    text: String,
    group: String? = null,
    isIncluded: Boolean = false
): PsiFile {
    val realFileName = "$formName.json"
    val groupPath = if(group == null) "" else "$group/"
    val includedPath = if(isIncluded) "includes/" else ""

    return addFileToProject(
        "main/resources/config/${includedPath}forms/$groupPath$realFileName",
        text
    )
}

/**
 * Creates new form in the correct directory with specified text and opens it in the editor
 *
 * @see createForm
 * @return Opened form psi file
 */
fun JavaCodeInsightTestFixture.createFormAndConfigure(
    formName: String,
    text: String,
    group: String? = null,
    isIncluded: Boolean = false
): PsiFile {
    val virtualFormFile = createForm(formName, text, group, isIncluded)
    configureFromExistingVirtualFile(virtualFormFile.virtualFile)
    return file
}

/**
 * Copies forms from testData directory to the correct directory and opens the first form in editor
 */
fun JavaCodeInsightTestFixture.configureByForms(vararg formPaths: String, group: String? = null): PsiFile? {
    val groupPath = if(group == null) "" else "$group/"
    val virtualFiles = formPaths.map {
        copyFileToProject(it, "main/resources/config/forms/$groupPath$it")
    }
    virtualFiles.firstOrNull()?.let { configureFromExistingVirtualFile(it) }
    return file
}

/**
 * Creates a new form file and opens it in the editor with the specified text.
 *
 * Caution: this method only imitates form file behavior,
 * but actually it is stored in aaa.json file in root directory.
 * For example, references to this form will not work.
 * For many purposes it is better to use [createFormAndConfigure] because it
 * will be placed into correct directory.
 */
fun JavaCodeInsightTestFixture.configureByFormText(text: String): PsiFile =
    this.configureByText(FormFileType, text)