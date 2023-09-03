package com.solanteq.solar.plugin.search

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.solanteq.solar.plugin.file.IncludedFormFileType
import com.solanteq.solar.plugin.file.RootFormFileType
import com.solanteq.solar.plugin.index.ROOT_FORM_INDEX_NAME
import com.solanteq.solar.plugin.util.firstWithProjectPrioritization
import com.solanteq.solar.plugin.util.getFormModuleName
import com.solanteq.solar.plugin.util.restrictedByFormFiles

object FormSearch {

    fun findRootForms(scope: GlobalSearchScope): Collection<VirtualFile> =
        FileTypeIndex.getFiles(RootFormFileType, scope.restrictedByFormFiles())

    fun findIncludedForms(scope: GlobalSearchScope): Collection<VirtualFile> =
        FileTypeIndex.getFiles(IncludedFormFileType, scope.restrictedByFormFiles())

    /**
     * Finds included and root forms
     */
    fun findAllForms(scope: GlobalSearchScope) =
        findRootForms(scope) + findIncludedForms(scope)

    fun findRootFormsInModule(scope: GlobalSearchScope, moduleName: String): Collection<VirtualFile> {
        if(moduleName.isBlank()) return emptySet()
        return findRootForms(scope).filter {
            it.getFormModuleName() == moduleName
        }
    }

    fun findIncludedFormsInModule(scope: GlobalSearchScope, moduleName: String): Collection<VirtualFile> {
        if(moduleName.isBlank()) return emptySet()
        return findIncludedForms(scope).filter {
            it.getFormModuleName() == moduleName
        }
    }

    /**
     * Finds a root form by its full solar name, for example: `test.testForm`
     */
    fun findRootFormBySolarName(
        fullName: String,
        scope: GlobalSearchScope
    ): VirtualFile? {
        val applicableForms = FileBasedIndex.getInstance().getContainingFiles(ROOT_FORM_INDEX_NAME, fullName, scope)
        return applicableForms.firstWithProjectPrioritization(scope.project)
    }

    /**
     * Finds root form by its module and name, for example:
     * ```
     * module = "test"
     * name = "testForm"
     * -> "test.testForm"
     * ```
     *
     * If multiple forms are found in project and libraries, forms in the project are prioritized.
     */
    fun findRootFormByModuleAndName(
        module: String,
        name: String,
        scope: GlobalSearchScope
    ) = findRootFormBySolarName("$module.$name", scope)

}