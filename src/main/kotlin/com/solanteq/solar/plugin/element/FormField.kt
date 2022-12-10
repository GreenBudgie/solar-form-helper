package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UField

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
 *
 * TODO: for now, only fields in forms with "source" requests are supported
 */
interface FormField : FormLocalizableElement<JsonObject> {

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
    val stringPropertyChain: List<String>?

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
    val propertyChain: List<FieldProperty>

    /**
     * Data class from source request that this field uses
     */
    val dataClass: UClass?

    val sourceRequest: FormRequest?

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

        const val ARRAY_NAME = "fields"

    }

}