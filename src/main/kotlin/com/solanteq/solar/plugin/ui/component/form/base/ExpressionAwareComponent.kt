package com.solanteq.solar.plugin.ui.component.form.base

import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.element.expression.ExpressionAware
import com.solanteq.solar.plugin.element.expression.ExpressionType
import com.solanteq.solar.plugin.ui.component.util.Refreshable
import com.solanteq.solar.plugin.ui.editor.FormEditor

/**
 * Base class for form component that can be altered by an expression
 */
abstract class ExpressionAwareComponent<T>(
    editor: FormEditor,
    formElement: T
) : FormComponent<T>(editor, formElement), Refreshable
    where T : FormElement<*>, T : ExpressionAware {

    private val visibleWhenExpressionName by lazy(LazyThreadSafetyMode.PUBLICATION) {
        formElement.getExpressionName(ExpressionType.VISIBLE_WHEN)
    }

    open fun shouldBeVisible(): Boolean {
        val expressionName = visibleWhenExpressionName ?: return true
        if (expressionName == ExpressionAware.NEVER_EXPRESSION) {
            return false
        }
        return editor.getState().isExpressionTrue(expressionName)
    }

    fun updateVisibility() {
        isVisible = shouldBeVisible()
    }

}