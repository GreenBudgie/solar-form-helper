package com.solanteq.solar.plugin.base

import com.intellij.json.psi.JsonFile
import com.intellij.openapi.vfs.VirtualFile
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
    module: String,
    text: String
): JsonFile {
    return addFileToProject(
        "main/resources/config/forms/$module/$formName.json",
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
    module: String,
    text: String
): JsonFile {
    val psiFormFile = createForm(formName, module, text)
    configureFromExistingVirtualFile(psiFormFile.virtualFile)
    return file as JsonFile
}

/**
 * Copies root forms from testData directory to the correct [module] directory and opens the first form in editor
 */
fun CodeInsightTestFixture.configureByRootForms(module: String, vararg formPaths: String): JsonFile {
    if (formPaths.isEmpty()) throw IllegalArgumentException("formPaths should not be empty")
    val virtualFiles = formPaths.map {
        copyFileToProject(it, "main/resources/config/forms/$module/$it")
    }
    configureFromExistingVirtualFile(virtualFiles.first())
    return file as JsonFile
}

/**
 * Copies included forms from testData directory to the correct directory using provided [relativePath]
 */
fun CodeInsightTestFixture.setUpIncludedForms(relativePath: String, vararg formPaths: String): List<VirtualFile> {
    if (formPaths.isEmpty()) throw IllegalArgumentException("formPaths should not be empty")
    return formPaths.map {
        copyFileToProject(it, "main/resources/config/includes/forms/$relativePath/$it")
    }
}

/**
 * Copies included forms from testData directory to the correct directory using provided [relativePath]
 * and opens the first form in editor
 */
fun CodeInsightTestFixture.configureByIncludedForms(relativePath: String, vararg formPaths: String): JsonFile {
    val includedForm = setUpIncludedForms(relativePath, *formPaths).first()
    configureFromExistingVirtualFile(includedForm)
    return file as JsonFile
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