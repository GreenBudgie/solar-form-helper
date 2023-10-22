package com.solanteq.solar.plugin.l10n.group

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.model.psi.PsiCompletableReference
import com.solanteq.solar.plugin.l10n.FormL10n
import com.solanteq.solar.plugin.symbol.FormSymbolSingleReference
import com.solanteq.solar.plugin.symbol.FormSymbolType

class L10nGroupSymbolReference(
    private val l10nChain: FormL10n
) : FormSymbolSingleReference(
    l10nChain.keyElement,
    l10nChain.groupTextRange!!,
    FormSymbolType.GROUP,
    // TODO CAN_BE_OPTIMIZED seems like it degrades performance, it's better to move it to resolve method
    l10nChain.referencedGroup
), PsiCompletableReference {

    override fun getCompletionVariants(): List<LookupElement> {
        val formElement = l10nChain.referencedFormFileElement ?: return emptyList()
        val groupNames = formElement.allGroups.mapNotNull { it.name }
        return groupNames.map {
            LookupElementBuilder.create(it)
        }
    }

}