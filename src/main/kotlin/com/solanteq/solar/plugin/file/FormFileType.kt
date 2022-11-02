package com.solanteq.solar.plugin.file

import com.intellij.json.JsonLanguage
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.openapi.vfs.VirtualFile
import com.solanteq.solar.plugin.asset.Assets

object FormFileType : LanguageFileType(JsonLanguage.INSTANCE), FileTypeIdentifiableByVirtualFile {

    override fun getName() = "AIR Form"

    override fun getDescription() = "SOLAR AIR form"

    override fun getDefaultExtension() = ""

    override fun getIcon() = Assets.FORM_ICON

    override fun isMyFileType(file: VirtualFile) = file.path.contains("/src/main/resources/config/forms")

}