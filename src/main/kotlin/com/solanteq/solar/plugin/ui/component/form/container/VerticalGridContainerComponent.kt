package com.solanteq.solar.plugin.ui.component.form.container

import com.intellij.util.ui.JBUI
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.element.expression.ExpressionAware
import com.solanteq.solar.plugin.ui.component.form.base.ExpressionAwareComponent
import com.solanteq.solar.plugin.ui.component.util.Refreshable
import com.solanteq.solar.plugin.ui.editor.FormEditor
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JPanel

/**
 * Places components vertically one under another.
 * Uses [GridBagLayout] with weights, so components may have arbitrary sizes.
 */
abstract class VerticalGridContainerComponent<T>(
    val editor: FormEditor,
    formElements: List<T>,
    /**
     * Inset between components.
     * Inset is not added to the topmost component
     */
    private val verticalInset: Int
) : JPanel(), Refreshable where T : FormElement<*>, T : ExpressionAware {

    val formComponents: List<ExpressionAwareComponent<T>>

    init {
        layout = GridBagLayout()
        formComponents = formElements.mapIndexed { index, element ->
            addComponent(index, element)
        }
    }

    /**
     * Creates the component for this container.
     *
     * **Do not** add the created component to the layout. It will be done automatically in [addComponent].
     * Usually it is enough to invoke the component constructor.
     */
    abstract fun createComponent(element: T): ExpressionAwareComponent<T>

    private fun addComponent(gridY: Int, element: T): ExpressionAwareComponent<T>  {
        val componentConstraints = GridBagConstraints().apply {
            gridx = 0
            gridy = gridY
            weightx = 1.0
            weighty = 1.0
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.FIRST_LINE_START
            insets = if (gridY == 0) JBUI.emptyInsets() else JBUI.insetsTop(verticalInset)
        }
        val component = createComponent(element)
        add(component, componentConstraints)
        return component
    }

}