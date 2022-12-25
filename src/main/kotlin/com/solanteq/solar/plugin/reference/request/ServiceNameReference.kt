package com.solanteq.solar.plugin.reference.request

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.solanteq.solar.plugin.element.FormRequest
import com.solanteq.solar.plugin.search.CallableServiceSearch
import com.solanteq.solar.plugin.util.serviceSolarName
import com.solanteq.solar.plugin.util.uastModificationTracker
import org.jetbrains.uast.UClass

class ServiceNameReference(
    element: JsonStringLiteral,
    range: TextRange,
    requestElement: FormRequest?
) : CallableServiceReference(element, range, requestElement) {

    override fun handleElementRename(newElementName: String): PsiElement {
        //Preventing rename of service solar name
        return element
    }

    override fun getVariants() = findAllCallableServiceLookups(element.project).toTypedArray()

    override fun resolveReferenceInService(serviceClass: UClass) = serviceClass.sourcePsi

    private fun findAllCallableServiceLookups(project: Project): List<LookupElementBuilder> {
        return CachedValuesManager.getManager(project).getCachedValue(
            project,
            callableServiceLookupsKey,
            {
                val lookups = CallableServiceSearch.findAllCallableServicesImpl(project).mapNotNull {
                    val solarName = it.serviceSolarName ?: return@mapNotNull null
                    LookupElementBuilder
                        .create(solarName)
                        .withIcon(it.getIcon(0))
                }

                CachedValueProvider.Result(
                    lookups,
                    project.uastModificationTracker()
                )
            },
            false)
    }

    companion object {

        private val callableServiceLookupsKey =
            Key<CachedValue<List<LookupElementBuilder>>>("solar.callableServiceLookups")

    }

}