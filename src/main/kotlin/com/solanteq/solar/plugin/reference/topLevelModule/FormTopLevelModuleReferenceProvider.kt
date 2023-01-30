package com.solanteq.solar.plugin.reference.topLevelModule

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.util.asArray

object FormTopLevelModuleReferenceProvider : PsiReferenceProvider()  {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        return FormTopLevelModuleReference(element as JsonStringLiteral).asArray()
    }

}