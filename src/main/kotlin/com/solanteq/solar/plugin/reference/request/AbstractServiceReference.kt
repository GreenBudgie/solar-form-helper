package com.solanteq.solar.plugin.reference.request

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.search.UseScopeEnlarger
import com.intellij.psi.search.searches.ReferencesSearch
import org.jetbrains.kotlin.idea.search.allScope
import org.jetbrains.uast.UClass
import org.jetbrains.uast.toUElementOfType

abstract class AbstractServiceReference(
    element: JsonStringLiteral,
    range: TextRange,
    protected val requestData: RequestData
) : PsiReferenceBase<JsonStringLiteral>(element, range, false) {

    override fun resolve(): PsiElement? {
        val exactServiceName =
            requestData.serviceName.replaceFirstChar { it.uppercaseChar() } + "Impl"

        val applicableServiceClasses = PsiShortNamesCache.getInstance(element.project).getClassesByName(
            exactServiceName,
            element.project.allScope()
        ).mapNotNull { it.toUElementOfType<UClass>() }

        val groupDotServiceName = "${requestData.groupName}.${requestData.serviceName}"

        applicableServiceClasses.forEach {
            val containsValidServiceAnnotation = it.uAnnotations.any { annotation ->
                annotation.qualifiedName == "org.springframework.stereotype.Service" &&
                        annotation.findAttributeValue("value")?.evaluate() == groupDotServiceName
            }

            if(!containsValidServiceAnnotation) return@forEach

            return resolveReference(it)
        }
        return null
    }

    abstract fun resolveReference(serviceClass: UClass): PsiElement?

}