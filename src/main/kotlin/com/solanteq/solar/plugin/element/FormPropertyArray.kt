package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonValue
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.util.FormPsiUtils
import com.solanteq.solar.plugin.util.asListOrEmpty
import kotlin.reflect.KClass

/**
 * Represents a json property with array value that contains form objects
 *
 * Example:
 * ```
 * "array": [
 *   {
 *     "key1": "value1"
 *   },
 *   {
 *     "key2": "value2"
 *   }
 * ]
 * ```
 */
class FormPropertyArray<T : FormElement<JsonObject>>(
    sourceElement: JsonProperty,
    private val formObjectClass: KClass<out T>
) : FormElement<JsonProperty>(sourceElement), List<T> {

    val valueArray by lazy {
        FormPsiUtils.getPropertyValue(sourceElement) as JsonArray
    }

    /**
     * Contents of this array represented as form object elements that this array stores.
     * It only returns valid form elements that can be resolved via [toFormElement].
     */
    val contents by lazy {
        valueArray.valueList.flatMap { resolveValueToFormElements(it) }
    }

    override val size: Int
        get() = contents.size

    override fun get(index: Int) = contents[index]

    override fun isEmpty() = contents.isEmpty()

    override fun iterator() = contents.iterator()

    override fun listIterator() = contents.listIterator()

    override fun listIterator(index: Int) = contents.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int) = contents.subList(fromIndex, toIndex)

    override fun lastIndexOf(element: T) = contents.lastIndexOf(element)

    override fun indexOf(element: T) = contents.indexOf(element)

    override fun containsAll(elements: Collection<T>) = contents.containsAll(elements)

    override fun contains(element: T) = contents.contains(element)

    private fun resolveValueToFormElements(value: JsonValue): List<T> {
        val jsonInclude = value.toFormElement<FormJsonInclude>()
        if(jsonInclude == null) {
            val resolvedElement = value.toFormElement(formObjectClass)
            return resolvedElement.asListOrEmpty()
        }

        val referencedForm = jsonInclude.referencedFormPsiFile ?: return emptyList()
        val topLevelValue = referencedForm.topLevelValue
        if(jsonInclude.type.isFlat) {
            if(topLevelValue !is JsonArray) return emptyList()
            val resolvedElements = topLevelValue.valueList.flatMap {
                resolveValueToFormElements(it)
            }
            return resolvedElements
        }

        val resolvedElement = topLevelValue.toFormElement(formObjectClass)
        return resolvedElement.asListOrEmpty()
    }

}