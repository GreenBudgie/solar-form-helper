package com.solanteq.solar.plugin.element.base.impl

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.element.base.FormPropertyArrayElement
import com.solanteq.solar.plugin.element.toFormElement
import kotlin.reflect.KClass

class FormPropertyArrayElementImpl<T : FormElement<JsonObject>>(
    sourceElement: JsonProperty,
    override val valueArray: JsonArray,
    private val formObjectClass: KClass<out T>
) : FormElementImpl<JsonProperty>(sourceElement), FormPropertyArrayElement<T> {

    override val contents by lazy {
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