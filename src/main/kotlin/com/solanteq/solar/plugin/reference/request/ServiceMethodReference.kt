package com.solanteq.solar.plugin.reference.request

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.solanteq.solar.plugin.element.FormRequest
import com.solanteq.solar.plugin.util.callableMethods
import org.jetbrains.uast.UClass

class ServiceMethodReference(
    element: JsonStringLiteral,
    range: TextRange,
    requestElement: FormRequest?
) : AbstractServiceReference(element, range, requestElement) {

    override fun getVariants(): Array<Any> {
        val service = requestElement?.findServiceFromRequest() ?: return emptyArray()
        return service.callableMethods.toTypedArray()
    }

    override fun resolveReferenceInService(serviceClass: UClass): PsiElement? {
        return requestElement?.findMethodFromRequest()?.sourcePsi
    }

}