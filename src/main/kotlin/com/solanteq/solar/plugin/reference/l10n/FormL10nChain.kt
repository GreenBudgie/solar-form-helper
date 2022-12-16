package com.solanteq.solar.plugin.reference.l10n

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.solanteq.solar.plugin.element.FormTopLevelFile
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.element.toFormElement
import com.solanteq.solar.plugin.search.FormSearch
import org.jetbrains.kotlin.idea.base.util.projectScope
import org.jetbrains.kotlin.idea.core.util.toPsiFile

/**
 * Represents a localization string in a l10n file for top-level form.
 *
 * L10n string property is considered a form localization if it starts with `<module_name>.form.<form_name>`.
 * This property is broken into separate "tokens", forming a chain of tokens.
 * Each chain token, starting after `form` token, will try to resolve to [FormLocalizableElement.sourceElement].
 *
 * In this example, we have three l10n chains, each corresponding to the specific top-level form element.
 * ```
 * {
 *   "test.form.testForm": "Form l10n!",
 *   "test.form.testForm.details": "Group l10n!",
 *   "test.form.testForm.details.name": "Field l10n!"
 * }
 * ```
 * Notice the number of references in this l10n file:
 * - 3 references to `test.testForm` form file
 * - 2 references to `details` group in `test.testForm` file
 * - 1 reference to `name` field in `details` group
 *
 * TODO for now, only supports form -> group -> field l10ns
 */
class FormL10nChain(
    val element: JsonStringLiteral,
    val module: String,
    private val chain: List<String>,
    private val textRangeStartIndex: Int
) {

    private val project = element.project

    val referencedFormVirtualFile by lazy {
        if(chain.isEmpty()) return@lazy null
        return@lazy FormSearch.findFormByModuleAndName(
            module,
            chain[formNameChainIndex],
            project.projectScope()
        )
    }

    val referencedFormPsiFile by lazy {
        referencedFormVirtualFile?.toPsiFile(project) as? JsonFile
    }

    val referencedFormTopLevelFileElement by lazy {
        referencedFormPsiFile.toFormElement<FormTopLevelFile>()
    }

    val formReference by lazy {
        return@lazy referencedFormPsiFile
    }

    val formNameTextRange by lazy {
        getTextRangeForChainEntryByIndex(formNameChainIndex)
    }

    val groupNameReference by lazy {
        val groups = referencedFormTopLevelFileElement?.allGroups ?: return@lazy null
        val groupName = chain[groupNameChainIndex]
        val groupElement = groups.find { it.name == groupName } ?: return@lazy null
        return@lazy groupElement.namePropertyValue
    }

    val groupNameTextRange by lazy {
        getTextRangeForChainEntryByIndex(groupNameChainIndex)
    }

    private fun getTextRangeForChainEntryByIndex(index: Int): TextRange? {
        if(index >= chain.size) return null
        val currentLiteralLength = chain[index].length
        val prevLiteralsLength = chain.take(index).joinToString().length
        val startPos = textRangeStartIndex + prevLiteralsLength + index + 1
        val endPos = startPos + currentLiteralLength
        return TextRange(startPos, endPos)
    }

    companion object {

        /**
         * Creates new l10n chain for the given element if it's possible, or returns null
         */
        fun fromElement(element: JsonStringLiteral): FormL10nChain? {
            val textSplit = element.value.split(".")
            if(textSplit.size < 3) return null
            val l10nType = textSplit[1]
            if(l10nType != "form") return null
            val l10nModule = textSplit[0]
            val l10nChain = textSplit.drop(2)
            return FormL10nChain(element, l10nModule, l10nChain, l10nModule.length + 6)
        }

        private val formNameChainIndex = 0
        private val groupNameChainIndex = 1
        private val firstFieldNameChainIndex = 2

    }

}