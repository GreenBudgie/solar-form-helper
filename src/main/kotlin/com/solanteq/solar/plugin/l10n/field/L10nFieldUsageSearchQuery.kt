package com.solanteq.solar.plugin.l10n.field

import com.intellij.model.psi.PsiSymbolReferenceService
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import com.solanteq.solar.plugin.element.FormField
import com.solanteq.solar.plugin.element.FormIncludedFile
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.element.toFormElement
import com.solanteq.solar.plugin.file.RootFormFileType
import com.solanteq.solar.plugin.l10n.search.L10nSearch
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolType
import com.solanteq.solar.plugin.symbol.FormSymbolUsage
import com.solanteq.solar.plugin.symbol.FormSymbolUsageSearchQuery
import com.solanteq.solar.plugin.util.RangeSplit
import com.solanteq.solar.plugin.util.asListOrEmpty
import com.solanteq.solar.plugin.util.convert
import org.jetbrains.kotlin.psi.psiUtil.contains

class L10nFieldUsageSearchQuery(
    resolveTarget: FormSymbol,
    searchScope: SearchScope
) : FormSymbolUsageSearchQuery(resolveTarget, searchScope){

    override fun processDeclarations(consumer: Processor<in FormSymbolUsage>): Boolean {
        val file = resolveTarget.file
        if(file !in searchScope) return true

        val fieldChain = RangeSplit.from(resolveTarget.element)
        val offsetInElement = resolveTarget.elementTextRange.startOffset
        val chainIndex = fieldChain.getEntryAtOffset(offsetInElement)?.index ?: return true

        val allRootForms = if(file.fileType == RootFormFileType) {
            file.toFormElement<FormRootFile>().asListOrEmpty()
        } else {
            val includedForm = file.toFormElement<FormIncludedFile>()
            includedForm?.allRootForms ?: emptyList()
        }

        val allFields = allRootForms.flatMap { it.allFields }
        val fieldsInScope = allFields.filter { it.sourceElement in searchScope }

        val provider = L10nFieldDeclarationProvider()
        fieldsInScope.forEach {
            if(!processDeclarationsInField(it, fieldChain, chainIndex, provider, consumer)) {
                return false
            }
        }
        return true
    }

    private fun processDeclarationsInField(field: FormField,
                                           sourceFieldChain: RangeSplit,
                                           compareUntilIndex: Int,
                                           declarationProvider: L10nFieldDeclarationProvider,
                                           consumer: Processor<in FormSymbolUsage>): Boolean {
        val nameElement = field.namePropertyValue ?: return true
        field.fieldNameChain.forEach { (textRange) ->
            ProgressManager.checkCanceled()
            val declarations = declarationProvider.getDeclarations(nameElement, textRange.startOffset)
            val fieldDeclaration = declarations.find {
                it.symbol.type == FormSymbolType.FIELD
            }
            if(fieldDeclaration?.symbol?.targetName != resolveTarget.targetName) {
                return@forEach
            }
            if(!compareChains(sourceFieldChain, field.fieldNameChain, compareUntilIndex + 1)) {
                return@forEach
            }
            if(!consumer.process(FormSymbolUsage(fieldDeclaration.symbol, true))) {
                return false
            }
        }
        return true
    }

    private fun compareChains(source: RangeSplit, compareTo: RangeSplit, untilIndex: Int): Boolean {
        if(compareTo.size < source.size) return false
        val trimmedSource = source.take(untilIndex).convert()
        val trimmedCompareTo = compareTo.take(untilIndex).convert()
        return trimmedSource == trimmedCompareTo
    }

    override fun processReferences(globalSearchScope: GlobalSearchScope,
                                   consumer: Processor<in FormSymbolUsage>): Boolean {
        val formL10ns = L10nSearch.findFormL10ns(resolveTarget.project, globalSearchScope)
        val keys = formL10ns.map { it.keyElement }
        val symbolReferenceService = PsiSymbolReferenceService.getService()
        keys.forEach {
            val references = symbolReferenceService.getReferences(it)
            val fieldReferences = references.filterIsInstance<L10nFieldSymbolReference>()
            if(!processReferences(fieldReferences, consumer)) {
                return false
            }
        }
        return true
    }

}