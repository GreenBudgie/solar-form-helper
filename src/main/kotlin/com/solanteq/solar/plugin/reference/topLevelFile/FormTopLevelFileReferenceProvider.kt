package com.solanteq.solar.plugin.reference.topLevelFile

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.util.asArray

object FormTopLevelFileReferenceProvider : PsiReferenceProvider()  {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        return FormTopLevelFileReference(element as JsonStringLiteral).asArray()
    }

}