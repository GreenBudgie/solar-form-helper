package com.solanteq.solar.plugin.element.creator

import com.intellij.json.psi.*
import com.solanteq.solar.plugin.element.FormJsonInclude
import com.solanteq.solar.plugin.element.base.AbstractFormElement
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.util.FormPsiUtils
import com.solanteq.solar.plugin.util.asListOrEmpty

/**
 * An interface that companion object of any [AbstractFormElement] that can be created as an
 * array entry should implement.
 */
abstract class FormArrayElementCreator<out ELEMENT : AbstractFormElement<JsonObject>> : FormElementCreator<ELEMENT, JsonObject>() {

    /**
     * Returns the required name of the property for it to be considered an array of specified type.
     * For example, a property named `groups` represents an array of groups.
     */
    abstract fun getArrayName(): String

    /**
     * Creates a new [AbstractFormElement] from the provided [sourceElement] if it's possible, or returns null.
     * This method **does not** check whether the parent array element has an appropriate name, but all other
     * validations are still done.
     */
    abstract fun createUnsafeFrom(sourceElement: JsonObject): ELEMENT?

    /**
     * The same as [createElementListFrom], but returns empty list if it's impossible to create the list from property.
     */
    fun createElementListFromOrEmpty(property: JsonProperty?) =
        createElementListFrom(property) ?: emptyList()

    /**
     * Collects all entries from an array that is a value of provided property and returns
     * a list of underlying [FormElement]s, or null if the list cannot be created from this property.
     */
    fun createElementListFrom(property: JsonProperty?): List<ELEMENT>? {
        property ?: return null
        if(property.name != getArrayName()) return null
        val valueArray = FormPsiUtils.getPropertyValue(property) as? JsonArray ?: return null
        return valueArray.valueList.flatMap { resolveValueToElements(it) }
    }

    /**
     * Creates a new [AbstractFormElement] from the provided [sourceElement] if it's possible, or returns null.
     * Performs [sourceElement] validation before creation, checks if it is applicable to the underlying [AbstractFormElement].
     * In this case it checks whether the parent property name is equal to [getArrayName].
     */
    override fun doCreate(sourceElement: JsonObject): ELEMENT? {
        if(!canBeCreatedAsListEntryFrom(sourceElement)) return null
        return createUnsafeFrom(sourceElement)
    }

    private fun resolveValueToElements(value: JsonValue): List<ELEMENT> {
        if(value is JsonObject) {
            return createUnsafeFrom(value).asListOrEmpty()
        }
        if(value !is JsonStringLiteral) return emptyList()
        val jsonInclude = FormJsonInclude.createFrom(value) ?: return emptyList()
        val referencedForm = jsonInclude.referencedFormPsiFile ?: return emptyList()
        val topLevelValue = referencedForm.topLevelValue ?: return emptyList()
        return when {
            topLevelValue is JsonObject -> createUnsafeFrom(topLevelValue).asListOrEmpty()
            topLevelValue is JsonArray && jsonInclude.type.isFlat ->
                topLevelValue.valueList.flatMap { resolveValueToElements(it) }
            else -> emptyList()
        }
    }

    private fun canBeCreatedAsListEntryFrom(sourceElement: JsonObject): Boolean {
        val jsonObject = sourceElement as? JsonObject ?: return false

        //TODO CAN_BE_OPTIMIZED make processParents function for performance
        val parentArrays = FormPsiUtils.parents(jsonObject)

        val containsParentPropertyWithArrayName = parentArrays.any {
            FormPsiUtils.isPropertyValueWithKey(it, getArrayName())
        }
        return containsParentPropertyWithArrayName
    }

}