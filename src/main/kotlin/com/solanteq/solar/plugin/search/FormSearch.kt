package com.solanteq.solar.plugin.search

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.solanteq.solar.plugin.file.IncludedFormFileType
import com.solanteq.solar.plugin.file.RootFormFileType
import com.solanteq.solar.plugin.util.getFormModuleName
import com.solanteq.solar.plugin.util.getFormSolarName

object FormSearch {

    fun findRootForms(scope: GlobalSearchScope): Collection<VirtualFile> =
        FileTypeIndex.getFiles(RootFormFileType, getFormSearchScope(scope))

    fun findIncludedForms(scope: GlobalSearchScope): Collection<VirtualFile> =
        FileTypeIndex.getFiles(IncludedFormFileType, getFormSearchScope(scope))

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

    fun findFormsInModule(scope: GlobalSearchScope, moduleName: String) =
        findRootFormsInModule(scope, moduleName) + findIncludedFormsInModule(scope, moduleName)

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
     * Returns the search scope restricted to only search in form files, root and included
     */
    fun getFormSearchScope(initialScope: GlobalSearchScope): GlobalSearchScope {
        return GlobalSearchScope.getScopeRestrictedByFileTypes(initialScope,
            RootFormFileType, IncludedFormFileType)
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
    private fun getModuleAndNameByFormName(fullName: String): Pair<String, String>? {
        val splitName = fullName.split(".")
        if(splitName.size != 2) {
            return null
        }
        return splitName[0] to splitName[1]
    }

}