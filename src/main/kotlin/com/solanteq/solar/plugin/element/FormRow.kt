package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.base.AbstractFormElement
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.element.creator.FormArrayElementCreator
import com.solanteq.solar.plugin.element.expression.ExpressionAware
import com.solanteq.solar.plugin.element.expression.ExpressionAwareImpl
import com.solanteq.solar.plugin.util.FormPsiUtils

/**
 * A single object inside `rows` array in [FormGroup] element
 */
class FormRow(
    sourceElement: JsonObject,
) : AbstractFormElement<JsonObject>(sourceElement), ExpressionAware by ExpressionAwareImpl(sourceElement) {

    override val parents by lazy(LazyThreadSafetyMode.PUBLICATION) {
        containingGroups
    }

    override val children by lazy(LazyThreadSafetyMode.PUBLICATION) {
        fields ?: emptyList()
    }

    /**
     * All groups that contain this row.
     *
     * Multiple containing groups can exist if this row is in included form
     */
    val containingGroups: List<FormGroup> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        FormPsiUtils.firstParentsOfType(sourceElement, JsonObject::class).mapNotNull {
            FormGroup.createFrom(it)
        }
    }

    /**
     * All fields form `field` property.
     *
     * Returns null if `field` property does not exist.
     */
    val fields by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val fieldsProperty = sourceElement.findProperty(FormField.getArrayName())
        FormField.createElementListFrom(fieldsProperty)
    }

    companion object : FormArrayElementCreator<FormRow>() {

        override fun getArrayName() = "rows"

        override fun createUnsafeFrom(sourceElement: JsonObject) = FormRow(sourceElement)

    }

}