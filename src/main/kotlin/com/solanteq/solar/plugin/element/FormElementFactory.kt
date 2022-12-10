package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.element.base.FormPropertyArrayElement
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

        FormTopLevelFile::class -> FormTopLevelFile.create(this)
        FormRequest::class -> FormRequest.create(this)
        FormField::class -> FormField.create(this)
        FormJsonInclude::class -> FormJsonInclude.create(this)
        FormGroupRow::class -> FormGroupRow.create(this)
        FormGroup::class -> FormGroup.create(this)
        FormRow::class -> FormRow.create(this)
        else -> null

    } as T?
}

/**
 * Converts this json property to form array element with specified inner object elements
 *
 * @return Form array element with contents of the specified type, or null if conversion is impossible
 */
inline fun <reified T : FormElement<JsonObject>> JsonProperty?.toFormArrayElement() = toFormArrayElement(T::class)

/**
 * @see toFormArrayElement
 */
fun <T : FormElement<JsonObject>> JsonProperty?.toFormArrayElement(
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

        FormGroupRow::class -> tryCreateElement(FormGroupRow.ARRAY_NAME)
        FormGroup::class -> tryCreateElement(FormGroup.ARRAY_NAME)
        FormRow::class -> tryCreateElement(FormRow.ARRAY_NAME)
        FormField::class -> tryCreateElement(FormField.ARRAY_NAME)
        else -> null

    }
}