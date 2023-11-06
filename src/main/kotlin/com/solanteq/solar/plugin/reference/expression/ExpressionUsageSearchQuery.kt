package com.solanteq.solar.plugin.reference.expression

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import com.solanteq.solar.plugin.element.expression.ExpressionType
import com.solanteq.solar.plugin.search.FormGraphSearch
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolUsage
import com.solanteq.solar.plugin.symbol.FormSymbolUsageSearchQuery
import com.solanteq.solar.plugin.util.absoluteTextRangeWithoutQuotes
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.kotlin.psi.psiUtil.contains

class ExpressionUsageSearchQuery(
    resolveTarget: FormSymbol,
    searchScope: SearchScope
) : FormSymbolUsageSearchQuery(resolveTarget, searchScope){

    override fun processDeclarations(consumer: Processor<in FormSymbolUsage>): Boolean {
        val searchScope = searchScope
        val resolveTarget = resolveTarget
        if(resolveTarget.file !in searchScope) return true
        return consumer.process(FormSymbolUsage(resolveTarget, true))
    }

    override fun processReferences(globalSearchScope: GlobalSearchScope,
                                   consumer: Processor<in FormSymbolUsage>): Boolean {
        val virtualFile = resolveTarget.virtualFile ?: return false
        val project = resolveTarget.project
        val expressionName = resolveTarget.targetName
        val result = FormGraphSearch.processAllRelatedForms(project, virtualFile, true) {
            ProgressManager.checkCanceled()
            if (it !in globalSearchScope) return@processAllRelatedForms true
            val file = it.toPsiFile(project) as JsonFile
            PsiTreeUtil.processElements(file, JsonProperty::class.java) { property ->
                processProperty(file, expressionName, property, consumer)
            }
        }
        return result != null
    }

    private fun processProperty(
        file: JsonFile,
        expressionName: String,
        property: JsonProperty,
        consumer: Processor<in FormSymbolUsage>
    ): Boolean {
        if (property.name !in ExpressionType.expressionProperties) {
            return true
        }
        val value = property.value as? JsonStringLiteral ?: return true
        if (value.value != expressionName) {
            return true
        }
        val usage = FormSymbolUsage(file, value.absoluteTextRangeWithoutQuotes, false)
        return consumer.process(usage)
    }

}