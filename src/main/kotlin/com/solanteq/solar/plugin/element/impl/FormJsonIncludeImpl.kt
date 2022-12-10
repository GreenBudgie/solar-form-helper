package com.solanteq.solar.plugin.element.impl

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonStringLiteral
import com.solanteq.solar.plugin.element.FormJsonInclude
import com.solanteq.solar.plugin.element.base.impl.FormElementImpl
import com.solanteq.solar.plugin.util.findIncludedForms

class FormJsonIncludeImpl(
    sourceElement: JsonStringLiteral,
    override val type: FormJsonInclude.JsonIncludeType
) : FormElementImpl<JsonStringLiteral>(sourceElement), FormJsonInclude {

    override val path by lazy {
        sourceElement.value.substring(type.prefix.length)
    }

    override val pathWithoutFormName by lazy {
        val lastSeparatorIndex = path.lastIndexOf("/")
        if(lastSeparatorIndex < 2) return@lazy null
        return@lazy path.substring(0, lastSeparatorIndex)
    }


    override val formNameWithExtension by lazy {
        val lastSeparatorIndex = path.lastIndexOf("/")
        if(lastSeparatorIndex == -1) return@lazy null
        return@lazy path.substring(lastSeparatorIndex + 1)
    }

    override val referencedFormFile by lazy {
        val formName = formNameWithExtension ?: return@lazy null
        val includedForms = findIncludedForms(sourceElement.project)
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

    companion object {

        fun create(sourceElement: JsonElement): FormJsonIncludeImpl? {
            val stringLiteral = sourceElement as? JsonStringLiteral ?: return null
            val stringLiteralValue = stringLiteral.value
            val includeType = FormJsonInclude.JsonIncludeType.values().find {
                stringLiteralValue.startsWith(it.prefix)
            } ?: return null
            return FormJsonIncludeImpl(stringLiteral, includeType)
        }

    }

}