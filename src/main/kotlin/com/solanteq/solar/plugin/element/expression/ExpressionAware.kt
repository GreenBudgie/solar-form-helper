package com.solanteq.solar.plugin.element.expression

import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.solanteq.solar.plugin.element.base.FormElement

/**
 * An interface for [FormElement]s that contain properties like `visibleWhen`
 */
interface ExpressionAware {

    /**
     * Returns the expression property by the given [type], or null if it is not present.
     */
    fun getExpressionProperty(type: ExpressionType): JsonProperty?

    /**
     * Returns the expression property value by the given [type], or null if it is not present
     */
    fun getExpressionPropertyValue(type: ExpressionType): JsonStringLiteral?

    /**
     * Returns the expression name by the given [type], or null if it is not present
     */
    fun getExpressionName(type: ExpressionType): String?
    /**
     * Returns all expressions that were found by the given [type], or an empty list if the declaration
     * of this expression does not exist or no expressions are found
     */
    fun getExpressions(type: ExpressionType): List<FormExpression>

    /**
     * Returns a single expression (the first one if multiple found) that is found by the given [type],
     * or null if the declaration of this expression does not exist or expression is not found
     */
    fun getExpression(type: ExpressionType): FormExpression?

    /**
     * `never` is a standard expressions that always has the value `false`. This method does not
     * search for available expressions (performance boost!) and just checks whether the value of the
     * given expression is `never`.
     *
     * Use with care: it will return `true` even if there is no `never` expression is set on the form or
     * it has a different value (this should NEVER happen, but who knows...)
     */
    fun isNeverExpression(type: ExpressionType): Boolean

    /**
     * Whether `visibleWhen` expression has `never` value
     */
    fun isNeverVisible(): Boolean

    companion object {

        const val NEVER_EXPRESSION = "never"

    }

}