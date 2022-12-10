package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.vfs.VirtualFile
import com.solanteq.solar.plugin.element.base.FormElement

/**
 * `JSON include` is a SOLAR platform specific feature that allows you to extract JSON objects and arrays
 * into separate files.
 *
 * JSON include can be placed in a property value or in an array.
 *
 * *Complaints!! :( *
 *
 * This feature makes plugin development significantly more complicated as any piece of JSON can be extracted
 * into separate file, which can also be separated into many other JSON files, and so on.
 *
 * It is a **pain** to traverse JSON include files from bottom to top as every file can have multiple usages.
 *
 * Finally, we have optional JSON includes that may not even be in the project or its libraries.
 * No way to resolve them!
 *
 * This plugin will probably struggle with JSON includes for a long time :(
 */
interface FormJsonInclude: FormElement<JsonStringLiteral> {

    val type: JsonIncludeType

    /**
     * Path to the form after "json://" ("json-flat://") prefix
     *
     * Example:
     * ```
     * "json-flat?://includes/forms/test/testForm.json"
     * -> includes/forms/test/testForm.json
     * ```
     */
    val path: String?

    /**
     * Path to the form without its name after "json://" ("json-flat://") prefix
     *
     * Example:
     * ```
     * "json-flat?://includes/forms/test/testForm.json"
     * -> includes/forms/test
     * ```
     */
    val pathWithoutFormName: String?

    val formNameWithExtension: String?

    val referencedFormFile: VirtualFile?

    enum class JsonIncludeType(
        val prefix: String,
        val isOptional: Boolean
    ) {

        JSON("json://", false),
        JSON_OPTIONAL("json?://", true),
        JSON_FLAT("json-flat://", false),
        JSON_FLAT_OPTIONAL("json-flat?://", true)

    }

}