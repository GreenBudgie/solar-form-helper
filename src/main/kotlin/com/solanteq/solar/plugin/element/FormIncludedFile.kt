package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.CachedValue
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.file.IncludedFormFileType
import com.solanteq.solar.plugin.search.FormSearch
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.kotlin.idea.base.util.minus

/**
 * Represents an included form file.
 *
 * Each included form is referenced via [FormJsonInclude] element from root or other included forms.
 */
class FormIncludedFile(
    sourceElement: JsonFile
) : FormElement<JsonFile>(sourceElement) {

    private var declarations: List<FormJsonInclude>? = null

    /**
     * Prevents infinite recursion
     */
    private var isSearchingForDeclarations = false

    /**
     * Finds [FormJsonInclude] elements that have a reference to this form.
     * Uses all scope to search for dependencies.
     *
     * Please note that some references might be in the modules that are not included as
     * dependencies so this is not guaranteed to return all JSON include declarations.
     *
     * Does not return declarations in the same file to prevent infinite recursion.
     *
     * Uses [isSearchingForDeclarations] field to prevent infinite recursion. See PLUGIN-2.
     */
    fun findDeclarations(): List<FormJsonInclude> {
        if(isSearchingForDeclarations) {
            return emptyList()
        }
        val foundDeclarations = declarations
        if(foundDeclarations != null) {
            return foundDeclarations
        }
        val containingFile = containingFile ?: return emptyList()
        isSearchingForDeclarations = true
        val searchScope = FormSearch.getFormSearchScope(project.allScope())
            .minus(GlobalSearchScope.fileScope(containingFile))

        val references = ProgressManager.getInstance().runProcess<Collection<PsiReference>>({
            ReferencesSearch.search(containingFile, searchScope).findAll()
        }, EmptyProgressIndicator())

        val referencedJsonElements = references.mapNotNull {
            it.element as? JsonStringLiteral
        }
        isSearchingForDeclarations = false
        declarations = referencedJsonElements.mapNotNull { it.toFormElement() }
        return declarations!!
    }

    /**
     * All root forms that included forms can lead to.
     *
     * Traverses up recursively through all included forms until the root form is found.
     */
    val allRootForms: List<FormRootFile> by lazy {
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

        override val key = Key<CachedValue<FormIncludedFile>>("solar.element.includedFile")

        override fun create(sourceElement: JsonElement): FormIncludedFile? {
            val jsonFile = sourceElement as? JsonFile ?: return null
            if(jsonFile.fileType == IncludedFormFileType) {
                return FormIncludedFile(jsonFile)
            }
            return null
        }

    }

}