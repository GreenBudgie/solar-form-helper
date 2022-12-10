package com.solanteq.solar.plugin.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.solanteq.solar.plugin.file.AbstractFormFileType
import com.solanteq.solar.plugin.file.IncludedFormFileType
import com.solanteq.solar.plugin.file.TopLevelFormFileType
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.kotlin.idea.base.util.projectScope

private val TOP_LEVEL_FORMS_ALL_SCOPE_KEY = Key<CachedValue<List<VirtualFile>>>("solar.topLevelFormsAllScope")
private val INCLUDED_FORMS_ALL_SCOPE_KEY = Key<CachedValue<List<VirtualFile>>>("solar.includedFormsAllScope")
private val TOP_LEVEL_FORMS_PROJECT_SCOPE_KEY = Key<CachedValue<List<VirtualFile>>>("solar.topLevelFormsProjectScope")
private val INCLUDED_FORMS_PROJECT_SCOPE_KEY = Key<CachedValue<List<VirtualFile>>>("solar.includedFormsProjectScope")

/**
 * Finds top level forms with caching in either all scope or project scope
 */
fun findTopLevelForms(project: Project, projectScope: Boolean = false) =
    findForms(
        project,
        TopLevelFormFileType,
        projectScope
    )

/**
 * Finds included forms with caching in either all scope or project scope
 */
fun findIncludedForms(project: Project, projectScope: Boolean = false) =
    findForms(
        project,
        IncludedFormFileType,
        projectScope
    )

/**
 * Finds included and top level forms with caching in either all scope or project scope
 */
fun findAllForms(project: Project, projectScope: Boolean = false) =
    findTopLevelForms(project, projectScope) + findIncludedForms(project, projectScope)

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

    val parentDirectory = parent
    if(!parentDirectory.isDirectory) {
        return null
    }

    val formsDirectory = parentDirectory.parent
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
 * Finds a form by its full name in all scope, or null if not found
 */
fun findFormByFullName(
    project: Project,
    fullName: String,
    scope: GlobalSearchScope = project.allScope()
): VirtualFile? {
    val (module, name) = getModuleAndNameByFormName(fullName) ?: return null
    return findFormByModuleAndName(project, module, name, scope)
}

/**
 * Finds a form by its module and name in all scope, or null if not found
 */
fun findFormByModuleAndName(
    project: Project,
    module: String,
    name: String,
    scope: GlobalSearchScope = project.allScope()
): VirtualFile? {
    val applicableFilesByName = FilenameIndex.getVirtualFilesByName(
        "$name.json",
        scope
    )
    return applicableFilesByName.firstOrNull {
        it.getFormSolarName() == "$module.$name"
    }
}

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

private fun findForms(
    project: Project,
    fileType: AbstractFormFileType,
    projectScope: Boolean
): List<VirtualFile> {
    val key = if(fileType is TopLevelFormFileType) {
        if(projectScope) TOP_LEVEL_FORMS_PROJECT_SCOPE_KEY else TOP_LEVEL_FORMS_ALL_SCOPE_KEY
    } else {
        if(projectScope) INCLUDED_FORMS_PROJECT_SCOPE_KEY else INCLUDED_FORMS_ALL_SCOPE_KEY
    }
    val scope = if(projectScope) project.projectScope() else project.allScope()
    return CachedValuesManager.getManager(project).getCachedValue(
        project,
        key,
        {
            val jsonFiles = FilenameIndex.getAllFilesByExt(project, "json", scope)
            CachedValueProvider.Result(
                jsonFiles
                    .filter { it.fileType == fileType }
                    .sortedBy { it.name }
                    .toList(),
                VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
            )
        },
        false)
}









