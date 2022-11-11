package com.solanteq.solar.plugin.reference.request

import com.intellij.json.findUsages.JsonWordScanner
import com.intellij.lang.HelpID
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiAnnotationMemberValue
import com.intellij.psi.PsiElement

class ServiceNameFindUsagesProvider : FindUsagesProvider {

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        val annotationValue = psiElement as? PsiAnnotationMemberValue ?: return false
        return true
    }

    override fun getHelpId(psiElement: PsiElement) = HelpID.FIND_OTHER_USAGES

    override fun getType(element: PsiElement) = ""

    override fun getDescriptiveName(element: PsiElement) = "sample"

    override fun getNodeText(element: PsiElement, useFullName: Boolean) = ""

    override fun getWordsScanner(): WordsScanner {
        return JsonWordScanner()
    }

}