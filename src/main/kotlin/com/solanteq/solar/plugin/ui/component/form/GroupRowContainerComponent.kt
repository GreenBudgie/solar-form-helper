package com.solanteq.solar.plugin.ui.component.form

import com.intellij.util.ui.JBUI
import com.solanteq.solar.plugin.element.FormGroupRow
import com.solanteq.solar.plugin.ui.component.util.Refreshable
import com.solanteq.solar.plugin.ui.editor.FormEditor
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JPanel

class GroupRowContainerComponent(
    editor: FormEditor,
    groupRows: List<FormGroupRow>
) : JPanel(), Refreshable {

    private val groupRowComponents: List<GroupRowComponent>

    init {
        layout = GridBagLayout()
        groupRowComponents = groupRows.mapIndexed { index, groupRow ->
            val groupRowConstrains = GridBagConstraints().apply {
                gridx = 0
                gridy = index
                weightx = 1.0
                weighty = 1.0
                fill = GridBagConstraints.HORIZONTAL
                anchor = GridBagConstraints.FIRST_LINE_START
                insets = if (index == 0) JBUI.emptyInsets() else JBUI.insetsTop(GroupContainerComponent.GROUP_INSET)
            }
            val groupRowComponent = GroupRowComponent(editor, groupRow)
            groupRowComponent.updateVisibility()
            add(groupRowComponent, groupRowConstrains)
            groupRowComponent
        }
    }

    override fun refresh() {
        groupRowComponents.forEach {
            it.updateVisibility()
            it.refresh()
        }
    }

}