package com.solanteq.solar.plugin.search

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiClass
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.solanteq.solar.plugin.index.CallableServiceImplIndex
import com.solanteq.solar.plugin.util.isCallableServiceClassImpl
import com.solanteq.solar.plugin.util.javaKotlinModificationTracker
import com.solanteq.solar.plugin.util.serviceSolarName
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.uast.UFile
import org.jetbrains.uast.toUElementOfType

object CallableServiceSearch {

    private val CALLABLE_SERVICES_KEY = Key<CachedValue<Map<String, PsiClass>>>("solar.callableServices")

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
            false
        )
    }

    private fun doFindAllCallableServicesImpl(project: Project): Map<String, PsiClass> {
        val allPossibleCallableServiceImplFiles = CallableServiceImplIndex
            .getAllPossibleFilesWithCallableServices(project)

        val callableServiceClasses = allPossibleCallableServiceImplFiles
            .mapNotNull { it.toPsiFile(project)?.toUElementOfType<UFile>() }
            .flatMap { it.classes }
            .filter { it.isCallableServiceClassImpl() }
            .map { it.javaPsi }
            .sortedBy { it.name }

        val result = mutableMapOf<String, PsiClass>()
        callableServiceClasses.forEach {
            val serviceName = it.serviceSolarName ?: return@forEach
            result += serviceName to it
        }
        return result
    }

}