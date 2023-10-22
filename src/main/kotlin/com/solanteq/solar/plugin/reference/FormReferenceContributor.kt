package com.solanteq.solar.plugin.reference

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.solanteq.solar.plugin.element.FormField
import com.solanteq.solar.plugin.element.FormRequest
import com.solanteq.solar.plugin.element.base.FormNamedElement
import com.solanteq.solar.plugin.reference.field.FieldReferenceProvider
import com.solanteq.solar.plugin.reference.form.FormReferenceProvider
import com.solanteq.solar.plugin.reference.include.JsonIncludeReferenceProvider
import com.solanteq.solar.plugin.reference.request.RequestReferenceProvider
import com.solanteq.solar.plugin.reference.topLevelFile.FormTopLevelFileReferenceProvider
import com.solanteq.solar.plugin.reference.topLevelModule.FormTopLevelModuleReferenceProvider
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
                .isPropertyValueWithKey(FormNamedElement.NAME_PROPERTY)
                .isInArrayWithKey(FormField.getArrayName()),
            FieldReferenceProvider
        )

        registrar.registerReferenceProvider(
            inForm<JsonStringLiteral>().isJsonIncludeDeclaration(),
            JsonIncludeReferenceProvider
        )

        registrar.registerReferenceProvider(
            inRootForm<JsonStringLiteral>()
                .notJsonIncludeDeclaration()
                .isPropertyValueWithKey(FormNamedElement.NAME_PROPERTY)
                .isAtTopLevelObject(),
            FormTopLevelFileReferenceProvider
        )

        registrar.registerReferenceProvider(
            inRootForm<JsonStringLiteral>()
                .notJsonIncludeDeclaration()
                .isPropertyValueWithKey("module")
                .isAtTopLevelObject(),
            FormTopLevelModuleReferenceProvider
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
                .isPropertyValueWithKey(FormNamedElement.NAME_PROPERTY)
                .isInObjectWithKey(*FormRequest.RequestType.requestLiterals)
        )
    }

}