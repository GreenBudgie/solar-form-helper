package com.solanteq.solar.plugin.ui.component.form

import com.intellij.util.ui.JBUI
import com.solanteq.solar.plugin.element.FormGroup
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JPanel

class GroupContainerComponent(
    private val groups: List<FormGroup>
) : JPanel() {

    init {
        layout = GridBagLayout()
        val visibleGroups = groups.filterNot { it.isNeverVisible() }
        visibleGroups.forEachIndexed { index, group ->
            val groupConstrains = GridBagConstraints().apply {
                gridx = 0
                gridy = index
                weightx = 1.0
                weighty = 1.0
                fill = GridBagConstraints.HORIZONTAL
                anchor = GridBagConstraints.PAGE_START
                insets = if (index == 0) JBUI.emptyInsets() else JBUI.insetsTop(GROUP_INSET)
            }
            add(GroupComponent(group), groupConstrains)
        }
    }

    companion object {

        const val GROUP_INSET = 17

    }

}