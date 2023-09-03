package com.solanteq.solar.plugin.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.solanteq.solar.plugin.file.IncludedFormFileType
import com.solanteq.solar.plugin.file.RootFormFileType
import org.jetbrains.kotlin.idea.base.util.projectScope

/**
 * Checks whether this virtual file is root or included form by checking its file type
 */
fun VirtualFile.isForm() = fileType == RootFormFileType || fileType == IncludedFormFileType

/**
 * Checks whether this psi file is root or included form by checking its file type
 */
fun PsiFile.isForm() = fileType == RootFormFileType || fileType == IncludedFormFileType

/**
 * @see getFormModuleName
 */
fun VirtualFile.getFormModuleName() = getFormModuleDirectory()?.name

/**
 * @see getFormModuleDirectory
 */
fun PsiFile.getFormModuleName() = getFormModuleDirectory()?.name

/**
 * Gets the module (directory) of this form by virtual file structure.
 * This method does not rely on form file contents ("name" and "module" properties).
 * It only relies on parent directories.
 *
 * Note that not all forms are directly located in the corresponding module directory.
 * For example, some included forms may be located in `config/includes/forms/test/tabs`.
 * Their real module is `test`, but they are located in `tabs` directory.
 * This method will return `test` in this case.
 */
fun VirtualFile.getFormModuleDirectory(): VirtualFile? {
    if(!isForm()) {
        return null
    }

    var parent: VirtualFile? = parent
    var child: VirtualFile = this
    while(parent != null) {
        if(parent.name == "config" || parent.name == "resources") {
            return null
        }
        if(parent.name == "forms") {
            break
        }
        child = parent
        parent = parent.parent
    }

    if(!child.isDirectory) {
        return null
    }

    return child
}

/**
 * @see getFormModuleDirectory
 */
fun PsiFile.getFormModuleDirectory() = virtualFile?.getFormModuleDirectory()

/**
 * Gets the form name of this virtual file, or null if this file can't be treated as form
 * @see isForm
 */
fun VirtualFile.getFormSolarName(): String {
    val formModule = getFormModuleName() ?: return nameWithoutExtension

    return "$formModule.${nameWithoutExtension}"
}

/**
 * @see getFormSolarName
 */
fun PsiFile.getFormSolarName() = virtualFile?.getFormSolarName()

/**
 * Whether this [VirtualFile] is a directory and can be considered a form module
 */
fun VirtualFile.isRootFormModule() = isDirectory && path.endsWith("config/forms/$name")

/**
 * Whether this [PsiDirectory] can be considered a form module
 */
fun PsiDirectory.isRootFormModule() = virtualFile.isRootFormModule()

/**
 * Whether this [VirtualFile] is a directory and can contain included forms
 */
fun VirtualFile.isIncludedFormDirectory() = isDirectory && path.contains("config/includes/forms/")

/**
 * Whether this [PsiDirectory] can contain included forms
 */
fun PsiDirectory.isIncludedFormDirectory() = virtualFile.isIncludedFormDirectory()

/**
 * Whether this [VirtualFile] is a directory and can be considered
 * a root form module or can contain included forms
 */
fun VirtualFile.isFormModuleOrDirectory() = isRootFormModule() || isIncludedFormDirectory()

/**
 * Whether this [PsiDirectory] can be considered a root form module or can contain included forms
 */
fun PsiDirectory.isFormModuleOrDirectory() = isRootFormModule() || isIncludedFormDirectory()

/**
 * Finds first file that is contained in the specified project
 */
fun Collection<VirtualFile>.firstWithProjectPrioritization(project: Project?): VirtualFile? {
    ifEmpty { return null }
    project ?: return firstOrNull()
    if(size == 1) {
        return first()
    }
    val projectScope = project.projectScope()
    val onlyProjectForms = filter { it in projectScope }
    return onlyProjectForms.firstOrNull() ?: first()

}