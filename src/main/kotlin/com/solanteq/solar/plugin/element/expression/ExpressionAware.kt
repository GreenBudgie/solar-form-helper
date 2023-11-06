package com.solanteq.solar.plugin.element.expression

import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.psi.PsiSymbolReferenceService
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.reference.expression.ExpressionSymbolReference
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.util.valueAsStringOrNull

/**
 * An interface for [FormElement]s that contain properties like `visibleWhen`
 */
interface ExpressionAware {

    fun getObjectContainingExpressions(): JsonObject

    /**
     * Returns the expression property by the given [type], or null if it is not present.
     */
    fun getExpressionProperty(type: ExpressionType): JsonProperty? {
        return getObjectContainingExpressions().findProperty(type.key)
    }

    /**
     * Returns the expression property value by the given [type], or null if it is not present
     */
    fun getExpressionPropertyValue(type: ExpressionType): JsonStringLiteral? {
        val visibleWhenProperty = getExpressionProperty(type) ?: return null
        return visibleWhenProperty.value as? JsonStringLiteral
    }

    /**
     * Returns all expressions that were found by the given [type], or an empty list if the declaration
     * of this expression does not exist or no expressions are found
     */
    fun getExpressions(type: ExpressionType): List<FormExpression> {
        val expressionSymbols = getExpressionSymbols(type)
        val expressionDeclarations = expressionSymbols.map { it.element }
        val expressionObjects = expressionDeclarations.mapNotNull {
            it.parent?.parent as? JsonObject
        }
        return expressionObjects.mapNotNull { FormExpression.createFrom(it) }
    }

    /**
     * Returns a single expression (the first one if multiple found) that is found by the given [type],
     * or null if the declaration of this expression does not exist or expression is not found
     */
    fun getExpression(type: ExpressionType): FormExpression? {
        val expressionSymbol = getExpressionSymbols(type).firstOrNull() ?: return null
        val expressionObject = expressionSymbol.element.parent?.parent as? JsonObject ?: return null
        return FormExpression.createFrom(expressionObject)
    }

    /**
     * `never` is a standard expressions that always has the value `false`. This method does not
     * search for available expressions (performance boost!) and just checks whether the value of the
     * given expression is `never`.
     *
     * Use with care: it will return `true` even if there is no `never` expression is set on the form or
     * it has a different value (this should NEVER happen, but who knows...)
     */
    fun isNeverExpression(type: ExpressionType): Boolean {
        return getExpressionProperty(type).valueAsStringOrNull() == NEVER_EXPRESSION
    }

    /**
     * Whether `visibleWhen` expression has `never` value
     */
    fun isNeverVisible() = isNeverExpression(ExpressionType.VISIBLE_WHEN)

    private fun getExpressionSymbols(type: ExpressionType): List<FormSymbol> {
        val value = getExpressionPropertyValue(type) ?: return emptyList()
        val expressionReference = PsiSymbolReferenceService
            .getService()
            .getReferences(value)
            .firstOrNull() as? ExpressionSymbolReference ?: return emptyList()
        return expressionReference.resolveReference()
    }

    companion object {

        const val NEVER_EXPRESSION = "never"

    }

}