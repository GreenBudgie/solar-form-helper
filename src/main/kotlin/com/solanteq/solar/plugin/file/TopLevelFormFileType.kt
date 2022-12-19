package com.solanteq.solar.plugin.file

import com.intellij.json.JsonLanguage
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.openapi.vfs.VirtualFile
import com.solanteq.solar.plugin.asset.Icons

object TopLevelFormFileType : LanguageFileType(JsonLanguage.INSTANCE), FileTypeIdentifiableByVirtualFile {

    override fun getName() = "AIR Form"

    override fun getDescription() = "SOLAR AIR form"

    override fun getIcon() = Icons.TOP_LEVEL_FORM_ICON

    override fun isMyFileType(file: VirtualFile) =
        file.path.contains("config/forms")

    override fun getDefaultExtension() = "json"

    override fun getDisplayName() = name

}