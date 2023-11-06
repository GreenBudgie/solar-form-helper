package com.solanteq.solar.plugin.ui.component

import com.solanteq.solar.plugin.element.FormGroupRow
import com.solanteq.solar.plugin.ui.FormUIConstants
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.BorderFactory
import javax.swing.JPanel

class GroupRowComponent(
    private val formElement: FormGroupRow
) : JPanel() {

    init {
        border = BorderFactory.createLineBorder(FormUIConstants.BORDER_COLOR)
        layout = GridBagLayout()
        var prevGridX = 0
        formElement.groups?.filterNot { it.isNeverVisible() }?.forEach { group ->
            val groupSize = group.size ?: FormUIConstants.COLUMNS
            val constraints = GridBagConstraints().apply {
                gridx = prevGridX
                gridwidth = 1
                fill = GridBagConstraints.BOTH
                weightx = groupSize / FormUIConstants.COLUMNS.toDouble()
                weighty = 1.0
                anchor = GridBagConstraints.NORTHWEST
                prevGridX += 1
            }
            add(GroupComponent(group, true), constraints)
        }
    }

    companion object {

        private const val DEFAULT_HEIGHT = 200

    }

}