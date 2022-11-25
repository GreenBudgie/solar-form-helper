package com.solanteq.solar.plugin.reference.form

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.util.findFormByFullName
import org.jetbrains.kotlin.idea.core.util.toPsiFile

object FormReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val jsonStringLiteral = element as JsonStringLiteral
        val referencedForm = findFormByFullName(element.project, jsonStringLiteral.value)?.toPsiFile(element.project)
        return arrayOf(FormReference(element, referencedForm))
    }

}