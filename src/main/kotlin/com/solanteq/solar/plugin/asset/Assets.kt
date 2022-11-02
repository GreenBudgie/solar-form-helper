/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2022 minecraft-dev
 *
 * MIT License
 */

package com.solanteq.solar.plugin.asset

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object Assets {

    val FORM_ICON = loadIcon("/assets/icons/form.png")

    private fun loadIcon(path: String): Icon {
        return IconLoader.getIcon(path, Assets::class.java)
    }

}
