package com.solanteq.solar.plugin.l10n.search

import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.index.l10n.L10N_INDEX_NAME
import com.solanteq.solar.plugin.l10n.L10n

/**
 * Utility object that helps to find the necessary plain [L10n]s
 */
object L10nSearch : L10nSearchBase<L10n>(L10N_INDEX_NAME) {

    override fun createL10n(property: JsonProperty) = L10n.fromElement(property)

}