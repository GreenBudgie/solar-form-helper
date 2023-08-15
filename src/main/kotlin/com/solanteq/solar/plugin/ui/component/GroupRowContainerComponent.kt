package com.solanteq.solar.plugin.ui.component

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.solanteq.solar.plugin.element.FormGroupRow
import java.awt.GridLayout
import javax.swing.BorderFactory

class GroupRowContainerComponent(
    private val groupRows: List<FormGroupRow>
) : JBPanel<GroupRowContainerComponent>(), FormComponent {

    init {
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        layout = GridLayout(groupRows.size, 1)
        background = JBColor.MAGENTA
        groupRows.forEach {
            add(GroupRowComponent(it))
        }
    }

}