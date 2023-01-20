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

    private val ROOT_MODULES_KEY =
        Key<CachedValue<List<VirtualFile>>>("solar.rootFormModules")
    private val INCLUDED_MODULES_KEY =
        Key<CachedValue<List<VirtualFile>>>("solar.includedFormModules")

    fun findRootFormModules(project: Project): List<VirtualFile> {
        return CachedValuesManager.getManager(project).getCachedValue(
            project,
            ROOT_MODULES_KEY,
            {
                CachedValueProvider.Result(
                    getRootFormModules(project),
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
        findRootFormModules(project) + findIncludedFormModules(project)

    private fun getConfigDirectories(project: Project): List<VirtualFile> {
        val sourceRoots = ProjectRootManager.getInstance(project).contentSourceRoots
        val directResourcesDirectories = sourceRoots.filter { it.name == "resources" }
        val srcDirectories = sourceRoots.filter { it.name == "src" }
        val mainDirectories = srcDirectories.childDirectoriesWithName("main")
        val resourcesDirectories = mainDirectories.childDirectoriesWithName("resources")

        val allResourcesDirectories = (directResourcesDirectories + resourcesDirectories).distinct()

        return allResourcesDirectories.childDirectoriesWithName("config")
    }

    private fun getRootFormModules(project: Project): List<VirtualFile> {
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