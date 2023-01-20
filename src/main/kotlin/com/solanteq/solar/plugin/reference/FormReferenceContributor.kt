package com.solanteq.solar.plugin.reference

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.solanteq.solar.plugin.element.FormRequest
import com.solanteq.solar.plugin.reference.field.FieldReferenceProvider
import com.solanteq.solar.plugin.reference.form.FormReferenceProvider
import com.solanteq.solar.plugin.reference.include.JsonIncludeReferenceProvider
import com.solanteq.solar.plugin.reference.request.RequestReferenceProvider
import com.solanteq.solar.plugin.reference.rootForm.FormNameReferenceProvider
import com.solanteq.solar.plugin.reference.rootModule.FormModuleReferenceProvider
import com.solanteq.solar.plugin.util.*

class FormReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            requestValuePattern(),
            RequestReferenceProvider
        )

        registrar.registerReferenceProvider(
            inForm<JsonStringLiteral>()
                .notJsonIncludeDeclaration()
                .isPropertyValueWithKey("form", "parentForm", "parametersForm"),
            FormReferenceProvider
        )

        registrar.registerReferenceProvider(
            inForm<JsonStringLiteral>()
                .notJsonIncludeDeclaration()
                .isPropertyValueWithKey("name")
                .isInObjectInArrayWithKey("fields"),
            FieldReferenceProvider
        )

        registrar.registerReferenceProvider(
            inForm<JsonStringLiteral>().isJsonIncludeDeclaration(),
            JsonIncludeReferenceProvider
        )

        registrar.registerReferenceProvider(
            inRootForm<JsonStringLiteral>()
                .notJsonIncludeDeclaration()
                .isPropertyValueWithKey("name")
                .isAtTopLevelObject(),
            FormNameReferenceProvider
        )

        registrar.registerReferenceProvider(
            inRootForm<JsonStringLiteral>()
                .notJsonIncludeDeclaration()
                .isPropertyValueWithKey("module")
                .isAtTopLevelObject(),
            FormModuleReferenceProvider
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
            baseInFormPattern
                .notJsonIncludeDeclaration()
                .isPropertyValueWithKey(*FormRequest.RequestType.requestLiterals),
            baseInFormPattern
                .notJsonIncludeDeclaration()
                .isPropertyValueWithKey("name")
                .isInObjectWithKey(*FormRequest.RequestType.requestLiterals)
        )
    }

}