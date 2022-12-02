package com.solanteq.solar.plugin.reference.l10n

import com.intellij.json.psi.JsonStringLiteral

/**
 * TODO
 */
class FormL10nChain(
    element: JsonStringLiteral,
    module: String,
    chain: List<String>
) {

    

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
            return FormL10nChain(element, l10nModule, l10nChain)
        }

    }

}