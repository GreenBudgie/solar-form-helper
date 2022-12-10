package com.solanteq.solar.plugin.element.base

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.element.toFormElement
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
class FormPropertyArrayElement<T : FormElement<JsonObject>>(
    sourceElement: JsonProperty,
    val valueArray: JsonArray,
    private val formObjectClass: KClass<out T>
) : FormElement<JsonProperty>(sourceElement), List<T> {

    /**
     * Contents of this array represented as form object elements that this array stores.
     * It only returns valid form elements that can be resolved via [toFormElement].
     *
     * TODO also resolve json include
     */
    val contents by lazy {
        valueArray.valueList.mapNotNull { it.toFormElement(formObjectClass) }
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

}