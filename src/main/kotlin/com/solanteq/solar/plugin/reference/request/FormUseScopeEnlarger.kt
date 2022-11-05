package com.solanteq.solar.plugin.reference.request

import com.intellij.psi.PsiElement
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.UseScopeEnlarger
import org.jetbrains.kotlin.idea.search.allScope
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElement

class FormUseScopeEnlarger : UseScopeEnlarger() {

    override fun getAdditionalUseScope(element: PsiElement): SearchScope? {
        if(element.toUElement() is UMethod) return element.project.allScope()
        return null
    }

}