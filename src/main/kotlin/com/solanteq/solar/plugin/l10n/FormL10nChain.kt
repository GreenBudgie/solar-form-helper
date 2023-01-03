package com.solanteq.solar.plugin.l10n

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.solanteq.solar.plugin.element.FormTopLevelFile
import com.solanteq.solar.plugin.element.toFormElement
import com.solanteq.solar.plugin.search.FormSearch
import com.solanteq.solar.plugin.util.dotSplit
import com.solanteq.solar.plugin.util.getFormModuleDirectory
import org.jetbrains.kotlin.idea.base.util.projectScope
import org.jetbrains.kotlin.idea.core.util.toPsiDirectory
import org.jetbrains.kotlin.idea.core.util.toPsiFile

/**
 * Represents a localization string in a l10n file for top-level form.
 *
 * L10n string property is considered a form localization if it starts with `<module_name>.form.<form_name>`.
 * This property is broken into "tokens", forming a chain of tokens.
 *
 * In this example, in this file we have three l10n chains, each corresponding to the specific form element.
 * ```
 * {
 *   "test.form.testForm": "Form l10n",
 *   "test.form.testForm.details": "Group l10n",
 *   "test.form.testForm.details.name": "Field l10n"
 * }
 * ```
 * Notice the number of references in this l10n file:
 * - 3 references to `test` module directory
 * - 3 references to `test.testForm` form file
 * - 2 references to `details` group in `test.testForm` file
 * - 1 reference to `name` field in `details` group
 *
 * TODO for now, only supports form -> group -> field l10ns
 */
class FormL10nChain private constructor(
    val element: JsonStringLiteral,
    val chain: List<Pair<TextRange, String>>,
) {

    val project = element.project

    val moduleName = chain.getOrNull(moduleNameChainIndex)?.second
    val formName = chain.getOrNull(formNameChainIndex)?.second
    val groupName = chain.getOrNull(groupNameChainIndex)?.second

    val moduleTextRange by lazy {
        chain.getOrNull(moduleNameChainIndex)?.first
    }

    val referencedModuleDirectory by lazy {
        val directory = referencedFormVirtualFile?.getFormModuleDirectory() ?: return@lazy null
        if(moduleName != directory.name) {
            return@lazy null
        }
        return@lazy directory
    }

    val referencedModulePsiDirectory by lazy {
        referencedModuleDirectory?.toPsiDirectory(project)
    }

    val referencedFormVirtualFile by lazy {
        if(moduleName == null || formName == null) {
            return@lazy null
        }
        return@lazy FormSearch.findFormByModuleAndName(
            moduleName,
            formName,
            project.projectScope()
        )
    }

    val referencedFormPsiFile by lazy {
        referencedFormVirtualFile?.toPsiFile(project) as? JsonFile
    }

    val referencedFormTopLevelFileElement by lazy {
        referencedFormPsiFile.toFormElement<FormTopLevelFile>()
    }

    val formTextRange by lazy {
        chain.getOrNull(formNameChainIndex)?.first
    }

    val referencedGroup by lazy {
        referencedGroupElement?.namePropertyValue
    }

    val referencedGroupElement by lazy {
        groupName ?: return@lazy null
        val groups = referencedFormTopLevelFileElement?.allGroups ?: return@lazy null
        return@lazy groups.find { it.name == groupName }
    }

    val groupTextRange by lazy {
        chain.getOrNull(groupNameChainIndex)?.first
    }

    val fieldChain by lazy {
        val fieldChainIndexRange = fieldChainStartIndex until chain.size
        return@lazy fieldChainIndexRange.map { chain[it] }
    }

    private val fieldsInForm by lazy {
        val form = referencedFormTopLevelFileElement ?: return@lazy emptyList()
        val groups = form.allGroups
        val rows = groups.flatMap { it.rows ?: emptyList() }
        return@lazy rows.flatMap { it.fields ?: emptyList() }
    }

    companion object {

        private const val moduleNameChainIndex = 0
        private const val formNameChainIndex = 2
        private const val groupNameChainIndex = 3
        private const val fieldChainStartIndex = 4

        private val key = Key<CachedValue<FormL10nChain>>("solar.l10n.chain")

        /**
         * Whether this element can be considered a form localization.
         * Note that the form may not be resolved, but it will still be considered a form l10n.
         */
        fun isFormL10n(element: JsonStringLiteral): Boolean {
            val textSplit = element.value.split(".")
            val l10nType = textSplit.getOrNull(1)
            return l10nType == "form"
        }

        /**
         * Creates new l10n chain for the given element if it's possible, or returns null
         */
        fun fromElement(element: JsonStringLiteral): FormL10nChain? {
            return CachedValuesManager.getCachedValue(element, key) {
                CachedValueProvider.Result(
                    createChain(element),
                    PsiModificationTracker.MODIFICATION_COUNT
                )
            }
        }

        private fun createChain(element: JsonStringLiteral): FormL10nChain? {
            val textSplit = element.dotSplit()
            if(textSplit.size < 3) return null
            val l10nType = textSplit[1].second
            if(l10nType != "form") return null
            return FormL10nChain(element, textSplit)
        }

    }

}