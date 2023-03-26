package com.solanteq.solar.plugin.l10n.search

import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.index.FormL10nIndex
import com.solanteq.solar.plugin.l10n.FormL10n

/**
 * Utility object that helps to find the necessary [FormL10n]s
 */
object FormL10nSearch : L10nSearchBase<FormL10n>(FormL10nIndex.NAME) {

    override fun createL10n(property: JsonProperty) = FormL10n.fromElement(property)

}