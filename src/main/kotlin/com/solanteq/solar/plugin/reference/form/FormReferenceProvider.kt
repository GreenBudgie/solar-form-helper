package com.solanteq.solar.plugin.reference.form

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext

object FormReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val jsonStringLiteral = element as JsonStringLiteral
        return arrayOf(FormReference(element, jsonStringLiteral.value))
    }

}