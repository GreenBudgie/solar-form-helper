package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonProperty
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

        FormRequest::class -> {
            val jsonProperty = this as? JsonProperty ?: return null
            if(jsonProperty.name in FormRequest.requestLiterals) {
                return FormRequest(jsonProperty) as T
            }
            return null
        }

    }
    return null
}

fun JsonProperty?.toFormRequest() {

}