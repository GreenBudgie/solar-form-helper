package com.solanteq.solar.plugin.scope

import com.intellij.psi.*
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.UseScopeEnlarger
import com.solanteq.solar.plugin.util.isForm
import com.solanteq.solar.plugin.util.isFormModuleOrDirectory
import com.solanteq.solar.plugin.util.restrictedByFormAndL10nFiles
import com.solanteq.solar.plugin.util.restrictedByFormFiles
import org.jetbrains.kotlin.idea.base.util.projectScope

class SolarProjectScopeEnlarger : UseScopeEnlarger() {

    override fun getAdditionalUseScope(element: PsiElement): SearchScope? = with(element) {
        if(this is PsiClass || this is PsiMethod) {
            return project.projectScope().restrictedByFormFiles()
        }
        if(this is PsiField) {
            return project.projectScope().restrictedByFormAndL10nFiles()
        }
        if(this is PsiFile && isForm()) {
            return project.projectScope().restrictedByFormAndL10nFiles()
        }
        if(this is PsiDirectory && isFormModuleOrDirectory()) {
            return project.projectScope().restrictedByFormAndL10nFiles()
        }
        return null
    }

}