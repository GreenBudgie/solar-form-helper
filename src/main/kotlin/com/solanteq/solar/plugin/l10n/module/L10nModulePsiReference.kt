package com.solanteq.solar.plugin.l10n.module

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiReferenceBase
import com.solanteq.solar.plugin.l10n.FormL10nChain
import com.solanteq.solar.plugin.search.FormModuleSearch
import com.solanteq.solar.plugin.util.asArray
import org.jetbrains.kotlin.idea.core.util.toPsiDirectory

class L10nModulePsiReference(
    val l10nChain: FormL10nChain
) : PsiReferenceBase<JsonStringLiteral>(l10nChain.element, l10nChain.moduleTextRange, false) {

    override fun getVariants(): Array<Any> {
        val allModules = FormModuleSearch.findTopLevelFormModules(l10nChain.project)
        val distinctModulesByName = allModules.distinctBy { it.name }
        val psiDirectories = distinctModulesByName.mapNotNull { it.toPsiDirectory(l10nChain.project) }
        return psiDirectories.map {
            LookupElementBuilder
                .create(it.name)
                .withIcon(it.getIcon(0))
        }.asArray()
    }

    override fun resolve() = l10nChain.referencedModulePsiDirectory

}