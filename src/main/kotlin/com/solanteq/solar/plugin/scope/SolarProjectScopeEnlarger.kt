package com.solanteq.solar.plugin.scope

import com.intellij.psi.*
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.UseScopeEnlarger
import com.solanteq.solar.plugin.util.*
import org.jetbrains.kotlin.idea.base.util.projectScope

class SolarProjectScopeEnlarger : UseScopeEnlarger() {

    override fun getAdditionalUseScope(element: PsiElement): SearchScope? = with(element) {
        val projectScope = project.projectScope()
        val formsScope = projectScope.restrictedByFormFiles()
        val formsAndLocalizationsScope = projectScope.restrictedByFormAndL10nFiles()
        if(this is PsiClass && hasAnnotation(DROPDOWN_ANNOTATION_FQ_NAME)) {
            return formsScope
        }
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