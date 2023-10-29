package com.solanteq.solar.plugin.element.creator

import com.intellij.json.psi.JsonElement
import com.solanteq.solar.plugin.element.base.AbstractFormElement
import com.solanteq.solar.plugin.element.base.FormElement
import kotlinx.html.SOURCE

/**
 * An interface that companion object of any [AbstractFormElement] should implement.
 * It is used to create a form element [ELEMENT] from the source element [SOURCE].
 */
abstract class FormElementCreator<out ELEMENT : FormElement<in SOURCE>, in SOURCE : JsonElement> {

    /**
     * Creates a new [AbstractFormElement] from the provided [sourceElement] if it's possible, or returns null.
     * Performs [sourceElement] validation before creation, checks if it is applicable to the underlying [AbstractFormElement].
     */
    protected abstract fun doCreate(sourceElement: SOURCE): ELEMENT?

    /**
     * Creates a new [AbstractFormElement] from the provided [sourceElement] if it's possible, or returns null.
     * Performs [sourceElement] validation before creation, checks if it is applicable to the underlying [AbstractFormElement].
     * If [sourceElement] is null, returns null without any computations.
     */
    fun createFrom(sourceElement: SOURCE?): ELEMENT? = sourceElement?.let { doCreate(it) }

    /**
     * Whether the underlying [AbstractFormElement] can be created from the provided [sourceElement].
     */
    fun canBeCreatedFrom(sourceElement: SOURCE?) = createFrom(sourceElement) != null

    /**
     * Creates a new [AbstractFormElement] from the provided [sourceElement] if it's possible, or throws an exception.
     * Performs [sourceElement] validation before creation, checks if it is applicable to the underlying [AbstractFormElement].
     */
    fun createFromOrThrow(sourceElement: SOURCE?) = createFrom(sourceElement)
        ?: throw FormElementCreationException(this::class, sourceElement)

}