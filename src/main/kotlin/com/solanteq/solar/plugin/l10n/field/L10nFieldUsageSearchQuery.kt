package com.solanteq.solar.plugin.l10n.field

import com.intellij.model.psi.PsiSymbolReferenceService
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.Processor
import com.solanteq.solar.plugin.element.FormField
import com.solanteq.solar.plugin.element.FormIncludedFile
import com.solanteq.solar.plugin.element.FormTopLevelFile
import com.solanteq.solar.plugin.element.toFormElement
import com.solanteq.solar.plugin.file.TopLevelFormFileType
import com.solanteq.solar.plugin.l10n.L10nService
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolType
import com.solanteq.solar.plugin.symbol.FormSymbolUsage
import com.solanteq.solar.plugin.symbol.FormSymbolUsageSearchQuery
import com.solanteq.solar.plugin.util.asListOrEmpty
import org.jetbrains.kotlin.psi.psiUtil.contains

class L10nFieldUsageSearchQuery(
    resolveTarget: FormSymbol,
    searchScope: SearchScope
) : FormSymbolUsageSearchQuery(resolveTarget, searchScope){

    override fun processDeclarations(consumer: Processor<in FormSymbolUsage>): Boolean {
        val file = resolveTarget.file
        if(file !in searchScope) return true

        val allTopLevelForms = if(file.fileType == TopLevelFormFileType) {
            file.toFormElement<FormTopLevelFile>().asListOrEmpty()
        } else {
            val includedForm = file.toFormElement<FormIncludedFile>()
            includedForm?.allTopLevelForms ?: emptyList()
        }

        val formsInScope = allTopLevelForms.filter { it.sourceElement in searchScope }
        val allFields = formsInScope.flatMap { it.allFields }

        val provider = L10nFieldDeclarationProvider()
        allFields.forEach {
            if(!processDeclarationsInField(it, provider, consumer)) {
                return false
            }
        }
        return true
    }

    private fun processDeclarationsInField(field: FormField,
                                           declarationProvider: L10nFieldDeclarationProvider,
                                           consumer: Processor<in FormSymbolUsage>): Boolean {
        val nameElement = field.namePropertyValue ?: return true
        field.fieldNameChain.forEach { (textRange, _) ->
            ProgressManager.checkCanceled()
            val declarations = declarationProvider.getDeclarations(nameElement, textRange.startOffset)
            val fieldDeclaration = declarations.find {
                it.symbol.type == FormSymbolType.FIELD
            }
            if(fieldDeclaration?.symbol?.targetName != resolveTarget.targetName) {
                return@forEach
            }
            if(!consumer.process(FormSymbolUsage(fieldDeclaration.symbol, true))) {
                return false
            }
        }
        return true
    }

    override fun processReferences(globalSearchScope: GlobalSearchScope,
                                  consumer: Processor<in FormSymbolUsage>): Boolean {
        val formL10ns = L10nService.findFormL10ns(resolveTarget.project, globalSearchScope)
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