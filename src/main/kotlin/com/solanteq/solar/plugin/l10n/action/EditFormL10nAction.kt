package com.solanteq.solar.plugin.l10n.action

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.json.psi.JsonObject
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.platform.util.progress.reportProgress
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.findParentOfType
import com.solanteq.solar.plugin.bundle.SolarBundle
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.element.creator.FormElementFactory
import com.solanteq.solar.plugin.l10n.FormL10nEntry
import com.solanteq.solar.plugin.l10n.editor.FormL10nEditor
import com.solanteq.solar.plugin.l10n.editor.L10nEditor

class EditFormL10nAction : PsiElementBaseIntentionAction() {

    init {
        text = SolarBundle.message("intention.edit.localization")
    }

    override fun getFamilyName() = SolarBundle.message("intention.edit.localization")

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo {
        // No preview since changes will be made in l10n files
        return IntentionPreviewInfo.EMPTY
    }

    override fun startInWriteAction(): Boolean {
        return false
    }

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return findParentLocalizableElement(element) != null
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val localizableElement = findParentLocalizableElement(element)
            ?: error("Unable to create localization as the element is not a localizable element")
        return invokeOnLocalizableElement(project, localizableElement)
    }

    fun invokeOnLocalizableElement(project: Project, localizableElement: FormLocalizableElement<*>) {
        val dialog = EditFormL10nDialog(project, localizableElement)
        if (!dialog.showAndGet()) {
            return
        }

        editL10n(project, localizableElement, dialog)
    }

    /**
     * This is a temporary workaround to resolve localizable element: it makes an assumption that all
     * localizable elements are objects that have direct "name" property. It works in most cases,
     * but after introducing more elements this might stop working
     */
    private fun findParentLocalizableElement(element: PsiElement): FormLocalizableElement<*>? {
        val firstParentObject = element.findParentOfType<JsonObject>() ?: return null
        return FormElementFactory.createLocalizableElement(firstParentObject)
    }

    private fun editL10n(project: Project, localizableElement: FormLocalizableElement<*>, dialog: EditFormL10nDialog) {
        executeCommand(project, SolarBundle.message("command.name.edit.localizations")) {
            CommandProcessor.getInstance().markCurrentCommandAsGlobal(project)
            runWithModalProgressBlocking(project, SolarBundle.message("dialog.l10n.edit.editing.l10n")) {
                reportProgress(dialog.l10nData.size) { reporter ->
                    dialog.l10nData.forEach { (l10nEntry, l10nData) ->
                        reporter.itemStep(
                            SolarBundle.message(
                                "dialog.l10n.edit.editing.l10n.entry",
                                l10nEntry.toString()
                            )
                        ) {
                            writeAction { editL10nEntry(l10nEntry, l10nData, localizableElement, dialog) }
                        }
                    }
                }
            }
        }
    }

    private fun editL10nEntry(
        l10nEntry: FormL10nEntry,
        l10nData: EditFormL10nDialog.L10nData,
        formElement: FormLocalizableElement<*>,
        dialog: EditFormL10nDialog,
    ) {
        val status = l10nData.currentStatus
        if (!status.modified) {
            return
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
            return
        }

        val l10n = l10nData.originalL10n ?: error("OK is pressed, l10n is not new, but original l10n is null")
        if (status == EditFormL10nDialog.L10nEntryStatus.MODIFIED) {
            L10nEditor.editL10n(l10n.property, l10nData.valueField.text)
            return
        }

        if (status == EditFormL10nDialog.L10nEntryStatus.DELETED) {
            L10nEditor.deleteL10n(l10n.property)
        }
    }

}