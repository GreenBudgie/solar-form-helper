package com.solanteq.solar.plugin.file

import com.intellij.json.JsonLanguage
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.openapi.vfs.VirtualFile
import com.solanteq.solar.plugin.asset.Assets

object IncludedFormFileType : LanguageFileType(JsonLanguage.INSTANCE), FileTypeIdentifiableByVirtualFile {

    override fun getName() = "Included AIR Form"

    override fun getDescription() = "Included SOLAR AIR form"

    override fun getDefaultExtension() = ""

    override fun getIcon() = Assets.INCLUDED_FORM_ICON

    override fun getDisplayName() = name

    override fun isMyFileType(file: VirtualFile) =
        file.path.contains("/src/main/resources/config/includes/forms")

}