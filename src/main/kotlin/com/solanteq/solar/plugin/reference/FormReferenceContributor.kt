package com.solanteq.solar.plugin.reference

import com.intellij.json.psi.*
import com.intellij.patterns.*
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.element.FormRequest
import com.solanteq.solar.plugin.file.L10nFileType
import com.solanteq.solar.plugin.reference.field.FieldReferenceProvider
import com.solanteq.solar.plugin.reference.form.FormReferenceProvider
import com.solanteq.solar.plugin.reference.formModule.FormModuleReferenceProvider
import com.solanteq.solar.plugin.reference.formName.FormNameReferenceProvider
import com.solanteq.solar.plugin.reference.include.JsonIncludeReferenceProvider
import com.solanteq.solar.plugin.reference.l10n.L10nReferenceProvider
import com.solanteq.solar.plugin.reference.request.RequestReferenceProvider
import com.solanteq.solar.plugin.util.inForm
import com.solanteq.solar.plugin.util.isInsideObjectWithKey
import com.solanteq.solar.plugin.util.isObjectInArrayWithKey
import com.solanteq.solar.plugin.util.isValueWithKey

class FormReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            requestValuePattern(),
            RequestReferenceProvider
        )

        registrar.registerReferenceProvider(
            inForm<JsonStringLiteral>().isValueWithKey("form", "parentForm"),
            FormReferenceProvider
        )

        registrar.registerReferenceProvider(
            inForm<JsonStringLiteral>()
                .isValueWithKey("name")
                .isObjectInArrayWithKey("fields"),
            FieldReferenceProvider
        )

        registrar.registerReferenceProvider(
            inForm<JsonStringLiteral>(),
            JsonIncludeReferenceProvider
        )

        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(JsonStringLiteral::class.java).inFile(
                PlatformPatterns.psiFile().withFileType(StandardPatterns.`object`(L10nFileType))
            ).isPropertyKey(),
            L10nReferenceProvider
        )

        registrar.registerReferenceProvider(
            inForm<JsonStringLiteral>().isValueWithKey("name").isValueAtTopLevelObject(),
            FormNameReferenceProvider
        )

        registrar.registerReferenceProvider(
            inForm<JsonStringLiteral>().isValueWithKey("module").isValueAtTopLevelObject(),
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
                .isValueWithKey(*FormRequest.RequestType.requestLiterals),
            baseInFormPattern
                .isValueWithKey("name")
                .isInsideObjectWithKey(*FormRequest.RequestType.requestLiterals)
        )
    }

    private fun PsiElementPattern.Capture<out JsonStringLiteral>.isPropertyKey() = with(
        object : PatternCondition<JsonStringLiteral>("isPropertyKey") {

            override fun accepts(element: JsonStringLiteral, context: ProcessingContext?) =
                JsonPsiUtil.isPropertyKey(element)

        }
    )

    private fun PsiElementPattern.Capture<out JsonStringLiteral>.isValueAtTopLevelObject() = with(
        object : PatternCondition<JsonStringLiteral>("isAtTopLevelObject") {

            override fun accepts(element: JsonStringLiteral, context: ProcessingContext?): Boolean {
                val property = element.parent as? JsonProperty ?: return false
                val topLevelObject = property.parent as? JsonObject ?: return false
                val jsonFile = topLevelObject.containingFile as? JsonFile ?: return false
                return jsonFile.topLevelValue == topLevelObject
            }

        }
    )

}