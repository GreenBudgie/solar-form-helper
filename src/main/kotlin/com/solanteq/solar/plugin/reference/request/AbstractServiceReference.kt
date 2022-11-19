package com.solanteq.solar.plugin.reference.request

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.solanteq.solar.plugin.element.FormRequest
import org.jetbrains.uast.UClass

abstract class AbstractServiceReference(
    element: JsonStringLiteral,
    range: TextRange,
    protected val requestElement: FormRequest?
) : PsiReferenceBase<JsonStringLiteral>(element, range, false) {

    override fun resolve(): PsiElement? {
        val service = requestElement?.findServiceFromRequest() ?: return null

        return resolveReferenceInService(service)
    }

    protected abstract fun resolveReferenceInService(serviceClass: UClass): PsiElement?

}