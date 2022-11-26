package com.solanteq.solar.plugin

import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture
import com.solanteq.solar.plugin.file.FormFileType

/**
 * Creates new form in the correct directory with specified text
 *
 * @param formName Form file name (without .json extension)
 * @param text Text to be placed in a form
 * @param module Form module. The file will be placed inside this directory
 *
 * @return Created form psi file
 */
fun JavaCodeInsightTestFixture.createForm(
    formName: String,
    text: String,
    module: String? = null
): PsiFile {
    val realFileName = "$formName.json"
    val modulePath = if(module == null) "" else "$module/"

    return addFileToProject(
        "main/resources/config/forms/$modulePath$realFileName",
        text
    )
}

/**
 * Creates new included form in the correct directory with specified text
 *
 * @param formName Form file name (without .json extension)
 * @param relativePath Form path relative to "config/includes/forms/". Separate with /.
 * @param text Text to be placed in a form
 * You may not insert a separator at the start and at the end.
 *
 * @return Created form psi file
 */
fun JavaCodeInsightTestFixture.createIncludedForm(
    formName: String,
    relativePath: String,
    text: String
): PsiFile {
    val realFileName = "$formName.json"

    return addFileToProject(
        "main/resources/config/includes/forms/$relativePath/$realFileName",
        text
    )
}

/**
 * Creates new included form in the correct directory with specified text and opens it in the editor
 *
 * @see createIncludedForm
 * @return Opened form psi file
 */
fun JavaCodeInsightTestFixture.createIncludedFormAndConfigure(
    formName: String,
    relativePath: String,
    text: String
): PsiFile {
    val psiFormFile = createIncludedForm(formName, text, relativePath)
    configureFromExistingVirtualFile(psiFormFile.virtualFile)
    return file
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
    module: String? = null
): PsiFile {
    val psiFormFile = createForm(formName, text, module)
    configureFromExistingVirtualFile(psiFormFile.virtualFile)
    return file
}

/**
 * Copies forms from testData directory to the correct directory and opens the first form in editor
 */
fun JavaCodeInsightTestFixture.configureByForms(vararg formPaths: String, module: String? = null): PsiFile? {
    val modulePath = if(module == null) "" else "$module/"
    val virtualFiles = formPaths.map {
        copyFileToProject(it, "main/resources/config/forms/$modulePath$it")
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