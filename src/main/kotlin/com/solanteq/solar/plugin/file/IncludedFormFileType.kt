package com.solanteq.solar.plugin.file

import com.intellij.openapi.vfs.VirtualFile
import com.solanteq.solar.plugin.asset.Assets

object IncludedFormFileType : AbstractFormFileType() {

    override fun getName() = "Included AIR Form"

    override fun getDescription() = "Included SOLAR AIR form"

    override fun getIcon() = Assets.INCLUDED_FORM_ICON

    override fun isMyFileType(file: VirtualFile) =
        file.path.contains("/src/main/resources/config/includes/forms")

}