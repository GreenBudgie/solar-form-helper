package com.solanteq.solar.plugin.reference.request

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

abstract class AbstractServiceReference(
    element: JsonStringLiteral,
    range: TextRange,
    protected val requestData: RequestData
) : PsiReferenceBase<JsonStringLiteral>(element, range, false) {

    override fun resolve(): PsiElement? {
        val facade = JavaPsiFacade.getInstance(element.project)
        val serviceAnnotation = facade.findClass(
            "org.springframework.stereotype.Service",
            GlobalSearchScope.allScope(element.project)
        ) ?: return null

        ReferencesSearch.search(serviceAnnotation).forEach {
            return when(val annotationElement = it.element) {
                is KtNameReferenceExpression -> findKotlinReference(annotationElement)
                is PsiIdentifier -> findJavaReference(annotationElement)
                else -> return@forEach
            } ?: return@forEach
        }

        return null
    }

    abstract fun findKotlinReference(element: KtNameReferenceExpression): PsiElement?

    abstract fun findJavaReference(element: PsiIdentifier): PsiElement?

}