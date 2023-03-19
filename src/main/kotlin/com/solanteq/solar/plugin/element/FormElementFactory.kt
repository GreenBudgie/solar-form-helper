package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.util.FormPsiUtils
import kotlin.reflect.KClass

/**
 * Whether this JSON element can be converted to the form element of the specified type.
 *
 * This method also invokes [toFormElement], so it performs form element conversion
 * computation does not return it.
 */
inline fun <reified T : FormElement<*>> JsonElement?.isFormElement() = toFormElement<T>() != null

/**
 * Converts this json element to form element with specified type.
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

    return when(formElementClass) {

        FormRootFile::class -> FormRootFile.create(this)
        FormIncludedFile::class -> FormIncludedFile.create(this)
        FormRequest::class -> FormRequest.create(this)
        FormField::class -> FormField.create(this)
        FormJsonInclude::class -> FormJsonInclude.create(this)
        FormGroupRow::class -> FormGroupRow.create(this)
        FormGroup::class -> FormGroup.create(this)
        FormRow::class -> FormRow.create(this)
        FormInline::class -> FormInline.create(this)
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
): FormPropertyArray<T>? {
    this ?: return null

    val valueArray = FormPsiUtils.getPropertyValue(this)
    if(valueArray !is JsonArray)  {
        return null
    }

    fun tryCreateElement(requiredPropertyName: String) =
        if(requiredPropertyName == name)
            FormPropertyArray(this, contentsClass)
        else
            null

    return when(contentsClass) {

        FormGroupRow::class -> tryCreateElement(FormGroupRow.ARRAY_NAME)
        FormGroup::class -> tryCreateElement(FormGroup.ARRAY_NAME)
        FormRow::class -> tryCreateElement(FormRow.ARRAY_NAME)
        FormField::class -> tryCreateElement(FormField.ARRAY_NAME)
        else -> null

    }
}