package com.solanteq.solar.plugin.l10n

import com.intellij.ide.FileIconPatcher
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.solanteq.solar.plugin.file.L10nFileType
import javax.swing.Icon

class L10nFileIconPatcher : FileIconPatcher {

    override fun patchIcon(baseIcon: Icon, file: VirtualFile, flags: Int, project: Project?): Icon {
        return L10nLocale.getByFile(file)?.icon ?: baseIcon
    }

}