package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.search.FormSearch
import com.solanteq.solar.plugin.util.RangeSplit
import com.solanteq.solar.plugin.util.convert
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
     * Path after "json://includes/forms/" ("json-flat://includes/forms/") prefix
     *
     * Examples:
     * ```
     * "json-flat?://includes/forms/test/testForm.json"
     * -> "test/testForm.json"
     * ```
     * ```
     * "json://includes/forms/dir1/dir2/"
     * -> "dir1/dir2/"
     * ```
     * ```
     * "json://includes/forms/"
     * -> ""
     * ```
     */
    val path by lazy {
        sourceElement.value.substring(type.prefix.length)
    }

    /**
     * A chain of [RangeSplit]s containing text ranges and corresponding directory/form names.
     * Uses real text ranges in string literal.
     */
    val pathChain by lazy {
        RangeSplit.from(path, '/').shiftedRight(type.prefix.length + 1)
    }

    private val reversedPathChain by lazy {
        pathChain.reversed().convert()
    }

    /**
     * Form name with `.json` extension, or null if this declaration is incomplete and
     * no form is present for now
     */
    val formName by lazy {
        if(!path.endsWith(".json")) return@lazy null
        val lastSeparatorIndex = path.lastIndexOf("/")
        if(lastSeparatorIndex == -1) return@lazy path
        return@lazy path.substring(lastSeparatorIndex + 1)
    }

    /**
     * A file that this element points to.
     * Can be null if this declaration is incomplete, invalid, or points to non-existent file.
     */
    val referencedFormVirtualFile by lazy {
        val formName = formName ?: return@lazy null
        val includedForms = FormSearch.findIncludedForms(project.allScope())
        val applicableFormsByName = includedForms.filter {
            it.name == formName
        }

        return@lazy applicableFormsByName.find { file ->
            var currentVirtualFile = file
            reversedPathChain.forEach {
                if(currentVirtualFile.name != it.text) {
                    return@find false
                }
                currentVirtualFile = currentVirtualFile.parent
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
     * A chain of [VirtualFile]s representing the **reversed** path to the
     * form or last directory in chain.
     *
     * Starts traversing from [referencedFormVirtualFile]. If it is null, then every
     * [VirtualFile] in the chain will be null.
     */
    val virtualFileChain: List<Pair<TextRange, VirtualFile?>> by lazy {
        val resultChain = mutableListOf<Pair<TextRange, VirtualFile?>>()
        var currentVirtualFile = referencedFormVirtualFile

        reversedPathChain.forEach {
            resultChain.add(it.range to currentVirtualFile)
            currentVirtualFile = currentVirtualFile?.parent
        }

        return@lazy resultChain
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