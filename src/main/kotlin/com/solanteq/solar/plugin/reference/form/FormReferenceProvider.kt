package com.solanteq.solar.plugin.reference.form

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.util.RangeSplit
import com.solanteq.solar.plugin.util.asArray

object FormReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val jsonStringLiteral = element as JsonStringLiteral
        val split = RangeSplit.from(jsonStringLiteral)
        val moduleEntry = split[0]
        val nameEntry = split.getOrNull(1)

        val nameReference = nameEntry?.let {
            FormNameReference(jsonStringLiteral, it.range, moduleEntry.text, it.text)
        }
        val referencedForm = nameReference?.resolve()

        val moduleReference = FormModuleReference(jsonStringLiteral, moduleEntry.range, referencedForm)
        return if(nameReference == null) {
            moduleReference.asArray()
        } else {
            arrayOf(moduleReference, nameReference)
        }
    }

}