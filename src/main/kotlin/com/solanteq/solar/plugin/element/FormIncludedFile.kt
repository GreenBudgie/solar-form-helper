package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.file.IncludedFormFileType
import com.solanteq.solar.plugin.index.JsonIncludeFileIndex
import com.solanteq.solar.plugin.util.restrictedByFormFiles
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.kotlin.idea.base.util.minus
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents an included form file.
 *
 * Each included form is referenced via [FormJsonInclude] element from root or other included forms.
 */
class FormIncludedFile(
    sourceElement: JsonFile
) : FormElement<JsonFile>(sourceElement) {

    /**
     * Finds [FormJsonInclude] elements that have a reference to this form.
     * Uses all scope to search for dependencies.
     *
     * Does not return declarations in the same file to prevent infinite recursion.
     *
     * TODO It uses a very unsafe approach of returning empty list when this file is already processing.
     * This can cause issues that are unknown for now
     */
    fun findDeclarations(): List<FormJsonInclude> {
        val containingFile = containingFile ?: return emptyList()
        if(!concurrentProcessingFileSet.add(containingFile)) {
            return emptyList()
        }
        val baseSearchScope = project.allScope()
            .restrictedByFormFiles()
            .minus(GlobalSearchScope.fileScope(containingFile)) as GlobalSearchScope

        val filesToSearch = JsonIncludeFileIndex.getFilesContainingDeclaration(
            containingFile.name, baseSearchScope
        )

        val effectiveFileScope = GlobalSearchScope.filesScope(project, filesToSearch)

        val references = ProgressManager.getInstance().runProcess<Collection<PsiReference>>({
            ReferencesSearch.search(containingFile, effectiveFileScope).findAll()
        }, EmptyProgressIndicator())

        val referencedJsonElements = references.mapNotNull {
            it.element as? JsonStringLiteral
        }
        val result: List<FormJsonInclude> = referencedJsonElements.mapNotNull { it.toFormElement() }

        concurrentProcessingFileSet.remove(containingFile)
        return result
    }

    /**
     * All root forms that included form can lead to.
     *
     * Traverses up recursively through all included forms until the root form is found.
     */
    val allRootForms: List<FormRootFile> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val containingFilesOfDeclarations = findDeclarations().mapNotNull {
            it.sourceElement.containingFile?.originalFile as? JsonFile
        }.distinct().filter { it != containingFile?.originalFile }

        val rootFormsOfDeclarations = containingFilesOfDeclarations.mapNotNull {
            it.toFormElement<FormRootFile>()
        }
        val includedFormsOfDeclarations = containingFilesOfDeclarations.mapNotNull {
            it.toFormElement<FormIncludedFile>()
        }

        val recursivelyCollectedRootForms = includedFormsOfDeclarations.flatMap {
            it.allRootForms
        }

        return@lazy recursivelyCollectedRootForms + rootFormsOfDeclarations
    }

    companion object : FormElementCreator<FormIncludedFile> {

        private val concurrentProcessingFileSet =
            Collections.newSetFromMap(ConcurrentHashMap<JsonFile, Boolean>())

        override fun create(sourceElement: JsonElement): FormIncludedFile? {
            val jsonFile = sourceElement as? JsonFile ?: return null
            if(jsonFile.fileType == IncludedFormFileType) {
                return FormIncludedFile(jsonFile)
            }
            return null
        }

    }

}