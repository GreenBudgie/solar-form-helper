package com.solanteq.solar.plugin.ui.component

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import com.solanteq.solar.plugin.element.FormGroupRow
import com.solanteq.solar.plugin.ui.FormUI
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.BorderFactory

class GroupRowComponent(
    private val groupRow: FormGroupRow
) : JBPanel<GroupRowComponent>(), FormComponent {

    init {
        layout = GridBagLayout()
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        background = JBColor.CYAN
        var prevGridX = 0
        groupRow.groups?.forEach { group ->
            val groupSize = group.size ?: FormUI.COLUMN_WIDTH
            val constraints = GridBagConstraints().apply {
                gridx = prevGridX
                gridwidth = 1
                fill = GridBagConstraints.BOTH
                insets = JBUI.insets(10)
                weightx = groupSize / 24.0
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