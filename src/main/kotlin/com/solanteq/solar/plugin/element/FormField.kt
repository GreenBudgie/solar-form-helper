package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.PsiType
import com.intellij.psi.util.TypeConversionUtil
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.element.base.FormNamedObjectElement
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
) : FormNamedObjectElement(sourceElement), FormLocalizableElement {

    /**
     * A list of properties as a chain from main to nested ones represented as raw strings.
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
    val stringPropertyChain by lazy { name?.split(".") }

    /**
     * A list of [FieldProperty] as a chain from main to nested ones represented as UAST fields.
     * Works similar to [stringPropertyChain].
     *
     * If any nested property is not resolved, every property to the right won't be resolved too
     * and the returned chain will only contain references to resolved properties.
     *
     * Example: consider we have a chain of five fields `field1`, `field2`...
     * If we make a typo at `field3`, then only `field1` and `field2` will be resolved.
     *
     * ```
     * "name": "field1.field2.fieldWithTypo.field4.field5"
     * -> [field1, field2, null, null, null]
     * ```
     */
    val propertyChain by lazy {
        val stringPropertyChain = stringPropertyChain ?: return@lazy emptyList()
        if(stringPropertyChain.isEmpty()) {
            return@lazy emptyList()
        }

        val propertyChain = mutableListOf<FieldProperty>()
        var currentDataClass = dataClass

        stringPropertyChain.forEach { fieldName ->
            if(currentDataClass == null) {
                propertyChain += FieldProperty(fieldName, null, null)
                return@forEach
            }

            val field = findFieldByNameInClass(currentDataClass!!, fieldName)
            if(field == null) {
                propertyChain += FieldProperty(fieldName, currentDataClass, null)
                return@forEach
            }

            propertyChain += FieldProperty(fieldName, currentDataClass, field)

            currentDataClass = psiTypeAsUClassOrNull(field.type)
        }

        return@lazy propertyChain.toList()
    }

    /**
     * Data class from source request that this field uses
     */
    val dataClass by lazy {
        val sourceRequest = sourceRequest ?: return@lazy null
        val method = sourceRequest.methodFromRequest ?: return@lazy null
        val derivedClass = sourceRequest.serviceFromRequest?.javaPsi ?: return@lazy null
        val superClass = method.containingClass ?: return@lazy null
        val rawReturnType = method.returnType ?: return@lazy null
        return@lazy substitutePsiType(
            superClass,
            derivedClass,
            rawReturnType
        )
    }

    private val sourceRequest by lazy {
        val jsonFile = sourceElement.containingFile as? JsonFile ?: return@lazy null
        val formFile = jsonFile.toFormElement<FormFile>() ?: return@lazy null
        return@lazy formFile.sourceRequest
    }

    private fun findFieldByNameInClass(uClass: UClass, fieldName: String): UField? =
        uClass.javaPsi.allFields.find { it.name == fieldName }.toUElementOfType()

    private fun substitutePsiType(superClass: PsiClass, derivedClass: PsiClass, psiType: PsiType): UClass? {
        val substitutedReturnType = TypeConversionUtil.getClassSubstitutor(
            superClass,
            derivedClass,
            PsiSubstitutor.EMPTY
        )?.substitute(psiType)
        val classReturnType = substitutedReturnType as? PsiClassType ?: return null
        return classReturnType.resolve().toUElementOfType()
    }

    private fun psiTypeAsUClassOrNull(psiType: PsiType): UClass? {
        val classReturnType = psiType as? PsiClassType ?: return null
        return classReturnType.resolve().toUElementOfType()
    }

    /**
     * @property name represents a name of this property.
     * Never null, but might be referencing to non-existing field.
     * @property containingClass Data class containing this field
     * @property referencedField Real psi element resolved from [name] property
     */
    data class FieldProperty(
        val name: String,
        val containingClass: UClass?,
        val referencedField: UField?
    )

    companion object {

        val ARRAY_NAME = "fields"

    }

}