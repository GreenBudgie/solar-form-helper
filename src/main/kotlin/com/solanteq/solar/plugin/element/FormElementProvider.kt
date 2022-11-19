package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.file.FormFileType
import com.solanteq.solar.plugin.util.isForm

/**
 * Converts this json element to form element with specified type
 *
 * @return Form element of specified type, or null if conversion is impossible
 *
 * @see FormElement
 */
inline fun <reified T : FormElement<*>> JsonElement?.toFormElement(): T? {
    this ?: return null

    if(this.containingFile?.isForm() == false) {
        return null
    }

    when(T::class) {

        FormFile::class -> {
            val jsonFile = this as? JsonFile ?: return null
            if(jsonFile.fileType == FormFileType) {
                return FormFile(jsonFile) as T
            }
            return null
        }

        FormRequest::class -> {
            val jsonProperty = this as? JsonProperty ?: return null
            if(jsonProperty.name in FormRequest.RequestType.requestLiterals) {
                return FormRequest(jsonProperty) as T
            }
            return null
        }

    }
    return null
}