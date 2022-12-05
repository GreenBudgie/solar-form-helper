package com.solanteq.solar.plugin.scope

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.UseScopeEnlarger
import com.solanteq.solar.plugin.util.isCallableMethod
import com.solanteq.solar.plugin.util.isCallableServiceClassImpl
import org.jetbrains.kotlin.idea.base.util.projectScope

class CallableServiceScopeEnlarger : UseScopeEnlarger() {

    override fun getAdditionalUseScope(element: PsiElement): SearchScope? = with(element) {
        if(this is PsiClass && isCallableServiceClassImpl()) {
            return project.projectScope()
        }
        if(this is PsiMethod && isCallableMethod()) {
            return project.projectScope()
        }
        return null
    }

}