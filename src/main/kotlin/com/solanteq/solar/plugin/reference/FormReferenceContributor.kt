package com.solanteq.solar.plugin.reference

import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.solanteq.solar.plugin.reference.request.RequestReferenceProvider
import com.solanteq.solar.plugin.util.requestValuePattern

class FormReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            requestValuePattern(),
            RequestReferenceProvider
        )

    }
}