package com.solanteq.solar.plugin.l10n

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement
import com.solanteq.solar.plugin.file.L10nFileType

class L10nVetoPropertyRename : Condition<PsiElement> {

    override fun value(element: PsiElement?): Boolean {
        if(element !is JsonProperty) return false
        if(element.containingFile?.fileType != L10nFileType) return false
        val parentObject = element.parent as? JsonObject ?: return false
        return parentObject.parent is JsonFile
    }

}