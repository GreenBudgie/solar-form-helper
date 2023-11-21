package com.solanteq.solar.plugin.ui.component.form

import com.intellij.ui.util.preferredHeight
import com.intellij.util.ui.JBUI
import com.solanteq.solar.plugin.element.FormGroup
import com.solanteq.solar.plugin.l10n.L10nLocale
import com.solanteq.solar.plugin.ui.FormColorScheme
import com.solanteq.solar.plugin.ui.component.form.base.ExpressionAwareComponent
import com.solanteq.solar.plugin.ui.component.util.Refreshable
import com.solanteq.solar.plugin.ui.component.util.UniversalBorder
import com.solanteq.solar.plugin.ui.component.util.ZeroWidthPanel
import com.solanteq.solar.plugin.ui.editor.FormEditor
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel

class GroupComponent(
    editor: FormEditor,
    val group: FormGroup
) : ExpressionAwareComponent<FormGroup>(editor, group), Refreshable {

    private var rowComponents: List<RowComponent>? = null

    init {
        layout = GridBagLayout()

        addHeader()
        addBody()
    }

    override fun refresh() {
        rowComponents?.forEach {
            it.updateVisibility()
            it.refresh()
        }
    }

    private fun addHeader() {
        val groupName = group.getL10nValue(L10nLocale.RU) ?: ""
        val headerLabel = JLabel(groupName).apply {
            font = font.deriveFont(HEADER_LABEL_FONT_SIZE)
        }
        val labelConstraints = GridBagConstraints().apply {
            anchor = GridBagConstraints.LINE_START
            insets = JBUI.insetsLeft(HEADER_LABEL_LEFT_INSET)
            weightx = 1.0
        }

        val header = JPanel().apply {
            layout = GridBagLayout()
            border = UniversalBorder.builder()
                .noOutline()
                .topRadius(GROUP_CORNER_RADIUS)
                .background(FormColorScheme.HEADER_COLOR)
                .build()
            preferredHeight = HEADER_HEIGHT
            add(headerLabel, labelConstraints)
        }
        val headerConstraints = GridBagConstraints().apply {
            this.fill = GridBagConstraints.HORIZONTAL
            this.gridx = 0
            this.gridy = 0
            this.weightx = 1.0
        }
        add(header, headerConstraints)
    }

    private fun addBody() {
        val border = UniversalBorder.builder()
            .color(FormColorScheme.BORDER_COLOR)
            .drawTop(false)
            .bottomRadius(GROUP_CORNER_RADIUS)
            .build()
        val paddingBorder = BorderFactory.createEmptyBorder(VERTICAL_INSET, SIDE_INSET, VERTICAL_INSET, SIDE_INSET)
        val body = ZeroWidthPanel().apply {
            this.border = BorderFactory.createCompoundBorder(border, paddingBorder)
            layout = GridBagLayout()
            addRows(this)
            if (rowComponents.isNullOrEmpty()) {
                preferredHeight = MIN_GROUP_HEIGHT
            }
        }
        val bodyConstraints = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            weighty = 1.0
            gridx = 0
            gridy = 1
            anchor = GridBagConstraints.PAGE_START
        }
        add(body, bodyConstraints)
    }

    private fun addRows(body: JPanel) {
        rowComponents = group.rows?.mapIndexed { index, row ->
            val rowConstraints = GridBagConstraints().apply {
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                gridx = 0
                gridy = index
            }
            val rowComponent = RowComponent(editor, row)
            rowComponent.updateVisibility()
            body.add(rowComponent, rowConstraints)
            rowComponent
        }
    }

    companion object {

        const val VERTICAL_INSET = 12
        const val SIDE_INSET = 17

        const val HEADER_HEIGHT = 29
        const val HEADER_LABEL_FONT_SIZE = 12f
        const val HEADER_LABEL_LEFT_INSET = 43

        const val GROUP_CORNER_RADIUS = 5
        const val MIN_GROUP_HEIGHT = VERTICAL_INSET * 2

    }

}