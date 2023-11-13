package com.solanteq.solar.plugin.ui.component

import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import com.solanteq.solar.plugin.element.FormGroupRow
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

class GroupRowContainerComponent(
    private val groupRows: List<FormGroupRow>
) : JBPanel<GroupRowContainerComponent>() {

    init {
        layout = GridBagLayout()
        val visibleGroupRows = groupRows
            .filterNot { it.isNeverVisible() }
            .filter { hasVisibleGroups(it) }
        visibleGroupRows.forEachIndexed { index, groupRow ->
            val groupRowConstrains = GridBagConstraints().apply {
                gridx = 0
                gridy = index
                weightx = 1.0
                weighty = 1.0
                fill = GridBagConstraints.HORIZONTAL
                anchor = GridBagConstraints.FIRST_LINE_START
                insets = if (index == 0) JBUI.emptyInsets() else JBUI.insetsTop(GroupContainerComponent.GROUP_INSET)
            }
            add(GroupRowComponent(groupRow), groupRowConstrains)
        }
    }

    private fun hasVisibleGroups(groupRow: FormGroupRow): Boolean {
        val groups = groupRow.groups ?: return false
        return groups.any { !it.isNeverVisible() }
    }

}