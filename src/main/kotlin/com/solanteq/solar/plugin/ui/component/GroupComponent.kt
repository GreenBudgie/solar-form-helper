package com.solanteq.solar.plugin.ui.component

import com.intellij.ui.util.preferredHeight
import com.intellij.util.ui.JBUI
import com.solanteq.solar.plugin.element.FormGroup
import com.solanteq.solar.plugin.l10n.L10nLocale
import com.solanteq.solar.plugin.ui.FormUIConstants
import com.solanteq.solar.plugin.ui.custom.UniversalBorder
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JPanel

class GroupComponent(
    private val group: FormGroup,
    private val isInGroupRow: Boolean
) : JPanel() {

    init {
        layout = GridBagLayout()

        addHeader()
        addBody()
    }

    private fun addHeader() {
        val groupName = group.getL10nValue(L10nLocale.RU) ?: ""
        val headerLabel = JLabel(groupName).apply {
            font = font.deriveFont(12.25f)
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
                .topRadius(5)
                .background(FormUIConstants.HEADER_COLOR)
                .build()
            preferredHeight = 29
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
        val body = JPanel().apply {
            border = UniversalBorder.builder()
                .color(FormUIConstants.BORDER_COLOR)
                .drawTop(false)
                .bottomRadius(5)
                .build()
            layout = GridBagLayout()
            addRows(this)
        }
        val bodyConstraints = GridBagConstraints().apply {
            this.fill = GridBagConstraints.HORIZONTAL
            this.weighty = 1.0
            this.gridx = 0
            this.gridy = 1
            this.anchor = GridBagConstraints.PAGE_START
        }
        add(body, bodyConstraints)
    }

    private fun addRows(body: JPanel) {
        val visibleRows = group.rows?.filterNot { it.isNeverVisible() } ?: emptyList()
        visibleRows.forEachIndexed { index, row ->
            val isFirst = index == 0
            val isLast = index == visibleRows.size - 1
            val topInset = if (isFirst) TOP_INSET else 0
            val bottomInset = if (isLast) BOTTOM_INSET else 0
            val rowConstraints = GridBagConstraints().apply {
                this.fill = GridBagConstraints.HORIZONTAL
                this.weightx = 1.0
                this.insets = JBUI.insets(topInset, SIDE_INSET, bottomInset, SIDE_INSET)
                this.gridx = 0
                this.gridy = index
            }
            body.add(RowComponent(row), rowConstraints)
        }
    }

    companion object {

        const val TOP_INSET = 12
        const val BOTTOM_INSET = 21
        const val SIDE_INSET = 16

        const val HEADER_LABEL_LEFT_INSET = 43

    }

}