package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.psi.search.searches.ReferencesSearch
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.file.IncludedFormFileType

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
     * dependencies. So this is not guaranteed to return all references.
     *
     * Returns empty list if no references are found.
     */
    val references by lazy {
        val containingFile = containingFile ?: return@lazy emptyList()
        val references = ReferencesSearch.search(containingFile).findAll()
        val referencedJsonElements = references.mapNotNull {
            it.element as? JsonElement
        }
        return@lazy referencedJsonElements.mapNotNull { it.toFormElement<FormJsonInclude>() }
    }

    companion object : FormElementCreator<FormIncludedFile> {

        override fun create(sourceElement: JsonElement): FormIncludedFile? {
            val jsonFile = sourceElement as? JsonFile ?: return null
            if(jsonFile.fileType == IncludedFormFileType) {
                return FormIncludedFile(jsonFile)
            }
            return null
        }

    }

}