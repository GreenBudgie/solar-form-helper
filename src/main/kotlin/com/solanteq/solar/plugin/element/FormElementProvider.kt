package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.file.FormFileType
import com.solanteq.solar.plugin.util.isForm
import kotlin.reflect.KClass

/**
 * Converts this json element to form element with specified type
 *
 * @return Form element of specified type, or null if conversion is impossible
 *
 * @see FormElement
 */
inline fun <reified T : FormElement<*>> JsonElement?.toFormElement() = toFormElement(T::class)

/**
 * @see toFormElement
 */
@Suppress("UNCHECKED_CAST")
fun <T : FormElement<*>> JsonElement?.toFormElement(formElementClass: KClass<out T>): T? {
    this ?: return null

    if(this.containingFile?.isForm() == false) {
        return null
    }

    return when(formElementClass) {

        FormFile::class -> formFile()
        FormRequest::class -> formRequest()
        else -> null

    } as T?
}

private fun JsonElement.formFile(): FormFile? {
    val jsonFile = this as? JsonFile ?: return null
    val topLevelObject = jsonFile.topLevelValue as? JsonObject ?: return null
    if(jsonFile.fileType == FormFileType) {
        return FormFile(jsonFile, topLevelObject)
    }
    return null
}

private fun JsonElement.formRequest(): FormRequest? {
    val jsonProperty = this as? JsonProperty ?: return null
    if(jsonProperty.name in FormRequest.RequestType.requestLiterals) {
        return FormRequest(jsonProperty)
    }
    return null
}