package com.solanteq.solar.plugin.util

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotationMemberValue
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.search.allScope

const val SERVICE_ANNOTATION_FQ_NAME = "org.springframework.stereotype.Service"
const val CALLABLE_SERVICE_ANNOTATION_FQ_NAME = "com.solanteq.solar.commons.annotations.CallableService"
const val CALLABLE_ANNOTATION_FQ_NAME = "com.solanteq.solar.commons.annotations.Callable"

private val CALLABLE_SERVICES_KEY = Key<CachedValue<List<PsiClass>>>("solar.callableServices")
private val CALLABLE_SERVICE_NAMES_KEY = Key<CachedValue<List<String>>>("solar.callableServiceNames")

fun Project.isSolarProject() =
    JavaPsiFacade.getInstance(this).findClass(
        "com.solanteq.solar.bridge.Adapter",
        GlobalSearchScope.allScope(this)
    ) != null

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
                    uastModificationTracker(project)
                )
            } else {
                val result = AnnotatedElementsSearch.searchPsiClasses(
                    serviceAnnotation,
                    project.allScope()
                ).findAll().filter { it.isCallableServiceClassImpl() }

                CachedValueProvider.Result(
                    result,
                    uastModificationTracker(project)
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
                uastModificationTracker(project)
            )
        },
        false)
}

private fun uastModificationTracker(project: Project) =
    PsiModificationTracker.getInstance(project).forLanguages {
        it is KotlinLanguage || it is JavaLanguage
    }

fun PsiAnnotationMemberValue.evaluateToString(): String? {
    return JavaPsiFacade.getInstance(project)
        .constantEvaluationHelper.computeConstantExpression(this) as? String ?: return null
}