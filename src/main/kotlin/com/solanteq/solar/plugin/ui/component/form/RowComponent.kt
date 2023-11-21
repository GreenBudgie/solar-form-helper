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
    row: FormRow
) : ExpressionAwareComponent<FormRow>(editor, row) {

    private val labeledFields: List<LabeledField>
    private var visibleLabeledFields: List<LabeledField>? = null

    init {
        layout = GridBagLayout()

        labeledFields = row.fields?.map {
            val labelSize = it.labelSize ?: FieldLabelComponent.DEFAULT_LABEL_SIZE
            val labelComponent = if (labelSize > 0) {
                FieldLabelComponent(editor, it)
            } else {
                null
            }
            val fieldComponent = FieldComponent(editor, it)
            LabeledField(labelComponent, fieldComponent)
        } ?: emptyList()
        rebuildIfNeeded()
        updateVisibility()
    }

    override fun refresh() {
        rebuildIfNeeded()
        visibleLabeledFields?.forEach {
            it.label?.refresh()
            it.field.refresh()
        }
        updateVisibility()
    }

    private fun rebuildIfNeeded() {
        val visibleLabeledFields = labeledFields.filter { it.field.shouldBeVisible() }
        if (visibleLabeledFields == this.visibleLabeledFields) {
            return
        }
        this.visibleLabeledFields = visibleLabeledFields
        removeAll()

        var index = 0
        var size = 0
        visibleLabeledFields.forEach { labeledField ->
            val labelComponent = labeledField.label
            val fieldComponent = labeledField.field
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

    data class LabeledField(val label: FieldLabelComponent?, val field: FieldComponent)

    companion object {

        const val ROW_HEIGHT = 35
        const val ROW_COLUMNS = 24

    }

}