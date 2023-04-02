package com.solanteq.solar.plugin.search

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.solanteq.solar.plugin.index.DropdownIndex
import com.solanteq.solar.plugin.util.DROPDOWN_ANNOTATION_FQ_NAME
import com.solanteq.solar.plugin.util.javaKotlinModificationTracker
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.uast.UFile
import org.jetbrains.uast.toUElementOfType

object DropdownSearch {

    private val DROPDOWNS_KEY = Key<CachedValue<Map<String, PsiClass>>>("solar.dropdowns")

    fun findAllDropdownEnums(project: Project): Map<String, PsiClass> {
        return CachedValuesManager.getManager(project).getCachedValue(
            project,
            DROPDOWNS_KEY,
            {
                CachedValueProvider.Result(
                    doFindAllDropdownEnums(project),
                    project.javaKotlinModificationTracker()
                )
            },
            false
        )
    }

    private fun doFindAllDropdownEnums(project: Project): Map<String, PsiClass> {
        val allPossibleDropdowns = DropdownIndex
            .getAllPossibleFilesWithDropdowns(project)
        val result = mutableMapOf<String, PsiClass>()

        allPossibleDropdowns
            .mapNotNull { it.toPsiFile(project)?.toUElementOfType<UFile>() }
            .flatMap { it.classes }
            .map { it.javaPsi }
            .forEach {
                val fullName = getDropdownFullNameOrNull(project, it) ?: return@forEach
                result += fullName to it
            }

        return result.toSortedMap()
    }

    private fun getDropdownFullNameOrNull(project: Project, psiClass: PsiClass): String? {
        if(!psiClass.isEnum) {
            return null
        }
        val dropdownAnnotation = psiClass.getAnnotation(DROPDOWN_ANNOTATION_FQ_NAME) ?: return null
        val moduleAttribute = dropdownAnnotation.findAttributeValue("module")
        val moduleName = JavaPsiFacade.getInstance(project)
            .constantEvaluationHelper.computeConstantExpression(moduleAttribute) as? String ?: return null
        val dropdownName = psiClass.name?.replaceFirstChar { it.lowercaseChar() } ?: return null
        return "$moduleName.$dropdownName"
    }

}