package com.solanteq.solar.plugin.inspection

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.JsonElementVisitor
import com.intellij.json.psi.JsonProperty
import com.intellij.psi.PsiReference
import com.solanteq.solar.plugin.element.FormRequest
import com.solanteq.solar.plugin.element.toFormElement
import com.solanteq.solar.plugin.reference.request.ServiceMethodReference
import com.solanteq.solar.plugin.reference.request.ServiceNameReference

class UnresolvedRequestReferenceInspection : FormInspection() {

    override fun buildVisitor(holder: ProblemsHolder) = Visitor(holder)

    class Visitor(private val holder: ProblemsHolder) : JsonElementVisitor() {

        override fun visitProperty(property: JsonProperty) {
            val requestElement = property.toFormElement<FormRequest>() ?: return
            val requestString = requestElement.requestStringElement ?: return
            val references = requestString.references
            val serviceReference = references.find { it is ServiceNameReference }
            if(serviceReference != null && serviceReference.resolve() == null) {
                registerProblem(serviceReference)
                return
            }
            val methodReference = references.find { it is ServiceMethodReference }
            if(methodReference != null && methodReference.resolve() == null) {
                registerProblem(methodReference)
            }
        }

        private fun registerProblem(reference: PsiReference) {
            holder.registerProblem(
                reference,
                ProblemsHolder.unresolvedReferenceMessage(reference),
                ProblemHighlightType.LIKE_UNKNOWN_SYMBOL
            )
        }

    }

}