package com.solanteq.solar.plugin.l10n

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.ui.IconManager
import com.intellij.ui.LayeredIcon
import com.intellij.ui.LayeredIcon.Companion.layeredIcon
import com.solanteq.solar.plugin.asset.Icons
import com.solanteq.solar.plugin.file.L10nFileType
import javax.swing.Icon

enum class L10nLocale(
    val displayName: String,
    val directoryName: String,
    val icon: Icon,
) {

    EN("en-US", "en-US", layeredIcon(Icons.L10N_EN_US)),
    RU("ru-RU", "ru-RU", layeredIcon(Icons.L10N_RU_RU));

    companion object {

        fun getByDirectoryName(directoryName: String) = entries.find {
            it.directoryName == directoryName
        }

        fun getByFile(file: PsiFile): L10nLocale? {
            if (file.fileType != L10nFileType) {
                return null
            }
            val l10nFileDirectory = file.parent ?: return null
            return getByDirectoryName(l10nFileDirectory.name)
        }

        fun getByFile(file: VirtualFile): L10nLocale? {
            if (file.fileType != L10nFileType) {
                return null
            }
            val l10nFileDirectory = file.parent ?: return null
            return getByDirectoryName(l10nFileDirectory.name)
        }

    }

}

private fun layeredIcon(icon: Icon): Icon {
    return IconManager.getInstance().createLayered(LayeredIcon.create(Icons.L10N_FILE_ICON, icon))
}