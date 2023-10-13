package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonObject
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiField
import com.intellij.psi.PsiType
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.element.creator.FormArrayElementCreator
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolType
import com.solanteq.solar.plugin.util.FormPsiUtils
import com.solanteq.solar.plugin.util.RangeSplit
import com.solanteq.solar.plugin.util.valueAsIntOrNull
import com.solanteq.solar.plugin.util.valueAsStringOrNull

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
class FormField private constructor(
    sourceElement: JsonObject
) : FormLocalizableElement<JsonObject>(sourceElement, sourceElement) {

    override val l10nKeys: List<String> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val containingGroups = containingGroups
        containingGroups.flatMap {
            it.l10nKeys.map { key -> "$key.$name" }
        }
    }

    val fieldSize by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val fieldSizeProperty = sourceElement.findProperty("fieldSize") ?: return@lazy null
        fieldSizeProperty.valueAsIntOrNull()
    }

    val labelSize by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val fieldSizeProperty = sourceElement.findProperty("labelSize") ?: return@lazy null
        fieldSizeProperty.valueAsIntOrNull()
    }

    /**
     * Type of this field represented as plain string
     *
     * TODO introduce enum with field types
     */
    val type by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val typeProperty = sourceElement.findProperty("type") ?: return@lazy null
        return@lazy typeProperty.valueAsStringOrNull()
    }

    /**
     * All rows that contain this field.
     *
     * Multiple containing rows can exist if this field is in included form
     */
    val containingRows by lazy(LazyThreadSafetyMode.PUBLICATION) {
        FormPsiUtils.firstParentsOfType(sourceElement, JsonObject::class).mapNotNull { FormRow.createFrom(it) }
    }

    /**
     * All groups that contain this field.
     *
     * Multiple containing groups can exist if this field is in included form
     */
    val containingGroups: List<FormGroup> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        containingRows.flatMap { it.containingGroups }
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
    val fieldNameChain by lazy(LazyThreadSafetyMode.PUBLICATION) {
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
    val propertyChain by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val stringPropertyChain = fieldNameChain
        if(stringPropertyChain.isEmpty()) {
            return@lazy emptyList()
        }

        val propertyChain = mutableListOf<FieldProperty>()
        var dataClasses: List<PsiClass> = findAllDataClassesFromRequests()

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

            val nextDataClass = psiTypeAsPsiClassOrNull(field.type)
            dataClasses = if(nextDataClass != null) listOf(nextDataClass) else emptyList()
        }

        return@lazy propertyChain.toList()
    }

    private fun findAllDataClassesFromRequests(): List<PsiClass> {
        val containingRootForm = FormRootFile.createFrom(containingFile)
        if(containingRootForm != null) {
            return containingRootForm.allDataClassesFromRequests
        }

        val containingIncludedForm = FormIncludedFile.createFrom(containingFile) ?: return emptyList()
        return containingIncludedForm.allRootForms.flatMap {
            it.allDataClassesFromRequests
        }
    }

    private fun findClassAndFieldByNameInClasses(psiClasses: List<PsiClass>, fieldName: String): Pair<PsiClass?, PsiField?> {
        psiClasses.forEach { psiClass ->
            psiClass.allFields
                .find { it.name == fieldName }
                ?.let { return psiClass to it }
        }
        return null to null
    }

    private fun psiTypeAsPsiClassOrNull(psiType: PsiType): PsiClass? {
        val classReturnType = psiType as? PsiClassType ?: return null
        return classReturnType.resolve()
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
        val applicableClasses: List<PsiClass>,
        val containingClass: PsiClass?,
        val referencedField: PsiField?,
        val symbol: FormSymbol?
    )

    companion object : FormArrayElementCreator<FormField>() {

        const val DEFAULT_LABEL_SIZE = 2

        override fun getArrayName() = "fields"

        override fun createUnsafeFrom(sourceElement: JsonObject) = FormField(sourceElement)

    }

}