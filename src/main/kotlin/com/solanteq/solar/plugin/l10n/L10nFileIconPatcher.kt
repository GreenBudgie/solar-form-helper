package com.solanteq.solar.plugin.l10n

import com.intellij.ide.FileIconPatcher
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.IconManager
import com.intellij.ui.LayeredIcon
import com.solanteq.solar.plugin.file.L10nFileType
import javax.swing.Icon

class L10nFileIconPatcher : FileIconPatcher {

    override fun patchIcon(baseIcon: Icon, file: VirtualFile, flags: Int, project: Project?): Icon {
        if (file.fileType != L10nFileType) {
            return baseIcon
        }
        val l10nFileDirectory = file.parent ?: return baseIcon
        val locale = L10nLocale.getByDirectoryName(l10nFileDirectory.name) ?: return baseIcon
        return IconManager.getInstance().createLayered(LayeredIcon(baseIcon, locale.icon))
    }

}