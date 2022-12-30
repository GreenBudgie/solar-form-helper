package com.solanteq.solar.plugin.file

import com.intellij.json.JsonFileType
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.openapi.vfs.VirtualFile
import com.solanteq.solar.plugin.asset.Icons

object IncludedFormFileType : JsonFileType(), FileTypeIdentifiableByVirtualFile {

    override fun getName() = "Included AIR Form"

    override fun getDescription() = "Included SOLAR AIR form"

    override fun getIcon() = Icons.INCLUDED_FORM_ICON

    override fun isMyFileType(file: VirtualFile) =
        file.path.contains("config/includes/forms")

    override fun getDisplayName() = name

}