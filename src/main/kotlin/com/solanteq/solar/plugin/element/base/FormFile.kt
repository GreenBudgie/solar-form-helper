package com.solanteq.solar.plugin.element.base

import com.intellij.json.psi.JsonFile
import com.solanteq.solar.plugin.element.FormIncludedFile
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.element.creator.FormElementCreator

/**
 * Represents a form file, root or included.
 * When created using its companion object, returns an instance of [FormRootFile] or [FormIncludedFile],
 * or null if nothing is applicable.
 */
interface FormFile : FormElement<JsonFile> {

    companion object : FormElementCreator<FormFile, JsonFile>() {

        override fun doCreate(sourceElement: JsonFile): FormFile? {
            FormIncludedFile.createFrom(sourceElement)?.let { return it }
            FormRootFile.createFrom(sourceElement)?.let { return it }
            return null
        }

    }

}