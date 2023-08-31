package com.solanteq.solar.plugin.ui.component

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.solanteq.solar.plugin.element.FormGroupRow
import com.solanteq.solar.plugin.ui.FormUIConstants
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

class GroupRowComponent(
    private val groupRow: FormGroupRow
) : JBPanel<GroupRowComponent>() {

    init {
        layout = GridBagLayout()
        background = JBColor.MAGENTA
        var prevGridX = 0
        groupRow.groups?.forEach { group ->
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