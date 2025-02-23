package com.solanteq.solar.plugin.l10n

import com.intellij.codeInsight.navigation.GotoTargetPresentationProvider
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.vfs.newvfs.VfsPresentationUtil
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import com.solanteq.solar.plugin.bundle.SolarBundle
import com.solanteq.solar.plugin.file.L10nFileType

class L10nTargetPresentationProvider : GotoTargetPresentationProvider {

    override fun getTargetPresentation(element: PsiElement, differentNames: Boolean): TargetPresentation? {
        if (element !is JsonProperty) {
            return null
        }

        val file = element.containingFile ?: return null
        if (file.fileType != L10nFileType) {
            return null
        }

        val l10n = FormL10n.fromElement(element) ?: return null
        val project = file.project
        val virtualFile = file.virtualFile

        return TargetPresentation
            .builder(l10n.value)
            .icon(l10n.locale.icon)
            .containerText(SolarBundle.message("l10n.presentation.container.text", l10n.fullFormName))
            .backgroundColor(VfsPresentationUtil.getFileBackgroundColor(project, virtualFile))
            .presentation()
    }

}