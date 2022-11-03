package com.solanteq.solar.plugin.util

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope

fun Project.isSolarProject() =
    JavaPsiFacade.getInstance(this).findClass(
        "com.solanteq.solar.bridge.Adapter",
        GlobalSearchScope.allScope(this)
    ) != null