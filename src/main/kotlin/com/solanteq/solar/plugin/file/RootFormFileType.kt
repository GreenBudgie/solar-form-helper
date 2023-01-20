package com.solanteq.solar.plugin.file

import com.intellij.json.JsonFileType
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.openapi.vfs.VirtualFile
import com.solanteq.solar.plugin.asset.Icons

object RootFormFileType : JsonFileType(), FileTypeIdentifiableByVirtualFile {

    override fun getName() = "AIR Form"

    override fun getDescription() = "SOLAR AIR form"

    override fun getIcon() = Icons.ROOT_FORM_ICON

    override fun isMyFileType(file: VirtualFile) =
        file.path.contains("config/forms")

    override fun getDisplayName() = name

}