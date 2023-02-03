package com.solanteq.solar.plugin.symbol

import com.intellij.find.usages.api.SearchTarget
import com.intellij.find.usages.api.UsageSearchParameters
import com.intellij.find.usages.api.UsageSearcher
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.refactoring.rename.api.RenameTarget
import com.intellij.refactoring.rename.api.RenameUsageSearchParameters
import com.intellij.refactoring.rename.api.RenameUsageSearcher
import com.solanteq.solar.plugin.util.asList

abstract class FormSymbolRenameUsageSearcher(
    typeToSearch: FormSymbolType
) : FormSymbolUsageSearcherBase(typeToSearch), RenameUsageSearcher {

    override fun collectSearchRequests(parameters: RenameUsageSearchParameters) =
        getSearchRequests(parameters.target, parameters.searchScope)

}

abstract class FormSymbolUsageSearcher(
    typeToSearch: FormSymbolType
) : FormSymbolUsageSearcherBase(typeToSearch), UsageSearcher {

    override fun collectSearchRequests(parameters: UsageSearchParameters) =
        getSearchRequests(parameters.target, parameters.searchScope)

}

abstract class FormSymbolUsageSearcherBase(
    private val typeToSearch: FormSymbolType
) {

    abstract fun getQuery(target: FormSymbol,
                          effectiveScope: SearchScope): FormSymbolUsageSearchQuery

    /**
     * Used to build the scope based on scope that is passed in parameters.
     * For example, you can restrict it by file types.
     */
    abstract fun prepareSearchScope(initialScope: SearchScope): SearchScope

    protected fun getSearchRequests(searchTarget: SearchTarget,
                                    initialScope: SearchScope): Collection<FormSymbolUsageSearchQuery> {
        if(searchTarget !is FormSymbol) return emptyList()
        return getSearchRequests(searchTarget, initialScope)
    }

    protected fun getSearchRequests(renameTarget: RenameTarget,
                                    initialScope: SearchScope): Collection<FormSymbolUsageSearchQuery> {
        if(renameTarget !is FormSymbol) return emptyList()
        return getSearchRequests(renameTarget, initialScope)
    }

    private fun getSearchRequests(symbol: FormSymbol,
                                  initialScope: SearchScope): Collection<FormSymbolUsageSearchQuery> {
        if(symbol.type != typeToSearch) {
            return emptyList()
        }
        val targetFile = symbol.file.originalFile.virtualFile
        val searchInTargetFile = if(targetFile == null) {
            true
        } else {
            targetFile in initialScope
        }
        val modifiedScope = prepareSearchScope(initialScope)
        val effectiveScope = if(searchInTargetFile) {
            modifiedScope.union(GlobalSearchScope.fileScope(symbol.file))
        } else {
            modifiedScope
        }
        return getQuery(symbol, effectiveScope).asList()
    }

}