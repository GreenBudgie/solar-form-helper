package com.solanteq.solar.plugin.l10n

import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement
import com.solanteq.solar.plugin.file.L10nFileType

class L10nVetoPropertyRename : Condition<PsiElement> {

    override fun value(element: PsiElement?): Boolean {
        if(element !is JsonProperty) return false
        if(element.containingFile?.fileType != L10nFileType) return false
        val propertyKey = element.nameElement as? JsonStringLiteral ?: return false
        return FormL10nChain.isFormL10n(propertyKey)
    }

}