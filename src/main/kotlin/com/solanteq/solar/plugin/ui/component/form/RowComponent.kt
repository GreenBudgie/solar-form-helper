package com.solanteq.solar.plugin.ui.component.form

import com.solanteq.solar.plugin.element.FormRow
import com.solanteq.solar.plugin.ui.component.form.base.ExpressionAwareComponent
import com.solanteq.solar.plugin.ui.editor.FormEditor
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.Box

class RowComponent(
    editor: FormEditor,
    private val row: FormRow
) : ExpressionAwareComponent<FormRow>(editor, row) {

    private val fieldPairs: List<Pair<FieldLabelComponent?, FieldComponent>>
    private var visibleFieldPairs: List<Pair<FieldLabelComponent?, FieldComponent>>? = null

    init {
        layout = GridBagLayout()

        fieldPairs = row.fields?.map {
            val labelSize = it.labelSize ?: FieldLabelComponent.DEFAULT_LABEL_SIZE
            val labelComponent = if (labelSize > 0) {
                FieldLabelComponent(editor, it)
            } else {
                null
            }
            labelComponent to FieldComponent(editor, it)
        } ?: emptyList()

        rebuildIfNeeded()
    }

    override fun refresh() {
        rebuildIfNeeded()
        visibleFieldPairs?.forEach {
            it.first?.refresh()
            it.second.refresh()
        }
    }

    private fun rebuildIfNeeded() {
        val visibleFieldPairs = fieldPairs.filter { it.second.shouldBeVisible() }
        if (visibleFieldPairs == this.visibleFieldPairs) {
            return
        }
        this.visibleFieldPairs = visibleFieldPairs
        removeAll()

        var index = 0
        var size = 0
        visibleFieldPairs.forEach { fieldPair ->
            val labelComponent = fieldPair.first
            val fieldComponent = fieldPair.second
            val fieldElement = fieldComponent.field
            val fieldSize = fieldElement.fieldSize ?: FieldComponent.DEFAULT_FIELD_SIZE
            val labelSize = fieldElement.labelSize ?: FieldLabelComponent.DEFAULT_LABEL_SIZE
            if(labelComponent != null && labelSize > 0) {
                val labelConstraint = GridBagConstraints().apply {
                    weightx = labelSize / ROW_COLUMNS.toDouble()
                    gridx = index++
                    gridy = 0
                    fill = GridBagConstraints.HORIZONTAL
                }
                add(labelComponent, labelConstraint)
            }
            val fieldConstraint = GridBagConstraints().apply {
                weightx = fieldSize / ROW_COLUMNS.toDouble()
                gridx = index++
                gridy = 0
                fill = GridBagConstraints.HORIZONTAL
            }
            add(fieldComponent, fieldConstraint)
            size += fieldSize + labelSize
        }
        if(size < ROW_COLUMNS) {
            val strutConstraint = GridBagConstraints().apply {
                weightx = (ROW_COLUMNS - size) / ROW_COLUMNS.toDouble()
                gridx = index++
                gridy = 0
                fill = GridBagConstraints.HORIZONTAL
            }
            val strut = Box.createHorizontalStrut(0).apply {
                preferredSize = Dimension(0, ROW_HEIGHT)
            }
            add(strut, strutConstraint)
        }
    }

    companion object {

        const val ROW_HEIGHT = 35
        const val ROW_COLUMNS = 24

    }

}