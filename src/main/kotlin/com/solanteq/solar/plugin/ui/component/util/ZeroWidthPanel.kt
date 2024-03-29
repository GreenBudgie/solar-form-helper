package com.solanteq.solar.plugin.ui.component.util

import java.awt.Dimension
import javax.swing.JPanel

/**
 * A neat hack for GridBagLayout.
 * This panel will tell the layout to distribute space evenly even if preferred width is set on this panel.
 */
open class ZeroWidthPanel : JPanel() {

    override fun getPreferredSize(): Dimension {
        return Dimension(0, super.getPreferredSize().height)
    }

}