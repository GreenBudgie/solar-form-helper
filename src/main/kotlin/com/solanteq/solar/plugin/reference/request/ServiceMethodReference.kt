package com.solanteq.solar.plugin.reference.request

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.util.PsiFormatUtil
import com.intellij.psi.util.TypeConversionUtil
import com.solanteq.solar.plugin.element.FormRequest
import com.solanteq.solar.plugin.util.callableMethods

class ServiceMethodReference(
    element: JsonStringLiteral,
    range: TextRange,
    requestElement: FormRequest?
) : CallableServiceReference(element, range, requestElement) {

    override fun getVariants(): Array<Any> {
        val service = requestElement?.serviceFromRequest ?: return emptyArray()
        return service.callableMethods.map { method ->
            var lookup = LookupElementBuilder
                .create(method.name)
                .withIcon(method.getIcon(0))
            getTailText(service, method)?.let { lookup = lookup.withTailText(it) }
            getTypeText(service, method)?.let { lookup = lookup.withTypeText(it) }
            return@map lookup
        }.toTypedArray()
    }

    override fun resolveReferenceInService(serviceClass: PsiClass): PsiElement? {
        return requestElement?.methodFromRequest
    }

    private fun getTailText(service: PsiClass, method: PsiMethod): String? {
        val containingClass = method.containingClass ?: return null
        val substitutor = TypeConversionUtil.getClassSubstitutor(
            containingClass,
            service,
            PsiSubstitutor.EMPTY
        ) ?: return null

        val methodParameters = PsiFormatUtil.formatMethod(
            method,
            substitutor,
            PsiFormatUtil.SHOW_PARAMETERS,
            PsiFormatUtil.SHOW_TYPE + PsiFormatUtil.SHOW_NAME,
            3
        )

        return "$methodParameters in ${containingClass.name}"
    }

    private fun getTypeText(service: PsiClass, method: PsiMethod): String? {
        val returnType = method.returnType ?: return null
        val containingClass = method.containingClass ?: return null
        val substitutor = TypeConversionUtil.getClassSubstitutor(
            containingClass,
            service,
            PsiSubstitutor.EMPTY
        ) ?: return null

        return PsiFormatUtil.formatType(returnType, 0, substitutor)
    }

}