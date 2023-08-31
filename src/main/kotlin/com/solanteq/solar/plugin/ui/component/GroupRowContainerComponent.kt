package com.solanteq.solar.plugin.ui.component

import com.intellij.ui.components.JBPanel
import com.solanteq.solar.plugin.element.FormGroupRow
import com.solanteq.solar.plugin.ui.FormUIConstants
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.BoxLayout

class GroupRowContainerComponent(
    private val groupRows: List<FormGroupRow>
) : JBPanel<GroupRowContainerComponent>() {

    init {
        border = BorderFactory.createEmptyBorder(24, 24, 24, 24)
        preferredSize = Dimension(FormUIConstants.MIN_VIEWPORT_WIDTH, 0)
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        groupRows.forEach {
            add(GroupRowComponent(it))
        }
    }

}