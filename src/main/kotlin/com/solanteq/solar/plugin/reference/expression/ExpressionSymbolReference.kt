package com.solanteq.solar.plugin.reference.expression

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.psi.PsiCompletableReference
import com.solanteq.solar.plugin.element.FormExpression
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.file.RootFormFileType
import com.solanteq.solar.plugin.search.FormGraphSearch
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolReference
import com.solanteq.solar.plugin.symbol.FormSymbolType
import com.solanteq.solar.plugin.util.textRangeWithoutQuotes
import org.jetbrains.kotlin.idea.core.util.toPsiFile

class ExpressionSymbolReference(
    sourceElement: JsonStringLiteral
) : FormSymbolReference(
    sourceElement,
    sourceElement.textRangeWithoutQuotes
), PsiCompletableReference {

    override fun resolveReference(): List<FormSymbol> {
        val expressionName = sourceElement.value
        val file = sourceElement.containingFile.originalFile as JsonFile
        if (file.fileType == RootFormFileType) {
            return findDeclarations(expressionName, file)
        }
        val virtualFile = file.virtualFile ?: return emptyList()
        val project = file.project
        return FormGraphSearch.findTopmostRootForms(project, virtualFile).flatMap {
            findDeclarations(expressionName, it.toPsiFile(project) as JsonFile)
        }
    }

    override fun getCompletionVariants(): List<LookupElement> {
        val file = sourceElement.containingFile.originalFile as JsonFile
        if (file.fileType == RootFormFileType) {
            return findLookupElements(file)
        }
        val virtualFile = file.virtualFile ?: return emptyList()
        val project = file.project
        return FormGraphSearch.findTopmostRootForms(project, virtualFile).flatMap {
            findLookupElements(it.toPsiFile(project) as JsonFile)
        }
    }

    private fun findLookupElements(file: JsonFile): List<LookupElement> {
        return findExpressions(file).mapNotNull {
            val name = it.name ?: return@mapNotNull null
            LookupElementBuilder
                .create(name)
                .withTypeText(it.valueText)
        }
    }

    private fun findExpressions(file: JsonFile): List<FormExpression> {
        return FormRootFile.createFrom(file)?.expressions ?: emptyList()
    }

    private fun findDeclarations(expressionName: String, file: JsonFile): List<FormSymbol> {
        val expressions = findExpressions(file)
        val applicableExpressions = expressions.filter { it.name == expressionName }
        return applicableExpressions.mapNotNull {
            val element = it.namePropertyValue ?: return@mapNotNull null
            FormSymbol.withElementTextRange(element, element.textRangeWithoutQuotes, FormSymbolType.EXPRESSION)
        }
    }

}