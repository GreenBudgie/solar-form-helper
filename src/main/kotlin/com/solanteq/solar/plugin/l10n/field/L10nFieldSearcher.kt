package com.solanteq.solar.plugin.l10n.field

import com.solanteq.solar.plugin.element.FormField
import com.solanteq.solar.plugin.util.convert

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
            val fieldNameSubChain = field.fieldNameChain.take(chainSize).convert().strings
            l10nFieldNameSubChain == fieldNameSubChain
        }
        return applicableFields.mapNotNull { it.propertyChain.getOrNull(index) }
    }

}