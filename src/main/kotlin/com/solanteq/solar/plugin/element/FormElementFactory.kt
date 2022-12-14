package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.util.Key
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.util.FormPsiUtils
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

    return when(formElementClass) {

        FormTopLevelFile::class -> createElement(FormTopLevelFile)
        FormIncludedFile::class -> createElement(FormIncludedFile)
        FormRequest::class -> createElement(FormRequest)
        FormField::class -> createElement(FormField)
        FormJsonInclude::class -> createElement(FormJsonInclude)
        FormGroupRow::class -> createElement(FormGroupRow)
        FormGroup::class -> createElement(FormGroup)
        FormRow::class -> createElement(FormRow)
        FormInline::class -> createElement(FormInline)
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

    fun tryCreateElement(requiredPropertyName: String): FormPropertyArray<T>? {
        val key = Key<CachedValue<FormPropertyArray<T>?>>("solar.element.array.$requiredPropertyName")
        return CachedValuesManager.getCachedValue(this, key) {
            val arrayElement = if(requiredPropertyName == name)
                FormPropertyArray(this, contentsClass)
            else
                null

            CachedValueProvider.Result(
                arrayElement,
                PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }

    return when(contentsClass) {

        FormGroupRow::class -> tryCreateElement(FormGroupRow.ARRAY_NAME)
        FormGroup::class -> tryCreateElement(FormGroup.ARRAY_NAME)
        FormRow::class -> tryCreateElement(FormRow.ARRAY_NAME)
        FormField::class -> tryCreateElement(FormField.ARRAY_NAME)
        else -> null

    }
}

/**
 * A wrapper method that creates new form element with caching
 */
private fun <T : FormElement<*>> JsonElement.createElement(creator: FormElement.FormElementCreator<T>): T? {
    return CachedValuesManager.getCachedValue(this, creator.key) {
        CachedValueProvider.Result(
            creator.create(this),
            PsiModificationTracker.MODIFICATION_COUNT
        )
    }
}