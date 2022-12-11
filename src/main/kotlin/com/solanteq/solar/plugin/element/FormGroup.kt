package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.base.FormLocalizableElement

/**
 * A single object inside `groups` array in form or [FormGroupRow] element.
 *
 * Can contain rows with fields, inline configuration or tabs.
 * Actually, it can also be "detailed", but it's too rare to implement *right now*.
 */
class FormGroup(
    sourceElement: JsonObject
) : FormLocalizableElement<JsonObject>(sourceElement, sourceElement) {

    /**
     * [FormRow] array as content of the group.
     *
     * Not null when type is [GroupContentType.ROWS].
     */
    val rows by lazy {
        sourceElement.findProperty(FormRow.ARRAY_NAME).toFormArrayElement<FormRow>()
    }

    /**
     * [FormInline] elements as content of the group.
     *
     * Not null when type is [GroupContentType.INLINE].
     */
    val inline by lazy {
        sourceElement.findProperty("inline").toFormElement<FormInline>()
    }

    val contentType by lazy {
        rows?.let { return@lazy GroupContentType.ROWS }
        inline?.let { return@lazy GroupContentType.INLINE }
        return@lazy GroupContentType.INVALID
    }

    enum class GroupContentType {

        ROWS,
        INLINE,
        // TODO TABS,
        /**
         * Used when element has invalid configuration, that is contains no required properties
         */
        INVALID

    }

    companion object : FormElementCreator<FormGroup> {

        const val ARRAY_NAME = "groups"

        override fun create(sourceElement: JsonElement): FormGroup? {
            if(canBeCreatedAsArrayElement(sourceElement, ARRAY_NAME)) {
                return FormGroup(sourceElement as JsonObject)
            }
            return null
        }

    }

}