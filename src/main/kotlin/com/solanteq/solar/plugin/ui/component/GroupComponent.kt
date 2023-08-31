package com.solanteq.solar.plugin.ui.component

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.solanteq.solar.plugin.element.FormGroup
import com.solanteq.solar.plugin.l10n.L10nLocale
import net.miginfocom.layout.AC
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import java.awt.Dimension
import javax.swing.JPanel

class GroupComponent(
    private val group: FormGroup,
    private val isInGroupRow: Boolean
) : JBPanel<GroupComponent>() {

    private val header = JPanel()
    private val body = JPanel()

    init {
        layout = MigLayout(
            LC().insetsAll("0"),
            AC().count(1),
            AC().count(2).gap("0")
        )

        val ruL10nValue = group.getL10nValue(L10nLocale.RU) ?: "NOT LOCALIZED"

        header.background = JBColor.CYAN
        header.preferredSize = Dimension(0, 30)
        header.add(JBLabel(ruL10nValue))

        configureBody(body)

        add(header, CC().wrap().width("100%"))
        add(body, CC().width("100%"))
    }

    private fun configureBody(body: JPanel) = with(body) {
        val rows = group.rows ?: emptyList()
        layout = MigLayout(
            LC().debug(),
            AC().count(1),
            AC().count(rows.size).gap("16"),
        )
        rows.forEach {
            add(RowComponent(it), CC().wrap().width("100%"))
        }
    }

    companion object {

        private const val DEFAULT_HEIGHT = 200

    }

}