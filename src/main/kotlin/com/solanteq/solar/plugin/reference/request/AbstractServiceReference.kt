package com.solanteq.solar.plugin.reference.request

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.PsiShortNamesCache
import com.solanteq.solar.plugin.util.SERVICE_ANNOTATION_FQ_NAME
import org.jetbrains.kotlin.idea.search.allScope
import org.jetbrains.uast.UClass
import org.jetbrains.uast.toUElementOfType

abstract class AbstractServiceReference(
    element: JsonStringLiteral,
    range: TextRange,
    protected val requestData: RequestData?
) : PsiReferenceBase<JsonStringLiteral>(element, range, false) {

    override fun resolve(): PsiElement? {
        val service = findService() ?: return null

        return resolveReferenceInService(service)
    }

    protected fun findService(): UClass? {
        requestData ?: return null

        val exactServiceName =
            requestData.serviceName.replaceFirstChar { it.uppercaseChar() } + "Impl"

        val applicableServiceClasses = PsiShortNamesCache.getInstance(element.project).getClassesByName(
            exactServiceName,
            element.project.allScope()
        ).mapNotNull { it.toUElementOfType<UClass>() }

        val groupDotServiceName = "${requestData.groupName}.${requestData.serviceName}"

        return applicableServiceClasses.find {
            it.uAnnotations.any { annotation ->
                annotation.qualifiedName == SERVICE_ANNOTATION_FQ_NAME &&
                        annotation.findAttributeValue("value")?.evaluate() == groupDotServiceName
            }
        }
    }

    protected abstract fun resolveReferenceInService(serviceClass: UClass): PsiElement?

}