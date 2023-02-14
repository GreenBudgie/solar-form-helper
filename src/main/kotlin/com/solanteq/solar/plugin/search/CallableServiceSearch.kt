package com.solanteq.solar.plugin.search

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.solanteq.solar.plugin.util.*
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.base.util.allScope

object CallableServiceSearch {

    private val CALLABLE_SERVICES_KEY = Key<CachedValue<Map<String, PsiClass>>>("solar.callableServices")
    private val SERVICE_ANNOTATION_CLASS_KEY = Key<CachedValue<PsiClass?>>("solar.serviceAnnotation")

    fun findAllCallableServicesImpl(project: Project): Map<String, PsiClass> {
        return CachedValuesManager.getManager(project).getCachedValue(
            project,
            CALLABLE_SERVICES_KEY,
            {
                CachedValueProvider.Result(
                    doFindAllCallableServicesImpl(project),
                    project.javaKotlinModificationTracker()
                )
            },
            false)
    }

    private fun doFindAllCallableServicesImpl(project: Project): Map<String, PsiClass> {
        val serviceAnnotation = findServiceAnnotation(project) ?: return emptyMap()

        val effectiveScope = project.allScope().restrictedByFileTypes(
            JavaFileType.INSTANCE,
            KotlinFileType.INSTANCE
        )

        val services = AnnotatedElementsSearch.searchPsiClasses(serviceAnnotation, effectiveScope)
            .filter { it.isCallableServiceClassImpl() }
            .sortedBy { it.name }

        val result = mutableMapOf<String, PsiClass>()
        services.forEach {
            val serviceName = it.serviceSolarName ?: return@forEach
            result += serviceName to it
        }
        return result
    }

    private fun findServiceAnnotation(project: Project): PsiClass? {
        return CachedValuesManager.getManager(project).getCachedValue(
            project,
            SERVICE_ANNOTATION_CLASS_KEY,
            {
                val serviceAnnotation = JavaPsiFacade.getInstance(project).findClass(
                    SERVICE_ANNOTATION_FQ_NAME,
                    project.allScope()
                )

                CachedValueProvider.Result(
                    serviceAnnotation,
                    VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
                )
            },
            false)
    }

}