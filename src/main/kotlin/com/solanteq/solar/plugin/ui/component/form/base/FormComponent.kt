package com.solanteq.solar.plugin.ui.component.form.base

import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.ui.editor.FormEditor
import javax.swing.JPanel

/**
 * Base class for form UI components backed by [FormElement]
 */
abstract class FormComponent<T : FormElement<*>>(
    val editor: FormEditor,
    val formElement: T
) : JPanel()