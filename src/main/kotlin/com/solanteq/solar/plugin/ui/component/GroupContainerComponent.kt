package com.solanteq.solar.plugin.ui.component

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.solanteq.solar.plugin.element.FormGroup
import java.awt.GridLayout

class GroupContainerComponent(
    private val groups: List<FormGroup>
) : JBPanel<GroupContainerComponent>(), FormComponent {

    init {
        layout = GridLayout(groups.size, 1)
        background = JBColor.RED
        groups.forEach {
            add(GroupComponent(it, false))
        }
    }

}