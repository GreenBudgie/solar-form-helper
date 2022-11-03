package com.solanteq.solar.plugin.reference.request

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

class ServiceMethodReference(
    element: JsonStringLiteral,
    range: TextRange,
    requestData: RequestData
) : AbstractServiceReference(element, range, requestData) {

    override fun findKotlinReference(element: KtNameReferenceExpression): PsiElement? {
        val annotationEntry = element.getParentOfType<KtAnnotationEntry>(false) ?: return null
        val argumentList = annotationEntry.getChildOfType<KtValueArgumentList>() ?: return null
        val serviceNameArgument = argumentList.arguments.firstOrNull() ?: return null

        if(!serviceNameArgument.text.contains(requestData.serviceName)) return null

        return element.containingKtFile.collectDescendantsOfType<KtNamedFunction>().find {
            it.name == requestData.methodName
        }
    }

    override fun findJavaReference(element: PsiIdentifier): PsiElement? {
        return null
    }

}