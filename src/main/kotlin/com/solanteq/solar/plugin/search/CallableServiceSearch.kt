package com.solanteq.solar.plugin.search

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.solanteq.solar.plugin.util.SERVICE_ANNOTATION_FQ_NAME
import com.solanteq.solar.plugin.util.isCallableServiceClassImpl
import com.solanteq.solar.plugin.util.serviceSolarName
import com.solanteq.solar.plugin.util.uastModificationTracker
import org.jetbrains.kotlin.idea.base.util.allScope

object CallableServiceSearch {

    private val CALLABLE_SERVICES_KEY = Key<CachedValue<List<PsiClass>>>("solar.callableServices")
    private val CALLABLE_SERVICE_NAMES_KEY = Key<CachedValue<List<String>>>("solar.callableServiceNames")

    fun findAllCallableServicesImpl(project: Project): List<PsiClass> {
        return CachedValuesManager.getManager(project).getCachedValue(
            project,
            CALLABLE_SERVICES_KEY,
            {
                val facade = JavaPsiFacade.getInstance(project)
                val serviceAnnotation = facade.findClass(
                    SERVICE_ANNOTATION_FQ_NAME,
                    GlobalSearchScope.allScope(project)
                )

                if(serviceAnnotation == null) {
                    CachedValueProvider.Result(
                        listOf(),
                        project.uastModificationTracker()
                    )
                } else {
                    val result = AnnotatedElementsSearch.searchPsiClasses(
                        serviceAnnotation,
                        project.allScope()
                    ).findAll().filter { it.isCallableServiceClassImpl() }

                    CachedValueProvider.Result(
                        result,
                        project.uastModificationTracker()
                    )
                }
            },
            false)
    }

    fun findAllCallableServiceSolarNames(project: Project): List<String> {
        return CachedValuesManager.getManager(project).getCachedValue(
            project,
            CALLABLE_SERVICE_NAMES_KEY,
            {
                val serviceNames = findAllCallableServicesImpl(project).mapNotNull {
                    it.serviceSolarName
                }

                CachedValueProvider.Result(
                    serviceNames,
                    project.uastModificationTracker()
                )
            },
            false)
    }

}