package com.solanteq.solar.plugin.inspection.form

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.JsonElementVisitor
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.project.Project
import com.intellij.psi.ElementManipulators
import com.solanteq.solar.plugin.util.FormPsiUtils
import com.solanteq.solar.plugin.util.textRangeWithoutQuotes

class InvalidFormModuleDeclarationInspection : FormInspection() {

    override fun buildVisitor(holder: ProblemsHolder) = Visitor(holder)

    class Visitor(private val holder: ProblemsHolder) : JsonElementVisitor() {

        override fun visitProperty(property: JsonProperty) {
            if(!FormPsiUtils.isAtTopLevelObject(property)) return
            if(property.name != "module") return

            val propertyValue = property.value as? JsonStringLiteral ?: return
            val realModuleName = property.containingFile?.originalFile?.parent?.name ?: return
            val declaredModuleName = propertyValue.value

            if(realModuleName == declaredModuleName) return

            holder.registerProblem(
                propertyValue,
                "Module name and form file directory name are not the same",
                ProblemHighlightType.ERROR,
                propertyValue.textRangeWithoutQuotes,
                RenameModuleFix(realModuleName)
            )
        }

    }

    class RenameModuleFix(private val realModuleName: String) : LocalQuickFix {

        override fun getFamilyName() = "Rename element"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement
            runWriteAction {
                ElementManipulators.handleContentChange(element, realModuleName)
            }
        }

    }

}