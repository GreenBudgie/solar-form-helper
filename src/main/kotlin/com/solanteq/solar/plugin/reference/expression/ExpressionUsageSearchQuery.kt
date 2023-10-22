package com.solanteq.solar.plugin.reference.expression

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import com.solanteq.solar.plugin.element.FormExpression
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
                if (property.name in FormExpression.expressionProperties) {
                    val value = property.value as? JsonStringLiteral ?: return@processElements true
                    if (value.value == expressionName) {
                        val usage = FormSymbolUsage(file, value.absoluteTextRangeWithoutQuotes, false)
                        if (!consumer.process(usage)) {
                            return@processElements false
                        }
                    }
                }
                true
            }
        }
        return result != null
    }

}