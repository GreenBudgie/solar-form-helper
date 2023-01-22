package com.solanteq.solar.plugin.search

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.libraries.LibraryUtil
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.util.CachedValue
import com.solanteq.solar.plugin.util.cacheByKey

/**
 * Helps find directories corresponding to form modules. Uses caching.
 *
 * Actually, included forms do not have modules. So, we treat their base directories as modules.
 */
object FormModuleSearch {

    private val PROJECT_ROOT_MODULES_KEY =
        Key<CachedValue<List<VirtualFile>>>("solar.projectRootFormModules")
    private val PROJECT_INCLUDED_BASE_DIRECTORIES_KEY =
        Key<CachedValue<List<VirtualFile>>>("solar.projectIncludedFormBaseDirectories")

    private val LIBRARY_ROOT_MODULES_KEY =
        Key<CachedValue<List<VirtualFile>>>("solar.libraryRootFormModules")
    private val LIBRARY_INCLUDED_BASE_DIRECTORIES_KEY =
        Key<CachedValue<List<VirtualFile>>>("solar.libraryIncludedFormBaseDirectories")

    fun findProjectRootFormModules(project: Project) =
        cacheByKey(project, PROJECT_ROOT_MODULES_KEY, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS) {
            getProjectConfigDirectories(project).getRootFormModulesInConfigDirectories()
        }

    fun findProjectIncludedFormBaseDirectories(project: Project) =
        cacheByKey(project, PROJECT_INCLUDED_BASE_DIRECTORIES_KEY, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS) {
            getProjectConfigDirectories(project).getIncludedFormModulesInConfigDirectories()
        }

    fun findLibrariesRootFormModules(project: Project) =
        cacheByKey(project, LIBRARY_ROOT_MODULES_KEY, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS) {
            getLibrariesConfigDirectories(project).getRootFormModulesInConfigDirectories()
        }

    fun findLibrariesIncludedFormDirectories(project: Project) =
        cacheByKey(project, LIBRARY_INCLUDED_BASE_DIRECTORIES_KEY, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS) {
            getLibrariesConfigDirectories(project).getIncludedFormModulesInConfigDirectories()
        }

    fun findAllProjectFormModules(project: Project) =
        findProjectRootFormModules(project) + findProjectIncludedFormBaseDirectories(project)

    fun findAllLibrariesFormModules(project: Project) =
        findLibrariesRootFormModules(project) + findLibrariesIncludedFormDirectories(project)

    fun findAllFormModules(project: Project) =
        findAllProjectFormModules(project) + findAllLibrariesFormModules(project)

    private fun List<VirtualFile>.getRootFormModulesInConfigDirectories() =
        getFormModulesInside()

    private fun List<VirtualFile>.getIncludedFormModulesInConfigDirectories() =
        childDirectoriesWithName("includes").getFormModulesInside()

    private fun getLibrariesConfigDirectories(project: Project): List<VirtualFile> {
        return LibraryUtil.getLibraryRoots(project).toList().childDirectoriesWithName("config")
    }

    private fun getProjectConfigDirectories(project: Project): List<VirtualFile> {
        val sourceRoots = ProjectRootManager.getInstance(project).contentSourceRoots
        val directResourcesDirectories = sourceRoots.filter { it.name == "resources" }
        val srcDirectories = sourceRoots.filter { it.name == "src" }
        val mainDirectories = srcDirectories.childDirectoriesWithName("main")
        val resourcesDirectories = mainDirectories.childDirectoriesWithName("resources")

        val allResourcesDirectories = (directResourcesDirectories + resourcesDirectories).distinct()

        return allResourcesDirectories.childDirectoriesWithName("config")
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