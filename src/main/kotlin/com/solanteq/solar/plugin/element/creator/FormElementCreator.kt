package com.solanteq.solar.plugin.element.creator

import com.intellij.json.psi.JsonElement
import com.solanteq.solar.plugin.element.base.FormElement

/**
 * An interface that companion object of any [FormElement] should implement.
 * It is used to create a form element [ELEMENT] from the source element [SOURCE].
 */
abstract class FormElementCreator<out ELEMENT : FormElement<in SOURCE>, in SOURCE : JsonElement> {

    /**
     * Creates a new [FormElement] from the provided [sourceElement] if it's possible, or returns null.
     * Performs [sourceElement] validation before creation, checks if it is applicable to the underlying [FormElement].
     */
    protected abstract fun doCreate(sourceElement: SOURCE): ELEMENT?

    /**
     * Creates a new [FormElement] from the provided [sourceElement] if it's possible, or returns null.
     * Performs [sourceElement] validation before creation, checks if it is applicable to the underlying [FormElement].
     * If [sourceElement] is null, returns null without any computations.
     */
    fun createFrom(sourceElement: SOURCE?): ELEMENT? = sourceElement?.let { doCreate(it) }

    /**
     * Whether the underlying [FormElement] can be created from the provided [sourceElement].
     */
    fun canBeCreatedFrom(sourceElement: SOURCE?) = createFrom(sourceElement) != null

    /**
     * Creates a new [FormElement] from the provided [sourceElement] if it's possible, or throws an exception.
     * Performs [sourceElement] validation before creation, checks if it is applicable to the underlying [FormElement].
     */
    fun createFromOrThrow(sourceElement: SOURCE?) = createFrom(sourceElement)
        ?: throw FormElementCreationException(this::class, sourceElement)

}