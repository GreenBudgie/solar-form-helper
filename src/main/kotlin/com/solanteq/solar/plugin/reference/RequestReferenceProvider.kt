package com.solanteq.solar.plugin.reference

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.ElementType
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchScopeUtil
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.intellij.util.PsiNavigateUtil
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

object RequestReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        return arrayOf(RequestMethodReference(element as JsonStringLiteral))
    }

    class RequestMethodReference(element: JsonStringLiteral) :
        PsiReferenceBase<JsonStringLiteral>(element), PsiReference {

        override fun resolve(): PsiElement? {
            val requestData = parseRequest(element.value) ?: return null

            val facade = JavaPsiFacade.getInstance(element.project)
            val serviceAnnotation = facade.findClass(
                "org.springframework.stereotype.Service",
                GlobalSearchScope.allScope(element.project)
            ) ?: return null

            ReferencesSearch.search(serviceAnnotation).forEach {
                return when(val annotationElement = it.element) {
                    is KtNameReferenceExpression -> processKotlinAnnotationDeclaration(annotationElement, requestData)
                    is PsiIdentifier -> processJavaAnnotationDeclaration(annotationElement, requestData)
                    else -> return@forEach
                } ?: return@forEach
            }

            return null
        }

        private fun processKotlinAnnotationDeclaration(
            element: KtNameReferenceExpression,
            requestData: RequestData
        ): KtNamedFunction? {
            val annotationEntry = element.getParentOfType<KtAnnotationEntry>(false) ?: return null
            val argumentList = annotationEntry.getChildOfType<KtValueArgumentList>() ?: return null
            val serviceNameArgument = argumentList.arguments.firstOrNull() ?: return null

            if(!serviceNameArgument.text.contains(requestData.serviceName)) return null

            return element.containingKtFile.collectDescendantsOfType<KtNamedFunction>().find {
                it.name == requestData.methodName
            }
        }

        private fun processJavaAnnotationDeclaration(
            element: PsiIdentifier,
            requestData: RequestData
        ): PsiClass? {
            val file = element.containingFile
            return null
        }

    }

    private fun parseRequest(requestString: String): RequestData? {
        val requestSplit = requestString.split(".")
        if(requestSplit.size != 3 || requestSplit.any { it.isEmpty() }) return null
        val (groupName, serviceName, methodName) = requestSplit
        return RequestData("$groupName.$serviceName", methodName)
    }

    private data class RequestData(
        val serviceName: String,
        val methodName: String)

}