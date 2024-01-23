package com.solanteq.solar.plugin.search

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.solanteq.solar.plugin.element.FormIncludedFile
import com.solanteq.solar.plugin.file.IncludedFormFileType
import com.solanteq.solar.plugin.file.RootFormFileType
import com.solanteq.solar.plugin.index.JSON_INCLUDE_DECLARATION_INDEX_NAME
import com.solanteq.solar.plugin.index.JsonIncludeDeclarationSearch
import com.solanteq.solar.plugin.util.isRootForm
import com.solanteq.solar.plugin.util.restrictedByFileTypes
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.kotlin.idea.base.util.minus

/**
 * Forms containing JSON include declarations produce a form graph.
 * - Parent forms are forms that contain a reference to this included form via JSON include.
 * It means that root forms cannot contain any parent forms.
 * - Child forms are forms that are referenced via JSON includes from this form.
 * It means that both root and included forms may have child forms.
 *
 * All these relations form a tree (graph) with root forms on top of it.
 */
object FormGraphSearch {

    /**
     * Finds direct parents of this form.
     * Always returns empty set if this [formFile] is a root form.
     */
    fun findParentForms(
        project: Project,
        formFile: VirtualFile,
        skipFiles: MutableSet<VirtualFile> = mutableSetOf()
    ): Set<VirtualFile> {
        if (formFile in skipFiles) return emptySet()
        skipFiles += formFile
        if (formFile.fileType == RootFormFileType) return emptySet()
        val relativePath = FormIncludedFile.getRelativePathByIncludedFormVirtualFile(formFile) ?: return emptySet()
        val scope = project
            .allScope()
            .minus(GlobalSearchScope.filesScope(project, skipFiles))
        val parentForms = JsonIncludeDeclarationSearch.getFilesContainingDeclaration(relativePath, scope)
        return parentForms.toSet()
    }

    /**
     * Recursively finds all parents of this form, meaning that it also searches for parents of the parent forms,
     * and so on.
     * Always returns empty set if this [formFile] is a root form.
     */
    fun findParentFormsRecursively(
        project: Project,
        formFile: VirtualFile,
        skipFiles: MutableSet<VirtualFile> = mutableSetOf()
    ): Set<VirtualFile> {
        if (formFile in skipFiles) return emptySet()
        if (formFile.fileType == RootFormFileType) {
            return emptySet()
        }
        val parentForms = findParentForms(project, formFile, skipFiles)
        val recursivelyFoundForms = parentForms.flatMap {
            findParentFormsRecursively(project, it, skipFiles)
        }.toSet()
        return recursivelyFoundForms + parentForms
    }

    /**
     * Recursively processes all parents of this form, meaning that it also processes parents of the parent forms,
     * and so on.
     * Always returns empty set if this [formFile] is a root form.
     * Stops processing and returns null if [processor] returned `false` at some point.
     * If processor did not return null, returns a set of found forms.
     */
    fun processParentFormsRecursively(
        project: Project,
        formFile: VirtualFile,
        skipFiles: MutableSet<VirtualFile> = mutableSetOf(),
        processor: (VirtualFile) -> Boolean = { true }
    ): Set<VirtualFile>? {
        if (formFile in skipFiles) return emptySet()
        if (formFile.fileType == RootFormFileType) return emptySet()
        val parentForms = findParentForms(project, formFile, skipFiles).onEach {
            if (!processor(it)) {
                return null // Stop processing when it is not necessary anymore
            }
        }
        val recursivelyFoundForms = parentForms.flatMap {
            processParentFormsRecursively(project, it, skipFiles, processor) ?: return null
        }.toSet()
        return recursivelyFoundForms + parentForms
    }

    /**
     * Finds direct children of this form
     */
    fun findChildForms(
        project: Project,
        formFile: VirtualFile,
        skipFiles: MutableSet<VirtualFile> = mutableSetOf()
    ): Set<VirtualFile> {
        if (formFile in skipFiles) return emptySet()
        skipFiles += formFile
        val jsonIncludeDeclarationsInFile = FileBasedIndex.getInstance().getFileData(
            JSON_INCLUDE_DECLARATION_INDEX_NAME,
            formFile,
            project
        ).keys
        val scope = project
            .allScope()
            .restrictedByFileTypes(IncludedFormFileType)
            .minus(GlobalSearchScope.filesScope(project, skipFiles))
        val childIncludedForms = jsonIncludeDeclarationsInFile.flatMap {
            FormSearch.findIncludedFormsByRelativePath(it, scope)
        }.toSet()
        return childIncludedForms
    }

    /**
     * Recursively finds all children of this form, meaning that it also searches for children of the child forms,
     * and so on.
     */
    fun findChildFormsRecursively(
        project: Project,
        formFile: VirtualFile,
        skipFiles: MutableSet<VirtualFile> = mutableSetOf(),
    ): Set<VirtualFile> {
        if (formFile in skipFiles) return emptySet()
        val childForms = findChildForms(project, formFile, skipFiles)
        val recursivelyFoundForms = childForms.flatMap {
            findChildFormsRecursively(project, it, skipFiles)
        }.toSet()
        return recursivelyFoundForms + childForms
    }

    /**
     * Recursively processes all children of this form, meaning that it also processes children of the child forms,
     * and so on.
     * Stops processing and returns null if [processor] returned `false` at some point.
     * If processor did not return null, returns a set of found forms.
     */
    fun processChildFormsRecursively(
        project: Project,
        formFile: VirtualFile,
        skipFiles: MutableSet<VirtualFile> = mutableSetOf(),
        skipProcessing: Set<VirtualFile> = emptySet(),
        processor: (VirtualFile) -> Boolean = { true }
    ): Set<VirtualFile>? {
        if (formFile in skipFiles) return emptySet()
        val childForms = findChildForms(project, formFile, skipFiles).onEach {
            if (it !in skipProcessing && !processor(it)) {
                return null // Stop processing when it is not necessary anymore
            }
        }
        val recursivelyFoundForms = childForms.flatMap {
            processChildFormsRecursively(project, it, skipFiles, skipProcessing, processor) ?: return null
        }.toSet()
        return recursivelyFoundForms + childForms
    }

    /**
     * Recursively finds all root forms on top of the graph.
     */
    fun findTopmostRootForms(
        project: Project,
        formFile: VirtualFile,
        skipFiles: MutableSet<VirtualFile> = mutableSetOf()
    ) = findParentFormsRecursively(project, formFile, skipFiles).filter { it.fileType == RootFormFileType }.toSet()

    /**
     * Recursively finds all child forms for each parent form.
     * This method gives all the forms that need to be considered "linked" together.
     * Use it for reference search.
     *
     * TODO CAN_BE_OPTIMIZED
     */
    fun findAllRelatedForms(
        project: Project,
        formFile: VirtualFile,
        includeSelf: Boolean = false
    ): Set<VirtualFile> {
        val rootForms = findTopmostRootForms(project, formFile).addCurrentFormIfRoot(formFile)
        val skipFiles = mutableSetOf<VirtualFile>()
        val childFormsOfRootForms = rootForms.flatMap {
            findChildFormsRecursively(project, it, skipFiles)
        }.toSet()
        return if (includeSelf) {
            childFormsOfRootForms + rootForms
        } else {
            childFormsOfRootForms + rootForms - formFile
        }
    }

    /**
     * Recursively processes all child forms for each parent form.
     * This method processes all the forms that need to be considered "linked" together.
     * Stops processing and returns null if [processor] returned `false` at some point.
     * If processor did not return null, returns a set of found forms.
     *
     * TODO CAN_BE_OPTIMIZED
     */
    fun processAllRelatedForms(
        project: Project,
        formFile: VirtualFile,
        includeSelf: Boolean = false,
        processor: (VirtualFile) -> Boolean = { true }
    ): Set<VirtualFile>? {
        if (includeSelf && !processor(formFile)) {
            return null
        }
        val parentForms = processParentFormsRecursively(project, formFile, processor = processor)
            ?.addCurrentFormIfRoot(formFile) ?: return null
        val topmostRootForms = parentForms.filter { it.fileType == RootFormFileType }
        val childFormsOfRootForms = topmostRootForms.flatMap {
            processChildFormsRecursively(
                project,
                it,
                skipProcessing = parentForms + formFile,
                processor = processor
            ) ?: return null
        }.toSet()
        return if (includeSelf) {
            childFormsOfRootForms + parentForms
        } else {
            childFormsOfRootForms + parentForms - formFile
        }
    }

    private fun Set<VirtualFile>.addCurrentFormIfRoot(formFile: VirtualFile): Set<VirtualFile> {
        if (formFile.isRootForm()) {
            return this + formFile
        }
        return this
    }

}