package com.solanteq.solar.plugin.ui.component

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanel
import com.solanteq.solar.plugin.element.FormGroup
import com.solanteq.solar.plugin.l10n.L10nLocale
import javax.swing.BorderFactory

class GroupComponent(
    private val group: FormGroup,
    private val isInGroupRow: Boolean
) : JBPanel<GroupComponent>(), FormComponent {

    private val borderGap = 20

    init {
        border = BorderFactory.createEmptyBorder(borderGap, borderGap, borderGap, borderGap)
        background = JBColor.GREEN
        val ruL10nValues = group.l10ns.filter { it.locale == L10nLocale.RU_RU }.map { it.value }
        //add(JBLabel(ruL10nValues.firstOrNull() ?: group.name ?: "Unnamed group"))

//        val groupSize = if(isInGroupRow) {
//            group.size ?: FormUI.COLUMN_WIDTH
//        } else {
//            FormUI.COLUMN_WIDTH
//        }
//        val parentAbsoluteWidth = parent.size.width
//        val currentAbsoluteWidth = parentAbsoluteWidth / FormUI.COLUMN_WIDTH * groupSize
        //size = Dimension(currentAbsoluteWidth, DEFAULT_HEIGHT)
    }

    companion object {

        private const val DEFAULT_HEIGHT = 200

    }

}