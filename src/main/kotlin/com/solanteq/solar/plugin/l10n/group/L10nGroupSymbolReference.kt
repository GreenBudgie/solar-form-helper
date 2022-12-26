package com.solanteq.solar.plugin.l10n.group

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.psi.PsiCompletableReference
import com.solanteq.solar.plugin.l10n.FormL10nChain
import com.solanteq.solar.plugin.symbol.FormSymbolReference

class L10nGroupSymbolReference(
    val l10nChain: FormL10nChain
) : FormSymbolReference<JsonStringLiteral>(
    l10nChain.element,
    l10nChain.groupTextRange!!,
    l10nChain.groupReference
), PsiCompletableReference {

    override fun getCompletionVariants(): List<LookupElement> {
        val formElement = l10nChain.referencedFormTopLevelFileElement ?: return emptyList()
        val groupNames = formElement.allGroups.mapNotNull { it.name }
        return groupNames.map {
            LookupElementBuilder.create(it)
        }
    }

}