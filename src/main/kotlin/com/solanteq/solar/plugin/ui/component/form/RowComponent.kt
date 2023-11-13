package com.solanteq.solar.plugin.ui.component.form

import com.solanteq.solar.plugin.element.FormRow
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.Box
import javax.swing.JPanel

class RowComponent(
    private val row: FormRow
) : JPanel() {

    init {
        layout = GridBagLayout()
        var index = 0
        var size = 0
        val visibleFields = row.fields?.filterNot { it.isNeverVisible() } ?: emptyList()
        visibleFields.forEach { field ->
            val fieldSize = field.fieldSize ?: FieldComponent.DEFAULT_FIELD_SIZE
            val labelSize = field.labelSize ?: FieldLabelComponent.DEFAULT_LABEL_SIZE
            if(labelSize > 0) {
                val labelConstraint = GridBagConstraints().apply {
                    weightx = labelSize / ROW_COLUMNS.toDouble()
                    gridx = index++
                    gridy = 0
                    fill = GridBagConstraints.HORIZONTAL
                }
                val labelComponent = FieldLabelComponent(field)
                add(labelComponent, labelConstraint)
            }
            val fieldConstraint = GridBagConstraints().apply {
                weightx = fieldSize / ROW_COLUMNS.toDouble()
                gridx = index++
                gridy = 0
                fill = GridBagConstraints.HORIZONTAL
            }
            val fieldComponent = FieldComponent(field)
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