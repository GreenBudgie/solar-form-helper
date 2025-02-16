package com.solanteq.solar.plugin.l10n.action

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.json.psi.JsonElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.psi.PsiElement
import com.solanteq.solar.plugin.element.creator.FormElementFactory

class EditFormL10nAction : PsiElementBaseIntentionAction() {

    override fun getFamilyName() = "Edit localization"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element !is JsonElement) {
            return false
        }
        return FormElementFactory.createLocalizableElement(element) != null
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val formElement = FormElementFactory.createLocalizableElement(element as JsonElement)
            ?: error("Unable to create localization as the element is not a localizable element")
        EditFormL10nDialog(project, formElement).show()
    }

}