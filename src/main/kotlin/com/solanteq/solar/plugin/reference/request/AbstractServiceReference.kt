package com.solanteq.solar.plugin.reference.request

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.PsiShortNamesCache
import org.jetbrains.kotlin.idea.search.allScope
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UAnnotationEx
import org.jetbrains.uast.UClass
import org.jetbrains.uast.toUElementOfType

abstract class AbstractServiceReference(
    element: JsonStringLiteral,
    range: TextRange,
    protected val requestData: RequestData
) : PsiReferenceBase<JsonStringLiteral>(element, range, false) {

    override fun resolve(): PsiElement? {
        val startTime = System.currentTimeMillis()

        val exactServiceName =
            requestData.serviceName.replaceFirstChar { it.uppercaseChar() } + "Impl"

        val applicableServiceClasses = PsiShortNamesCache.getInstance(element.project).getClassesByName(
            exactServiceName,
            element.project.allScope()
        ).mapNotNull { it.toUElementOfType<UClass>() }

        val groupDotServiceName = "${requestData.groupName}.${requestData.serviceName}"

        applicableServiceClasses.forEach {
            val annotation = it.uAnnotations.find { annotation ->
                annotation.qualifiedName == "org.springframework.stereotype.Service" &&
                        annotation.findAttributeValue("value")?.evaluate() == groupDotServiceName
            } ?: return@forEach

            return resolveReference(it, annotation)
        }

        println("Time2: ${System.currentTimeMillis() - startTime}")
        return null
    }

    abstract fun resolveReference(serviceClass: UClass, serviceAnnotation: UAnnotation): PsiElement?

}