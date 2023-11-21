package com.solanteq.solar.plugin.ui.component.form

import com.intellij.util.ui.JBUI
import com.solanteq.solar.plugin.element.FormGroup
import com.solanteq.solar.plugin.ui.component.util.Refreshable
import com.solanteq.solar.plugin.ui.editor.FormEditor
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JPanel

class GroupContainerComponent(
    editor: FormEditor,
    groups: List<FormGroup>
) : JPanel(), Refreshable {

    private val groupComponents: List<GroupComponent>

    init {
        layout = GridBagLayout()
        val visibleGroups = groups.filterNot { it.isNeverVisible() }
        groupComponents = visibleGroups.mapIndexed { index, group ->
            val groupConstrains = GridBagConstraints().apply {
                gridx = 0
                gridy = index
                weightx = 1.0
                weighty = 1.0
                fill = GridBagConstraints.HORIZONTAL
                anchor = GridBagConstraints.PAGE_START
                insets = if (index == 0) JBUI.emptyInsets() else JBUI.insetsTop(GROUP_INSET)
            }
            val groupComponent = GroupComponent(editor, group)
            groupComponent.updateVisibility()
            add(groupComponent, groupConstrains)
            groupComponent
        }
    }

    override fun refresh() {
        groupComponents.forEach {
            it.updateVisibility()
            it.refresh()
        }
    }

    companion object {

        const val GROUP_INSET = 17

    }

}