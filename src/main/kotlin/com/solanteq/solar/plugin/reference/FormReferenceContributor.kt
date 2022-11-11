package com.solanteq.solar.plugin.reference

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.StandardPatterns
import com.intellij.patterns.uast.injectionHostUExpression
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import com.intellij.util.text.PlaceholderTextRanges
import com.solanteq.solar.plugin.reference.request.RequestData
import com.solanteq.solar.plugin.reference.request.RequestReferenceProvider
import com.solanteq.solar.plugin.util.SERVICE_ANNOTATION_FQ_NAME
import com.solanteq.solar.plugin.util.inForm
import com.solanteq.solar.plugin.util.isInsideObjectWithKey
import com.solanteq.solar.plugin.util.isValueWithKey
import org.jetbrains.uast.UElement

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
            RequestReferenceProvider
        )

//        val injection = injectionHostUExpression()
//
//        registrar.registerUastReferenceProvider(
//            injection.annotationParam(SERVICE_ANNOTATION_FQ_NAME, "value"),
//            TestReferenceProvider
//        )
    }

    object TestReferenceProvider : UastReferenceProvider() {

        override fun getReferencesByElement(element: UElement, context: ProcessingContext): Array<PsiReference> {
            return emptyArray()
        }

    }

}