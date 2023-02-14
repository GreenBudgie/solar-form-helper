package com.solanteq.solar.plugin.reference.request

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.solanteq.solar.plugin.element.FormRequest

abstract class CallableServiceReference(
    element: JsonStringLiteral,
    range: TextRange,
    protected val requestElement: FormRequest?
) : PsiReferenceBase<JsonStringLiteral>(element, range, false) {

    override fun resolve(): PsiElement? {
        val service = requestElement?.serviceFromRequest ?: return null

        return resolveReferenceInService(service)
    }

    protected abstract fun resolveReferenceInService(serviceClass: PsiClass): PsiElement?

}