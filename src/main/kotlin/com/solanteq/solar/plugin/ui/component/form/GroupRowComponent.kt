package com.solanteq.solar.plugin.ui.component.form

import com.intellij.util.ui.JBUI
import com.solanteq.solar.plugin.element.FormGroupRow
import com.solanteq.solar.plugin.ui.component.form.base.ExpressionAwareComponent
import com.solanteq.solar.plugin.ui.component.util.Refreshable
import com.solanteq.solar.plugin.ui.editor.FormEditor
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.Box

class GroupRowComponent(
    editor: FormEditor,
    private val groupRow: FormGroupRow
) : ExpressionAwareComponent<FormGroupRow>(editor, groupRow), Refreshable {

    private val groupComponents: List<GroupComponent>
    private var visibleGroups: List<GroupComponent>? = null

    init {
        layout = GridBagLayout()
        groupComponents = groupRow.groups?.map {
            GroupComponent(editor, it)
        } ?: emptyList()
        rebuildIfNeeded()
    }

    override fun refresh() {
        rebuildIfNeeded()
        visibleGroups?.forEach { it.refresh() }
        //repaint()
    }

    override fun shouldBeVisible(): Boolean {
        if (!super.shouldBeVisible()) {
            return false
        }
        return !visibleGroups.isNullOrEmpty()
    }

    private fun rebuildIfNeeded() {
        val visibleGroups = groupComponents.filter { it.shouldBeVisible() }
        if (visibleGroups == this.visibleGroups) {
            return
        }
        removeAll()

        var size = 0
        this.visibleGroups = visibleGroups.mapIndexed { index, groupComponent ->
            val groupSize = groupComponent.group.size ?: GROUP_COLUMNS
            val constraints = GridBagConstraints().apply {
                gridx = index
                gridy = 0
                fill = GridBagConstraints.HORIZONTAL
                weightx = groupSize / GROUP_COLUMNS.toDouble()
                anchor = GridBagConstraints.FIRST_LINE_START
                insets = if (index == 0) JBUI.emptyInsets() else JBUI.insetsLeft(GROUP_INSET)
            }
            add(groupComponent, constraints)
            size += groupSize
            groupComponent
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