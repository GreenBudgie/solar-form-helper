package com.solanteq.solar.plugin.search

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager

/**
 * Helps find directories corresponding to form modules. Uses caching.
 */
object FormModuleSearch {

    private val TOP_LEVEL_MODULES_KEY =
        Key<CachedValue<List<VirtualFile>>>("solar.topLevelFormModules")
    private val INCLUDED_MODULES_KEY =
        Key<CachedValue<List<VirtualFile>>>("solar.includedFormModules")

    fun findTopLevelFormModules(project: Project): List<VirtualFile> {
        return CachedValuesManager.getManager(project).getCachedValue(
            project,
            TOP_LEVEL_MODULES_KEY,
            {
                CachedValueProvider.Result(
                    getTopLevelFormModules(project),
                    VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
                )
            },
            false
        )
    }

    fun findIncludedFormModules(project: Project): List<VirtualFile> {
        return CachedValuesManager.getManager(project).getCachedValue(
            project,
            INCLUDED_MODULES_KEY,
            {
                CachedValueProvider.Result(
                    getIncludedFormModules(project),
                    VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
                )
            },
            false
        )
    }

    fun findFormModules(project: Project) =
        findTopLevelFormModules(project) + findIncludedFormModules(project)

    private fun getConfigDirectories(project: Project): List<VirtualFile> {
        val resourcesDirectories = ProjectRootManager.getInstance(project).contentSourceRoots
            .filter { it.name == "resources" }
        return resourcesDirectories.childDirectoriesWithName("config")
    }

    private fun getTopLevelFormModules(project: Project): List<VirtualFile> {
        return getConfigDirectories(project).getFormModulesInside()
    }

    private fun getIncludedFormModules(project: Project): List<VirtualFile> {
        val configDirectories = getConfigDirectories(project)
        val includesDirectories = configDirectories.childDirectoriesWithName("includes")
        return includesDirectories.getFormModulesInside()
    }

    private fun List<VirtualFile>.getFormModulesInside(): List<VirtualFile> {
        val formsDirectories = childDirectoriesWithName("forms")
        val formModuleDirectories = formsDirectories.flatMap {
            it.children.filter(VirtualFile::isDirectory)
        }
        return formModuleDirectories
    }

    private fun List<VirtualFile>.childDirectoriesWithName(name: String): List<VirtualFile> {
        return flatMap {
            it.children.filter { childDirectory ->
                childDirectory.isDirectory && childDirectory.name == name
            }
        }
    }

}