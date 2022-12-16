package com.solanteq.solar.plugin.file

import com.intellij.openapi.vfs.VirtualFile
import com.solanteq.solar.plugin.asset.Icons

object TopLevelFormFileType : AbstractFormFileType() {

    override fun getName() = "AIR Form"

    override fun getDescription() = "SOLAR AIR form"

    override fun getIcon() = Icons.TOP_LEVEL_FORM_ICON

    override fun isMyFileType(file: VirtualFile) =
        file.path.contains("config/forms")

}