package com.solanteq.solar.plugin.ui.component

import com.intellij.ui.components.JBPanel
import com.solanteq.solar.plugin.element.FormField
import com.solanteq.solar.plugin.element.FormRow
import com.solanteq.solar.plugin.ui.FormUIConstants
import net.miginfocom.layout.AC
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout

class RowComponent(
    private val row: FormRow
) : JBPanel<GroupRowComponent>() {

    init {
        layout = MigLayout(
            LC().fillX().insetsAll("0"),
            AC().count(FormUIConstants.COLUMNS).gap("1").fill(),
            AC().count(1)
        )
        row.fields?.forEach { field ->
            val fieldSize = field.fieldSize ?: 4
            val labelSize = field.labelSize ?: FormField.DEFAULT_LABEL_SIZE
            if(labelSize > 0) {
                add(FieldLabelComponent(field), CC().spanX(labelSize).alignX("right").alignY("center"))
            }
            add(FieldComponent(field), CC().spanX(fieldSize))
        }
    }

}