package com.solanteq.solar.plugin.element.creator

import com.intellij.json.psi.*
import com.solanteq.solar.plugin.element.*
import com.solanteq.solar.plugin.element.base.AbstractFormElement
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.element.expression.FormExpression

/**
 * If you don't know which [AbstractFormElement] can be created from [JsonElement], you can use this factory
 */
object FormElementFactory {

    /**
     * Creates [AbstractFormElement] from provided [sourceElement] if it is possible, or returns null.
     */
    fun createElement(sourceElement: JsonElement): AbstractFormElement<*>? {
        if (sourceElement is JsonObject) {
            FormExpression.createFrom(sourceElement)?.let { return it }
            FormGroup.createFrom(sourceElement)?.let { return it }
            FormGroupRow.createFrom(sourceElement)?.let { return it }
            FormRow.createFrom(sourceElement)?.let { return it }
            FormField.createFrom(sourceElement)?.let { return it }
        }
        if (sourceElement is JsonFile) {
            FormRootFile.createFrom(sourceElement)?.let { return it }
            FormIncludedFile.createFrom(sourceElement)?.let { return it }
        }
        if (sourceElement is JsonProperty) {
            FormRequest.createFrom(sourceElement)?.let { return it }
            FormInline.createFrom(sourceElement)?.let { return it }
        }
        if (sourceElement is JsonStringLiteral) {
            FormJsonInclude.createFrom(sourceElement)?.let { return it }
        }
        return null
    }

    /**
     * Creates a [FormLocalizableElement] from provided [sourceElement] if it is possible, or returns null.
     */
    fun createLocalizableElement(sourceElement: JsonElement): FormLocalizableElement<*>? {
        if (sourceElement is JsonObject) {
            FormGroup.createFrom(sourceElement)?.let { return it }
            FormField.createFrom(sourceElement)?.let { return it }
        }
        if (sourceElement is JsonFile) {
            FormRootFile.createFrom(sourceElement)?.let { return it }
        }
        return null
    }
    
}