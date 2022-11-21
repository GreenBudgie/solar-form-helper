package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import com.solanteq.solar.plugin.util.valueAsString
import org.jetbrains.kotlin.asJava.namedUnwrappedElement
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UField
import org.jetbrains.uast.toUElementOfType

/**
 * Represents a field on a form.
 *
 * Each form field contains a reference to corresponding data class field (property).
 * For simplicity, we will call fields in data classes "properties".
 * Any data class may have another data classes as properties,
 * they can have their own data classes as properties, and so on.
 * This structure will form a chain of properties, and the final one must be a primitive type:
 * ```
 * "client.type.id"
 * ```
 *
 * Fields example:
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
 *
 * TODO: for now, only fields in forms with "source" requests are supported
 */
class FormField(
    sourceElement: JsonObject
) : FormObjectInArrayElement(sourceElement) {

    /**
     * Full name of this field.
     *
     * Example:
     * ```
     * "fields": [
     *   {
     *     "name": "property.nestedProperty",
     *     "fieldSize": 4,
     *     "type": "STRING"
     *   }
     * ]
     * ```
     *
     * This will return "property.nestedProperty"
     */
    val name by lazy { sourceElement.findProperty("name").valueAsString() }

    /**
     * A list of properties as a chain from main to nested ones represented as raw strings.
     *
     * Example:
     * ```
     * "fields": [
     *   {
     *     "name": "property.nestedProperty.nextNestedProperty",
     *     "fieldSize": 4,
     *     "type": "STRING"
     *   }
     * ]
     * ```
     *
     * This will return: [
     *   "property",
     *   "nestedProperty",
     *   "nextNestedProperty"
     * ]
     *
     */
    val stringPropertyChain by lazy { name?.split(".") }

    /**
     * A list of properties as a chain from main to nested ones represented as UAST fields.
     * Works similar to [stringPropertyChain], but returns real data class fields.
     *
     * If any nested property is not resolved, every property to the right won't be resolved too
     * and the returned chain will only contain references to resolved properties.
     *
     * Example: consider we have a chain of five fields `field1`, `field2`...
     * If we make a typo at `field3`, then only `field1` and `field2` will be resolved.
     *
     * ```
     * "name": "field1.field2.fieldWithTypo.field4.field5"
     * -> [field1, field2]
     * ```
     */
    val propertyChain: List<UField> by lazy {
        val stringPropertyChain = stringPropertyChain ?: return@lazy emptyList()
        if(stringPropertyChain.isEmpty()) {
            return@lazy emptyList()
        }

        val dataClass = dataClass ?: return@lazy emptyList()

        val propertyChain = mutableListOf<UField>()

        var currentDataClass = dataClass

        stringPropertyChain.forEach { fieldName ->
            val field = findFieldByNameInClass(currentDataClass, fieldName) ?: return@lazy propertyChain.toList()
            propertyChain += field
            currentDataClass = psiTypeAsUClassOrNull(field.type) ?: return@lazy propertyChain.toList()
        }

        return@lazy propertyChain.toList()
    }

    /**
     * Data class from source request that this field uses
     */
    val dataClass by lazy {
        val sourceRequest = sourceRequest ?: return@lazy null
        val rawReturnType = sourceRequest.methodFromRequest?.returnType ?: return@lazy null
        return@lazy psiTypeAsUClassOrNull(rawReturnType)
    }

    private val sourceRequest by lazy {
        val jsonFile = sourceElement.containingFile as? JsonFile ?: return@lazy null
        val formFile = jsonFile.toFormElement<FormFile>() ?: return@lazy null
        return@lazy formFile.sourceRequest
    }

    private fun findFieldByNameInClass(uClass: UClass, fieldName: String) =
        uClass.fields.find { it.namedUnwrappedElement?.name == fieldName }

    private fun psiTypeAsUClassOrNull(psiType: PsiType): UClass? {
        val classReturnType = psiType as? PsiClassType ?: return null
        return classReturnType.resolve().toUElementOfType()
    }

}