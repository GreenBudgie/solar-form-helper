package com.solanteq.solar.plugin.file

import com.intellij.json.JsonFileType
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.openapi.vfs.VirtualFile
import com.solanteq.solar.plugin.asset.Icons

object L10nFileType : JsonFileType(), FileTypeIdentifiableByVirtualFile {

    override fun getName() = "SOLAR Localization"

    override fun getDescription() = "SOLAR localization file"

    override fun getIcon() = Icons.L10N_FILE_ICON

    override fun isMyFileType(file: VirtualFile) =
        file.extension == "json" && file.path.contains("config/l10n")

}