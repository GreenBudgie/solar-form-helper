package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.solanteq.solar.plugin.element.base.AbstractFormElement
import com.solanteq.solar.plugin.element.creator.FormElementCreator
import com.solanteq.solar.plugin.search.FormSearch
import com.solanteq.solar.plugin.util.RangeSplit
import com.solanteq.solar.plugin.util.convert
import com.solanteq.solar.plugin.util.firstWithProjectPrioritization
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.kotlin.idea.core.util.toPsiFile

/**
 * `JSON include` is a SOLAR platform specific feature that allows you to extract JSON objects and arrays
 * into separate files.
 *
 * JSON include can be placed in a property value or in an array.
 */
class FormJsonInclude(
    sourceElement: JsonStringLiteral,
    val type: JsonIncludeType
) : AbstractFormElement<JsonStringLiteral>(sourceElement) {

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
    val path by lazy(LazyThreadSafetyMode.PUBLICATION) {
        sourceElement.value.substring(type.prefix.length)
    }

    /**
     * A chain of [RangeSplit]s containing text ranges and corresponding directory/form names.
     * Uses real text ranges in string literal.
     */
    val pathChain by lazy(LazyThreadSafetyMode.PUBLICATION) {
        RangeSplit.from(path, '/').shiftedRight(type.prefix.length + 1)
    }

    private val reversedPathChain by lazy(LazyThreadSafetyMode.PUBLICATION) {
        pathChain.reversed().convert()
    }

    /**
     * Form name with `.json` extension, or null if this declaration is incomplete and
     * no form is present for now
     */
    val formName by lazy(LazyThreadSafetyMode.PUBLICATION) {
        if(!path.endsWith(".json")) return@lazy null
        val lastSeparatorIndex = path.lastIndexOf("/")
        if(lastSeparatorIndex == -1) return@lazy path
        return@lazy path.substring(lastSeparatorIndex + 1)
    }

    /**
     * A file that this element points to.
     * Can be null if this declaration is incomplete, invalid, or points to non-existent file.
     */
    val referencedFormVirtualFile by lazy(LazyThreadSafetyMode.PUBLICATION) {
        FormSearch.findIncludedFormsByRelativePath(path, project.allScope()).firstWithProjectPrioritization(project)
    }

    val referencedFormPsiFile by lazy(LazyThreadSafetyMode.PUBLICATION) {
        referencedFormVirtualFile?.toPsiFile(project) as? JsonFile
    }

    val referencedForm by lazy(LazyThreadSafetyMode.PUBLICATION) {
       FormIncludedFile.createFrom(referencedFormPsiFile)
    }

    /**
     * A chain of [VirtualFile]s representing the **reversed** path to the
     * form or last directory in chain.
     *
     * Starts traversing from [referencedFormVirtualFile]. If it is null, then every
     * [VirtualFile] in the chain will be null.
     */
    val virtualFileChain: List<Pair<TextRange, VirtualFile?>> by lazy(LazyThreadSafetyMode.PUBLICATION) {
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

    companion object : FormElementCreator<FormJsonInclude, JsonStringLiteral>() {

        override fun doCreate(sourceElement: JsonStringLiteral): FormJsonInclude? {
            val includeType = getJsonIncludeDeclarationType(sourceElement) ?: return null
            return FormJsonInclude(sourceElement, includeType)
        }

        /**
         * Whether the given element can be considered a JSON include declaration.
         * Checks if its value starts with `prefix://includes/forms`.
         */
        fun isJsonIncludeDeclaration(element: JsonStringLiteral) =
            getJsonIncludeDeclarationType(element) != null

        private fun getJsonIncludeDeclarationType(element: JsonStringLiteral): JsonIncludeType? {
            val value = element.value
            return JsonIncludeType.entries.find {
                value.startsWith(it.prefix)
            }
        }

    }

}