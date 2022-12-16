package com.solanteq.solar.plugin.util

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.solanteq.solar.plugin.file.IncludedFormFileType
import com.solanteq.solar.plugin.file.TopLevelFormFileType

/**
 * Checks whether this virtual file is top level or included form by checking its file type
 */
fun VirtualFile.isForm() = fileType == TopLevelFormFileType || fileType == IncludedFormFileType

/**
 * Checks whether this psi file is top level or included form by checking its file type
 */
fun PsiFile.isForm() = fileType == TopLevelFormFileType || fileType == IncludedFormFileType

/**
 * Gets the parent directory name of this form file, or null if this file can't be treated as form.
 * This directory may correspond to a form module, but that might not be true for included forms.
 */
fun VirtualFile.getFormModule(): String? {
    if(!isForm()) {
        return null
    }

    val parentDirectory = parent ?: return null
    if(!parentDirectory.isDirectory) {
        return null
    }

    val formsDirectory = parentDirectory.parent ?: return null
    if(!formsDirectory.isDirectory || formsDirectory.name != "forms") {
        return null
    }

    return parentDirectory.name
}

/**
 * @see getFormModule
 */
fun PsiFile.getFormModule() = virtualFile?.getFormModule()

/**
 * Gets the form name of this virtual file, or null if this file can't be treated as form
 * @see isForm
 */
fun VirtualFile.getFormSolarName(): String {
    val formModule = getFormModule() ?: return nameWithoutExtension

    return "$formModule.${nameWithoutExtension}"
}

/**
 * @see getFormSolarName
 */
fun PsiFile.getFormSolarName() = virtualFile?.getFormSolarName()

/**
 * Gets module and name by form full name, or null if the specified name has invalid format.
 *
 * Example:
 * ```
 * val (module, name) = getModuleAndNameByFormName("test.form") ?: return null
 * -> module = test
 * -> name = form
 * ```
 */
fun getModuleAndNameByFormName(fullName: String): Pair<String, String>? {
    val splitName = fullName.split(".")
    if(splitName.size != 2) {
        return null
    }
    return splitName[0] to splitName[1]
}







