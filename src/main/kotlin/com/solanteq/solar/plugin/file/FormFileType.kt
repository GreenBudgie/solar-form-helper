package com.solanteq.solar.plugin.file

import com.intellij.openapi.vfs.VirtualFile
import com.solanteq.solar.plugin.asset.Assets

object FormFileType : AbstractFormFileType() {

    override fun getName() = "AIR Form"

    override fun getDescription() = "SOLAR AIR form"

    override fun getDefaultExtension() = ""

    override fun getIcon() = Assets.FORM_ICON

    override fun getDisplayName() = name

    override fun isMyFileType(file: VirtualFile) =
        file.path.contains("/src/main/resources/config/forms")

}