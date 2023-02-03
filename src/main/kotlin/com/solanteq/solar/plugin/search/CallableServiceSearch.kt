package com.solanteq.solar.plugin.search

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.solanteq.solar.plugin.util.SERVICE_ANNOTATION_FQ_NAME
import com.solanteq.solar.plugin.util.isCallableServiceClassImpl
import com.solanteq.solar.plugin.util.javaKotlinModificationTracker
import org.jetbrains.kotlin.idea.base.util.allScope

object CallableServiceSearch {

    private val CALLABLE_SERVICES_KEY = Key<CachedValue<List<PsiClass>>>("solar.callableServices")

    fun findAllCallableServicesImpl(project: Project): List<PsiClass> {
        return CachedValuesManager.getManager(project).getCachedValue(
            project,
            CALLABLE_SERVICES_KEY,
            {
                val facade = JavaPsiFacade.getInstance(project)
                val serviceAnnotation = facade.findClass(
                    SERVICE_ANNOTATION_FQ_NAME,
                    GlobalSearchScope.allScope(project)
                ) ?: return@getCachedValue CachedValueProvider.Result(
                    emptyList(),
                    project.javaKotlinModificationTracker(),
                    VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
                )

                val result = AnnotatedElementsSearch.searchPsiClasses(
                    serviceAnnotation,
                    project.allScope()
                )
                    .filter { it.isCallableServiceClassImpl() }
                    .sortedBy { it.qualifiedName }

                return@getCachedValue CachedValueProvider.Result(
                    result,
                    project.javaKotlinModificationTracker(),
                    VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
                )
            },
            false)
    }

}