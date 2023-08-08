package com.solanteq.solar.plugin.inspection

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.JsonElementVisitor
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiReference
import com.solanteq.solar.plugin.element.FormRequest
import com.solanteq.solar.plugin.element.toFormElement
import com.solanteq.solar.plugin.reference.request.CallableMethodReference
import com.solanteq.solar.plugin.reference.request.CallableServiceReference
import com.solanteq.solar.plugin.reference.request.DropdownReference
import com.solanteq.solar.plugin.search.FormModuleSearch

class UnresolvedRequestReferenceInspection : FormInspection() {

    override fun buildVisitor(holder: ProblemsHolder) = Visitor(holder)

    class Visitor(private val holder: ProblemsHolder) : JsonElementVisitor() {

        override fun visitProperty(property: JsonProperty) {
            val requestElement = property.toFormElement<FormRequest>() ?: return
            val requestString = requestElement.requestStringElement ?: return
            val problemHighlightType = if(isInProject(requestString))
                ProblemHighlightType.LIKE_UNKNOWN_SYMBOL
            else
                ProblemHighlightType.WARNING
            val references = requestString.references
            val serviceReference = references.find { it is CallableServiceReference }
            if(serviceReference != null && serviceReference.resolve() == null) {
                registerProblem(serviceReference, problemHighlightType)
                return
            }
            val methodReference = references.find { it is CallableMethodReference }
            if(methodReference != null && methodReference.resolve() == null) {
                registerProblem(methodReference, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                return
            }
            val dropdownReference = references.find { it is DropdownReference }
            if(dropdownReference is DropdownReference
                && dropdownReference.isExplicit
                && dropdownReference.resolve() == null) {
                registerProblem(dropdownReference, problemHighlightType)
            }
        }

        private fun registerProblem(reference: PsiReference, problemHighlightType: ProblemHighlightType) {
            holder.registerProblem(
                reference,
                ProblemsHolder.unresolvedReferenceMessage(reference),
                problemHighlightType
            )
        }

        /**
         * Checks whether this request refers to the project service/dropdown.
         * This changes the severity of registered problems.
         *
         * For example, if we are in CRM project and cannot find "crm.supportService" - this is an ERROR,
         * because there always should be such a service.
         * But if we cannot find "bo.supportService" - this is a WARNING, because BO might not be included
         * as dependency at all or its sources might not be downloaded.
         */
        private fun isInProject(requestString: JsonStringLiteral): Boolean {
            val projectModuleNames = FormModuleSearch
                .findProjectRootFormModules(requestString.project)
                .map { it.name }
            if(projectModuleNames.isEmpty()) return false
            val requestStringValue = requestString.value
            return projectModuleNames.any { requestStringValue.startsWith(it) }
        }

    }

}