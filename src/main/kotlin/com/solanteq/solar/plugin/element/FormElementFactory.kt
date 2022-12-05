package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.element.base.FormObjectElement
import com.solanteq.solar.plugin.element.base.FormPropertyArrayElement
import com.solanteq.solar.plugin.file.TopLevelFormFileType
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

    if(containingFile?.isForm() == false) {
        return null
    }

    return when(formElementClass) {

        FormFile::class -> formFile()
        FormRequest::class -> formRequest()
        FormField::class -> formField()
        FormJsonInclude::class -> formJsonInclude()
        else -> null

    } as T?
}

/**
 * Converts this json property to form array element with specified inner object elements
 *
 * @return Form array element with contents of the specified type, or null if conversion is impossible
 */
inline fun <reified T : FormObjectElement> JsonProperty?.toFormArrayElement() = toFormArrayElement(T::class)

fun <T : FormObjectElement> JsonProperty?.toFormArrayElement(
    contentsClass: KClass<out T>
): FormPropertyArrayElement<T>? {
    this ?: return null

    if(containingFile?.isForm() == false) {
        return null
    }

    val valueArray = value
    if(valueArray !is JsonArray)  {
        return null
    }

    fun tryCreateElement(requiredPropertyName: String): FormPropertyArrayElement<T>? {
        return if(requiredPropertyName == name)
            FormPropertyArrayElement(this, valueArray, contentsClass)
        else
            null
    }

    return when(contentsClass) {

        FormField::class -> tryCreateElement(FormField.ARRAY_NAME)
        else -> null

    }
}

private fun JsonElement.formFile(): FormFile? {
    val jsonFile = this as? JsonFile ?: return null
    val topLevelObject = jsonFile.topLevelValue as? JsonObject ?: return null
    if(jsonFile.fileType == TopLevelFormFileType) {
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

private fun JsonElement.formField(): FormField? {
    val jsonObject = this as? JsonObject ?: return null
    val parentArray = jsonObject.parent as? JsonArray ?: return null
    val fieldProperty = parentArray.parent as? JsonProperty ?: return null
    if(fieldProperty.name == "fields") {
        return FormField(jsonObject)
    }
    return null
}

private fun JsonElement.formJsonInclude(): FormJsonInclude? {
    val stringLiteral = this as? JsonStringLiteral ?: return null
    val stringLiteralValue = stringLiteral.value
    val includeType = FormJsonInclude.JsonIncludeType.values().find {
        stringLiteralValue.startsWith(it.prefix)
    } ?: return null
    return FormJsonInclude(stringLiteral, includeType)
}