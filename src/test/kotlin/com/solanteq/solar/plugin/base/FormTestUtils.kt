package com.solanteq.solar.plugin.base

import com.intellij.json.psi.JsonFile
import com.intellij.psi.PsiDirectory
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.solanteq.solar.plugin.file.RootFormFileType

/**
 * Creates a new directory by placing `.placeholder` file in it
 *
 * @param path The directory path relative to `src`
 *
 * @return Created directory psi
 */
fun CodeInsightTestFixture.createDirectory(
    path: String
): PsiDirectory {
    val file = addFileToProject(
        "$path/.placeholder",
        ""
    )
    return file.parent!!
}

/**
 * Creates new form in the correct directory with specified text
 *
 * @param formName Form file name (without .json extension)
 * @param text Text to be placed in a form
 * @param module Form module. The file will be placed inside this directory
 *
 * @return Created form psi file
 */
fun CodeInsightTestFixture.createForm(
    formName: String,
    text: String,
    module: String? = null
): JsonFile {
    val realFileName = "$formName.json"
    val modulePath = if(module == null) "" else "$module/"

    return addFileToProject(
        "main/resources/config/forms/$modulePath$realFileName",
        text
    ) as JsonFile
}

/**
 * Creates new included form in the correct directory with specified text
 *
 * @param formName Form file name (without .json extension)
 * @param relativePath Form path relative to "config/includes/forms/". Separate with /.
 * Can be treated as module, but included forms do not really have modules.
 * @param text Text to be placed in a form
 * You may not insert a separator at the start and at the end.
 *
 * @return Created form psi file
 */
fun CodeInsightTestFixture.createIncludedForm(
    formName: String,
    relativePath: String,
    text: String
): JsonFile {
    val realFileName = "$formName.json"
    val realRelativePath = if(relativePath.isBlank()) "" else "$relativePath/"

    return addFileToProject(
        "main/resources/config/includes/forms/$realRelativePath$realFileName",
        text
    ) as JsonFile
}

/**
 * Creates new included form in the correct directory with specified text and opens it in the editor
 *
 * @see createIncludedForm
 * @return Opened form psi file
 */
fun CodeInsightTestFixture.createIncludedFormAndConfigure(
    formName: String,
    relativePath: String,
    text: String
): JsonFile {
    val psiFormFile = createIncludedForm(formName, relativePath, text)
    configureFromExistingVirtualFile(psiFormFile.virtualFile)
    return file as JsonFile
}

/**
 * Creates new form in the correct directory with specified text and opens it in the editor
 *
 * @see createForm
 * @return Opened form psi file
 */
fun CodeInsightTestFixture.createFormAndConfigure(
    formName: String,
    text: String,
    module: String? = null
): JsonFile {
    val psiFormFile = createForm(formName, text, module)
    configureFromExistingVirtualFile(psiFormFile.virtualFile)
    return file as JsonFile
}

/**
 * Copies forms from testData directory to the correct directory and opens the first form in editor
 */
fun CodeInsightTestFixture.configureByForms(vararg formPaths: String, module: String? = null): JsonFile? {
    val modulePath = if(module == null) "" else "$module/"
    val virtualFiles = formPaths.map {
        copyFileToProject(it, "main/resources/config/forms/$modulePath$it")
    }
    virtualFiles.firstOrNull()?.let { configureFromExistingVirtualFile(it) }
    return file as? JsonFile
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
fun CodeInsightTestFixture.configureByFormText(text: String): JsonFile =
    this.configureByText(RootFormFileType, text) as JsonFile