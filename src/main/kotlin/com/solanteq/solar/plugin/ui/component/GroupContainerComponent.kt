package com.solanteq.solar.plugin.ui.component

import com.solanteq.solar.plugin.element.FormGroup
import java.awt.GridLayout
import javax.swing.JPanel

class GroupContainerComponent(
    private val groups: List<FormGroup>
) : JPanel() {

    init {
        layout = GridLayout(groups.size, 1)
        val visibleGroups = groups.filterNot { it.isNeverVisible() }
        visibleGroups.forEach {
            add(GroupComponent(it, false))
        }
    }

}