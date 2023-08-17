package com.solanteq.solar.plugin.l10n.search

import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.index.l10n.FORM_L10N_INDEX_NAME
import com.solanteq.solar.plugin.l10n.FormL10n

/**
 * Utility object that helps to find the necessary [FormL10n]s
 */
object FormL10nSearch : L10nSearchBase<FormL10n>(FORM_L10N_INDEX_NAME) {

    override fun createL10n(property: JsonProperty) = FormL10n.fromElement(property)

}