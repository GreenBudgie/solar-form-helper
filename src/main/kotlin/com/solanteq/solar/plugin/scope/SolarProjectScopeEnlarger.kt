package com.solanteq.solar.plugin.scope

import com.intellij.psi.*
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.UseScopeEnlarger
import com.solanteq.solar.plugin.search.FormSearch
import com.solanteq.solar.plugin.util.isCallableMethod
import com.solanteq.solar.plugin.util.isCallableServiceClassImpl
import com.solanteq.solar.plugin.util.isForm
import com.solanteq.solar.plugin.util.isFormModuleOrDirectory
import org.jetbrains.kotlin.idea.base.util.projectScope

class SolarProjectScopeEnlarger : UseScopeEnlarger() {

    override fun getAdditionalUseScope(element: PsiElement): SearchScope? = with(element) {
        val projectScope = project.projectScope()
        val formsScope = FormSearch.getFormSearchScope(projectScope)
        val formsAndLocalizationsScope = FormSearch.getFormAndL10nSearchScope(projectScope)
        if(this is PsiClass && isCallableServiceClassImpl()) {
            return formsScope
        }
        if(this is PsiMethod && isCallableMethod()) {
            return formsScope
        }
        if(this is PsiField) {
            return formsAndLocalizationsScope
        }

        if(this is PsiFile && isForm()) {
            return formsAndLocalizationsScope
        }
        if(this is PsiDirectory && isFormModuleOrDirectory()) {
            return formsAndLocalizationsScope
        }
        return null
    }

}