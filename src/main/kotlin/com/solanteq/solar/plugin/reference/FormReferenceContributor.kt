package com.solanteq.solar.plugin.reference

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.containers.toArray
import com.solanteq.solar.plugin.reference.request.RequestReferenceProvider
import com.solanteq.solar.plugin.util.inForm
import com.solanteq.solar.plugin.util.isInsideObjectWithKey
import com.solanteq.solar.plugin.util.isValueWithKey

class FormReferenceContributor : PsiReferenceContributor() {

    private val requestLiterals = arrayOf(
        "request",
        "countRequest",
        "source",
        "save",
        "remove"
    )

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        val basePattern = inForm(JsonStringLiteral::class.java)

        registrar.registerReferenceProvider(
            StandardPatterns.or(
                basePattern.isValueWithKey(*requestLiterals),
                basePattern.isValueWithKey("name").isInsideObjectWithKey(*requestLiterals)
            ),
            RequestReferenceProvider)
    }



}