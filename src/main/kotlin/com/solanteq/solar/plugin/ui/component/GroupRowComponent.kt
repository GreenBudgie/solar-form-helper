package com.solanteq.solar.plugin.ui.component

import com.intellij.util.ui.JBUI
import com.solanteq.solar.plugin.element.FormGroupRow
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.Box
import javax.swing.JPanel

class GroupRowComponent(
    private val groupRow: FormGroupRow
) : JPanel() {

    init {
        layout = GridBagLayout()
        var size = 0
        val visibleGroups = groupRow.groups?.filterNot { it.isNeverVisible() } ?: emptyList()
        visibleGroups.forEachIndexed { index, group ->
            val groupSize = group.size ?: GROUP_COLUMNS
            val constraints = GridBagConstraints().apply {
                gridx = index
                gridy = 0
                fill = GridBagConstraints.HORIZONTAL
                weightx = groupSize / GROUP_COLUMNS.toDouble()
                anchor = GridBagConstraints.FIRST_LINE_START
                insets = if (index == 0) JBUI.emptyInsets() else JBUI.insetsLeft(GROUP_INSET)
            }
            add(GroupComponent(group), constraints)
            size += groupSize
        }
        if(size < GROUP_COLUMNS) {
            val strutConstraint = GridBagConstraints().apply {
                weightx = (GROUP_COLUMNS - size) / GROUP_COLUMNS.toDouble()
                gridx = visibleGroups.size
                gridy = 0
                fill = GridBagConstraints.HORIZONTAL
            }
            val strut = Box.createHorizontalStrut(0)
            add(strut, strutConstraint)
        }
    }

    companion object {

        const val GROUP_COLUMNS = 24
        const val GROUP_INSET = 20

    }

}