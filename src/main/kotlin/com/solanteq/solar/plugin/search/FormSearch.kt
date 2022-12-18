package com.solanteq.solar.plugin.search

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.solanteq.solar.plugin.file.IncludedFormFileType
import com.solanteq.solar.plugin.file.TopLevelFormFileType
import com.solanteq.solar.plugin.util.getFormSolarName
import com.solanteq.solar.plugin.util.getModuleAndNameByFormName

object FormSearch {

    fun findTopLevelForms(scope: GlobalSearchScope): Collection<VirtualFile> =
        FileTypeIndex.getFiles(TopLevelFormFileType, getFormSearchScope(scope))

    fun findIncludedForms(scope: GlobalSearchScope): Collection<VirtualFile> =
        FileTypeIndex.getFiles(IncludedFormFileType, getFormSearchScope(scope))

    /**
     * Finds included and top level forms
     */
    fun findAllForms(scope: GlobalSearchScope) =
        findTopLevelForms(scope) + findIncludedForms(scope)

    /**
     * Finds a form by its full solar name, for example: `test.testForm`
     */
    fun findFormBySolarName(
        fullName: String,
        scope: GlobalSearchScope
    ): VirtualFile? {
        val (module, name) = getModuleAndNameByFormName(fullName) ?: return null
        return findFormByModuleAndName(module, name, scope)
    }

    /**
     * Finds a form by its module and name, for example:
     * ```
     * module = "test"
     * name = "testForm"
     * -> "test.testForm"
     * ```
     */
    fun findFormByModuleAndName(
        module: String,
        name: String,
        scope: GlobalSearchScope
    ): VirtualFile? {
        val applicableForms = findAllForms(scope).filter {
            it.name == "$name.json"
        }
        return applicableForms.firstOrNull {
            it.getFormSolarName() == "$module.$name"
        }
    }

    /**
     * Returns the search scope restricted to only search in form files, top-level and included
     */
    fun getFormSearchScope(initialScope: GlobalSearchScope): GlobalSearchScope {
        return GlobalSearchScope.getScopeRestrictedByFileTypes(initialScope,
            TopLevelFormFileType, IncludedFormFileType)
    }

}