package com.solanteq.solar.plugin.l10n.field

import com.solanteq.solar.plugin.element.FormField

object L10nFieldSearcher {

    fun findApplicablePropertiesByNameChainAtIndex(
        fields: List<FormField>,
        fieldNameChain: List<String>,
        index: Int,
        excludeLastIndexFromSearch: Boolean = false
    ): List<FormField.FieldProperty> {
        val chainSize = if(excludeLastIndexFromSearch) {
            index
        } else {
            index + 1
        }
        val l10nFieldNameSubChain = fieldNameChain.take(chainSize)
        val applicableFields = fields.filter { field ->
            val fieldNameSubChain = field.stringPropertyChain.take(chainSize).map { it.second }
            l10nFieldNameSubChain == fieldNameSubChain
        }
        return applicableFields.mapNotNull { it.propertyChain.getOrNull(index) }
    }

}