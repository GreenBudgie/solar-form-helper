package com.solanteq.solar.plugin.file

import com.intellij.json.JsonLanguage
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.openapi.vfs.VirtualFile
import com.solanteq.solar.plugin.asset.Icons

object L10nFileType : LanguageFileType(JsonLanguage.INSTANCE), FileTypeIdentifiableByVirtualFile {

    override fun getDefaultExtension() = "json"

    override fun getName() = "SOLAR Localization"

    override fun getDescription() = "SOLAR Form localization file"

    override fun getIcon() = Icons.L10N_FILE_ICON

    override fun isMyFileType(file: VirtualFile) =
        file.path.contains("config/l10n")

}