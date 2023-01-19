package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.Key
import com.intellij.psi.util.CachedValue
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.search.FormSearch
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.kotlin.idea.core.util.toPsiFile

/**
 * `JSON include` is a SOLAR platform specific feature that allows you to extract JSON objects and arrays
 * into separate files.
 *
 * JSON include can be placed in a property value or in an array.
 *
 * **Complaints!! :(**
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
class FormJsonInclude(
    sourceElement: JsonStringLiteral,
    val type: JsonIncludeType
) : FormElement<JsonStringLiteral>(sourceElement) {

    /**
     * Path to the form after "json://" ("json-flat://") prefix
     *
     * Example:
     * ```
     * "json-flat?://includes/forms/test/testForm.json"
     * -> includes/forms/test/testForm.json
     * ```
     */
    val path by lazy {
        sourceElement.value.substring(type.prefix.length)
    }

    /**
     * Path to the form without its name after "json://" ("json-flat://") prefix
     *
     * Example:
     * ```
     * "json-flat?://includes/forms/test/testForm.json"
     * -> includes/forms/test
     * ```
     */
    val pathWithoutFormName by lazy {
        val lastSeparatorIndex = path.lastIndexOf("/")
        if(lastSeparatorIndex < 2) return@lazy null
        return@lazy path.substring(0, lastSeparatorIndex)
    }

    val formNameWithExtension by lazy {
        val lastSeparatorIndex = path.lastIndexOf("/")
        if(lastSeparatorIndex == -1) return@lazy null
        return@lazy path.substring(lastSeparatorIndex + 1)
    }

    val referencedFormVirtualFile by lazy {
        val formName = formNameWithExtension ?: return@lazy null
        val includedForms = FormSearch.findIncludedForms(project.allScope())
        val applicableFormsByName = includedForms.filter {
            it.name == formName
        }
        val parentDirectoryChain = getParentDirectoryChain() ?: return@lazy null

        return@lazy applicableFormsByName.find { file ->
            var currentDirectory = file.parent
            parentDirectoryChain.forEach { directoryName ->
                if(!currentDirectory.isDirectory || currentDirectory.name != directoryName) {
                    return@find false
                }
                currentDirectory = currentDirectory.parent
            }
            return@find true
        }
    }

    val referencedFormPsiFile by lazy {
        referencedFormVirtualFile?.toPsiFile(project) as? JsonFile
    }

    val referencedForm by lazy {
        referencedFormPsiFile.toFormElement<FormIncludedFile>()
    }

    /**
     * A chain of parent directories (reversed) after "json://" ("json-flat://") prefix represented as an array
     *
     * Example:
     * ```
     * "json-flat?://includes/forms/test/testForm.json"
     * -> ["test", "forms", "includes"]
     * ```
     */
    private fun getParentDirectoryChain(): List<String>? {
        val pathWithoutFormName = pathWithoutFormName ?: return null
        return pathWithoutFormName.split("/").reversed()
    }

    enum class JsonIncludeType(
        val prefix: String,
        val isFlat: Boolean,
        val isOptional: Boolean
    ) {

        JSON("json://includes/forms/", false, false),
        JSON_OPTIONAL("json?://includes/forms/", false , true),
        JSON_FLAT("json-flat://includes/forms/", true, false),
        JSON_FLAT_OPTIONAL("json-flat?://includes/forms/", true, true)

    }

    companion object : FormElementCreator<FormJsonInclude> {

        override val key = Key<CachedValue<FormJsonInclude>>("solar.element.jsonInclude")

        override fun create(sourceElement: JsonElement): FormJsonInclude? {
            val stringLiteral = sourceElement as? JsonStringLiteral ?: return null
            val includeType = getJsonIncludeDeclarationType(stringLiteral) ?: return null
            return FormJsonInclude(stringLiteral, includeType)
        }

        /**
         * Whether the given element can be considered a JSON include declaration.
         * Checks if its value starts with `prefix://includes/forms`.
         */
        fun isJsonIncludeDeclaration(element: JsonStringLiteral) =
            getJsonIncludeDeclarationType(element) != null

        private fun getJsonIncludeDeclarationType(element: JsonStringLiteral): JsonIncludeType? {
            val value = element.value
            return JsonIncludeType.values().find {
                value.startsWith(it.prefix)
            }
        }

    }

}