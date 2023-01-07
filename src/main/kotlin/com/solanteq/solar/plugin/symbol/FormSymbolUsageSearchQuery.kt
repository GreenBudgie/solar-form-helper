package com.solanteq.solar.plugin.symbol

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.AbstractQuery
import com.intellij.util.Processor

abstract class FormSymbolUsageSearchQuery(
    protected val resolveTarget: FormSymbol,
    protected val searchScope: SearchScope
) : AbstractQuery<FormSymbolUsage>() {

    override fun processResults(consumer: Processor<in FormSymbolUsage>): Boolean {
        var continueProcessing = runReadAction {
            processDeclarations(consumer)
        }
        if(!continueProcessing) return false
        continueProcessing = runReadAction {
            processReferences(getGlobalSearchScope(), consumer)
        }
        return continueProcessing
    }

    /**
     * Used to process [FormSymbol] declarations.
     *
     * @return Whether it makes sense to continue the processing
     */
    protected abstract fun processDeclarations(consumer: Processor<in FormSymbolUsage>): Boolean

    /**
     * Used to process [FormSymbol] references.
     * This method usually processes references in other files, so global search scope is
     * also provided.
     *
     * @return Whether it makes sense to continue the processing
     */
    protected abstract fun processReferences(globalSearchScope: GlobalSearchScope,
                                             consumer: Processor<in FormSymbolUsage>): Boolean

    /**
     * Processes provided references of they resolve to [resolveTarget]
     *
     * @return Whether it makes sense to continue the processing
     */
    protected fun processReferences(references: List<FormSymbolReference>,
                                    consumer: Processor<in FormSymbolUsage>): Boolean {
        references.forEach { reference ->
            ProgressManager.checkCanceled()
            if(!reference.resolvesTo(resolveTarget)) {
                return@forEach
            }
            val symbolUsage = FormSymbolUsage(reference)
            if(!consumer.process(symbolUsage)) {
                return false
            }
        }
        return true
    }

    private fun getGlobalSearchScope() = if(searchScope is GlobalSearchScope) {
        searchScope
    } else {
        searchScope as LocalSearchScope
        GlobalSearchScope.filesScope(resolveTarget.project, searchScope.virtualFiles.toList())
    }

}