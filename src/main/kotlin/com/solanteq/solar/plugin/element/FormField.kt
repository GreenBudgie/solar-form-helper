package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.l10n.FormL10n
import com.solanteq.solar.plugin.l10n.search.L10nSearch
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolType
import com.solanteq.solar.plugin.util.RangeSplit
import com.solanteq.solar.plugin.util.valueAsString
import org.jetbrains.kotlin.idea.base.util.projectScope
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UField
import org.jetbrains.uast.toUElementOfType

/**
 * A single object inside `fields` array in [FormRow] element.
 *
 * Each form field contains a reference to corresponding data class field (property).
 * For simplicity, we will call fields in data classes "properties".
 * Any data class may have another data classes as properties,
 * they can have their own data classes as properties, and so on.
 * This structure will form a chain of properties, and the final one must be a primitive type:
 * ```
 * "client.type.id"
 * ```
 * Example:
 * ```
 * "fields": [
 *   { //Field 1
 *     "name": "property",
 *     "fieldSize": 4,
 *     "type": "STRING"
 *   },
 *   { //Field 2
 *     "name": "property.nestedProperty",
 *     "fieldSize": 4,
 *     "type": "STRING"
 *   }
 * ]
 * ```
 */
class FormField(
    sourceElement: JsonObject
) : FormLocalizableElement<JsonObject>(sourceElement, sourceElement) {

    override val localizations: List<String> by lazy {
        val formL10ns = L10nSearch.findFormL10ns(project, project.projectScope())
        return@lazy formL10ns
            .filter { it.type == FormL10n.L10nType.FIELD }
            .filter { this == it.referencedFieldElement }
            .map { it.value }
    }

    /**
     * Type of this field represented as plain string
     *
     * TODO introduce enum with field types
     */
    val type by lazy {
        val typeProperty = sourceElement.findProperty("type") ?: return@lazy null
        return@lazy typeProperty.valueAsString()
    }

    /**
     * A list of field names as a chain from main to nested ones represented as raw strings.
     *
     * Example:
     * ```
     * "fields": [
     *   {
     *     "name": "property.nestedProperty.nextNestedProperty.",
     *     "fieldSize": 4,
     *     "type": "STRING"
     *   }
     * ]
     * ```
     *
     * This will return:
     * ```
     * [
     *   "property",
     *   "nestedProperty",
     *   "nextNestedProperty",
     *   "" //We have an empty string here, because field name ends with a dot
     * ]
     * ```
     *
     */
    val fieldNameChain by lazy {
        namePropertyValue?.let { RangeSplit.from(it) } ?: RangeSplit.empty()
    }

    /**
     * A list of [FieldProperty] as a chain from main to nested ones represented as UAST fields.
     * Works similar to [fieldNameChain].
     *
     * If any nested property is not resolved, every property to the right won't be resolved too
     * and the returned chain will only contain references to resolved properties.
     *
     * Example: consider we have a chain of five fields `field1`, `field2`...
     * If we make a typo at `field3`, then only `field1` and `field2` will be resolved.
     * Further fields will be considered "fake", and symbols will be used to describe them.
     */
    val propertyChain by lazy {
        val stringPropertyChain = fieldNameChain
        if(stringPropertyChain.isEmpty()) {
            return@lazy emptyList()
        }

        val propertyChain = mutableListOf<FieldProperty>()
        var dataClasses: List<UClass> = findAllDataClassesFromRequests()

        stringPropertyChain.forEach { (textRange, fieldName) ->
            if(dataClasses.isEmpty()) {
                propertyChain += FieldProperty(
                    fieldName,
                    emptyList(),
                    null,
                    null,
                    FormSymbol.withElementTextRange(namePropertyValue!!, textRange, FormSymbolType.FIELD)
                )
                return@forEach
            }

            val (containingClass, field) = findClassAndFieldByNameInClasses(dataClasses, fieldName)
            if(field == null) {
                propertyChain += FieldProperty(
                    fieldName,
                    dataClasses,
                    containingClass,
                    null,
                    FormSymbol.withElementTextRange(namePropertyValue!!, textRange, FormSymbolType.FIELD)
                )
                return@forEach
            }

            propertyChain += FieldProperty(fieldName, dataClasses, containingClass, field, null)

            val nextDataClass = psiTypeAsUClassOrNull(field.type)
            dataClasses = if(nextDataClass != null) listOf(nextDataClass) else emptyList()
        }

        return@lazy propertyChain.toList()
    }

    private fun findAllDataClassesFromRequests(): List<UClass> {
        val containingRootForm = containingFile.toFormElement<FormRootFile>()
        if(containingRootForm != null) {
            return containingRootForm.allDataClassesFromRequests
        }

        val containingIncludedForm = containingFile.toFormElement<FormIncludedFile>() ?: return emptyList()
        return containingIncludedForm.allRootForms.flatMap {
            it.allDataClassesFromRequests
        }
    }

    private fun findClassAndFieldByNameInClasses(uClasses: List<UClass>, fieldName: String): Pair<UClass?, UField?> {
        uClasses.forEach { uClass ->
            uClass.javaPsi.allFields
                .find { it.name == fieldName }
                ?.let { return uClass to it.toUElementOfType() }
        }
        return null to null
    }

    private fun psiTypeAsUClassOrNull(psiType: PsiType): UClass? {
        val classReturnType = psiType as? PsiClassType ?: return null
        return classReturnType.resolve().toUElementOfType()
    }

    /**
     * @property name represents a name of this property.
     * Never null, but might be referencing to non-existing field.
     * @property applicableClasses List of classes from inline requests,
     * or a single class directly from source request
     * @property containingClass Data class containing this field
     * @property referencedField Real psi element resolved from [name] property
     * @property symbol If no field is found, this symbol can be used instead
     */
    data class FieldProperty(
        val name: String,
        val applicableClasses: List<UClass>,
        val containingClass: UClass?,
        val referencedField: UField?,
        val symbol: FormSymbol?
    )

    companion object : FormElementCreator<FormField> {

        const val ARRAY_NAME = "fields"

        override fun create(sourceElement: JsonElement): FormField? {
            if(canBeCreatedAsArrayElement(sourceElement, ARRAY_NAME)) {
                return FormField(sourceElement as JsonObject)
            }
            return null
        }

    }

}