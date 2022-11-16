package com.solanteq.solar.plugin.reference

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.solanteq.solar.plugin.element.FormRequest
import com.solanteq.solar.plugin.reference.form.FormReferenceProvider
import com.solanteq.solar.plugin.reference.request.RequestReferenceProvider
import com.solanteq.solar.plugin.util.*

class FormReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            requestValuePattern(),
            RequestReferenceProvider
        )

        registrar.registerReferenceProvider(
            inForm<JsonStringLiteral>().isValueWithKey("form"),
            FormReferenceProvider
        )

    }

    /**
     * Generates a request value json string literal pattern.
     *
     * In the following example the pattern will select elements named "VALUE":
     * ```
     * {
     *  "request": "VALUE",
     *  "save": {
     *    "name": "VALUE"
     *  }
     * }
     * ```
     */
    private fun requestValuePattern(): ElementPattern<JsonStringLiteral> {
        val baseInFormPattern = inForm<JsonStringLiteral>()

        return StandardPatterns.or(
            baseInFormPattern.isValueWithKey(*FormRequest.requestLiterals),
            baseInFormPattern.isValueWithKey("name").isInsideObjectWithKey(*FormRequest.requestLiterals)
        )
    }

}