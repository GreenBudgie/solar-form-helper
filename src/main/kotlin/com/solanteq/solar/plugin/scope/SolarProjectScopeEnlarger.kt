package com.solanteq.solar.plugin.scope

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.UseScopeEnlarger
import com.solanteq.solar.plugin.util.isCallableMethod
import com.solanteq.solar.plugin.util.isCallableServiceClassImpl
import com.solanteq.solar.plugin.util.isForm
import com.solanteq.solar.plugin.util.isSolarProject
import org.jetbrains.kotlin.idea.base.util.projectScope

class SolarProjectScopeEnlarger : UseScopeEnlarger() {

    override fun getAdditionalUseScope(element: PsiElement): SearchScope? = with(element) {
        val projectScope = project.projectScope()
        if(this is PsiClass && isCallableServiceClassImpl()) {
            return projectScope
        }
        if(this is PsiMethod && isCallableMethod()) {
            return projectScope
        }
        if(this is PsiField) {
            return projectScope
        }
        if(this is PsiFile && isForm()) {
            return projectScope
        }
        return null
    }

}