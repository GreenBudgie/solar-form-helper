package com.solanteq.solar.plugin.symbol

import com.solanteq.solar.plugin.element.FormExpression
import com.solanteq.solar.plugin.element.FormField
import com.solanteq.solar.plugin.element.FormGroup

/**
 * As all [FormSymbol]s share the same logic, inheritance isn't necessary.
 * Thus, every [FormSymbol] must have a type defined on its creation.
 * It is used in usages search to distinguish between different [FormSymbol]s.
 */
enum class FormSymbolType {
    /**
     * @see FormGroup
     */
    GROUP,
    /**
     * @see FormField
     */
    FIELD,
    /**
     * @see FormExpression
     */
    EXPRESSION,
}