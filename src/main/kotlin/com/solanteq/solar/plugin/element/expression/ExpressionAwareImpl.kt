package com.solanteq.solar.plugin.element.expression

import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.psi.PsiSymbolReferenceService
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.element.expression.ExpressionAware.Companion.NEVER_EXPRESSION
import com.solanteq.solar.plugin.reference.expression.ExpressionSymbolReference
import com.solanteq.solar.plugin.symbol.FormSymbol
import io.ktor.util.collections.*

/**
 * An implementation of interface for [FormElement]s that contain properties like `visibleWhen`
 */
class ExpressionAwareImpl(private val objectContainingExpressions: JsonObject) : ExpressionAware {

    private val expressionPropertyCache: MutableMap<ExpressionType, JsonProperty?> = ConcurrentMap()
    private val expressionsCache: MutableMap<ExpressionType, List<FormExpression>> = ConcurrentMap()
    private val expressionSymbolsCache: MutableMap<ExpressionType, List<FormSymbol>> = ConcurrentMap()
    private val expressionElementCache: MutableMap<ExpressionType, FormExpression?> = ConcurrentMap()

    override fun getExpressionProperty(type: ExpressionType): JsonProperty? {
        return expressionPropertyCache.computeIfAbsent(type) {
            objectContainingExpressions.findProperty(type.key)
        }
    }

    override fun getExpressionPropertyValue(type: ExpressionType): JsonStringLiteral? {
        val expressionProperty = getExpressionProperty(type) ?: return null
        return expressionProperty.value as? JsonStringLiteral
    }

    override fun getExpressionName(type: ExpressionType): String? {
        return getExpressionPropertyValue(type)?.value
    }

    override fun getExpressions(type: ExpressionType): List<FormExpression> {
        return expressionsCache.computeIfAbsent(type) {
            val expressionSymbols = getExpressionSymbols(type)
            val expressionDeclarations = expressionSymbols.map { it.element }
            val expressionObjects = expressionDeclarations.mapNotNull {
                it.parent?.parent as? JsonObject
            }
            expressionObjects.mapNotNull { FormExpression.createFrom(it) }
        }
    }

    override fun getExpression(type: ExpressionType): FormExpression? {
        return expressionElementCache.computeIfAbsent(type) {
            val expressionSymbol = getExpressionSymbols(type).firstOrNull() ?: return@computeIfAbsent null
            val expressionObject = expressionSymbol.element.parent?.parent as? JsonObject ?: return@computeIfAbsent null
            FormExpression.createFrom(expressionObject)
        }
    }

    override fun isNeverExpression(type: ExpressionType): Boolean {
        return getExpressionName(type) == NEVER_EXPRESSION
    }

    override fun isNeverVisible() = isNeverExpression(ExpressionType.VISIBLE_WHEN)

    private fun getExpressionSymbols(type: ExpressionType): List<FormSymbol> {
        return expressionSymbolsCache.computeIfAbsent(type) {
            val value = getExpressionPropertyValue(type) ?: return@computeIfAbsent emptyList()
            val expressionReference = PsiSymbolReferenceService
                .getService()
                .getReferences(value)
                .firstOrNull() as? ExpressionSymbolReference ?: return@computeIfAbsent emptyList()
            expressionReference.resolveReference()
        }
    }

}