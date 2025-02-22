package com.solanteq.solar.plugin.l10n.action

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.json.psi.JsonElement
import com.intellij.openapi.application.runUndoTransparentWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.solanteq.solar.plugin.element.creator.FormElementFactory
import com.solanteq.solar.plugin.l10n.editor.FormL10nEditor
import com.solanteq.solar.plugin.l10n.editor.L10nEditor

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
        val dialog = EditFormL10nDialog(project, formElement)
        if (!dialog.showAndGet()) {
            return
        }

        runUndoTransparentWriteAction {
            dialog.l10nData.forEach { l10nEntry, l10nData ->
                val status = l10nData.currentStatus
                if (!status.modified) {
                    return@forEach
                }
                if (status == EditFormL10nDialog.L10nEntryStatus.NEW) {
                    val placement = FormL10nEditor.findBestPlacement(
                        formElement,
                        l10nEntry,
                        dialog.getFile(l10nData.fileChooserField)
                    ) ?: error("OK is pressed, but not all files are selected")
                    L10nEditor.generateL10n(
                        l10nEntry.key,
                        l10nData.valueField.text,
                        placement
                    )
                    return@forEach
                }
                val l10n = l10nData.originalL10n ?: error("OK is pressed, l10n is not new, but original l10n is null")
                if (status == EditFormL10nDialog.L10nEntryStatus.MODIFIED) {
                    L10nEditor.editL10n(l10n.property, l10nData.valueField.text)
                    return@forEach
                }
                if (status == EditFormL10nDialog.L10nEntryStatus.DELETED) {
                    L10nEditor.deleteL10n(l10n.property)
                    return@forEach
                }
            }
        }
    }

}