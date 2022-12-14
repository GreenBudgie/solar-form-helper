package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.Key
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
 * Each included form is referenced via [FormJsonInclude] element
 * from top level or other included forms.
 */
class FormIncludedFile(
    sourceElement: JsonFile
) : FormElement<JsonFile>(sourceElement) {

    /**
     * A list of [FormJsonInclude] elements that have a reference to this form.
     * Uses all scope to search for dependencies.
     *
     * Please note that some references might be in the modules that are not included as
     * dependencies so this is not guaranteed to return all JSON include declarations.
     *
     * Does not return declarations in the same file to prevent infinite recursion.
     *
     * TODO make the search faster
     */
    val declarations by lazy {
        val containingFile = containingFile ?: return@lazy emptyList()
        val searchScope = FormSearch.getFormSearchScope(project.allScope())
            .minus(GlobalSearchScope.fileScope(containingFile))
        val references = ReferencesSearch.search(containingFile, searchScope).findAll()
        val referencedJsonElements = references.mapNotNull {
            it.element as? JsonStringLiteral
        }
        return@lazy referencedJsonElements.mapNotNull { it.toFormElement<FormJsonInclude>() }
    }

    /**
     * All top-level forms that included forms can lead to.
     *
     * Traverses up recursively through all included forms until the top-level form is found.
     *
     * //TODO test recursive references
     */
    val allTopLevelForms: List<FormTopLevelFile> by lazy {
        val containingFilesOfDeclarations = declarations.mapNotNull {
            it.sourceElement.containingFile?.originalFile as? JsonFile
        }.distinct().filter { it != containingFile?.originalFile }

        val topLevelFormsOfDeclarations = containingFilesOfDeclarations.mapNotNull {
            it.toFormElement<FormTopLevelFile>()
        }
        val includedFormsOfDeclarations = containingFilesOfDeclarations.mapNotNull {
            it.toFormElement<FormIncludedFile>()
        }

        val recursivelyCollectedTopLevelForms = includedFormsOfDeclarations.flatMap {
            it.allTopLevelForms
        }

        return@lazy recursivelyCollectedTopLevelForms + topLevelFormsOfDeclarations
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