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
import com.solanteq.solar.plugin.bundle.SolarBundle
import com.solanteq.solar.plugin.util.FormPsiUtils
import com.solanteq.solar.plugin.util.textRangeWithoutQuotes

class InvalidFormNameDeclarationInspection : FormInspection() {

    override fun buildVisitor(holder: ProblemsHolder) = Visitor(holder)

    class Visitor(private val holder: ProblemsHolder) : JsonElementVisitor() {

        override fun visitProperty(property: JsonProperty) {
            if(!FormPsiUtils.isAtTopLevelObject(property)) return
            if(property.name != "name") return

            val propertyValue = property.value as? JsonStringLiteral ?: return
            val formFileName = property.containingFile?.originalFile?.virtualFile?.nameWithoutExtension ?: return
            val declaredFormName = propertyValue.value

            if(formFileName == declaredFormName) return

            holder.registerProblem(
                propertyValue,
                SolarBundle.message("inspection.message.invalid.form.name.declaration"),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                propertyValue.textRangeWithoutQuotes,
                RenameFormNameDeclarationFix(formFileName)
            )
        }

    }

    class RenameFormNameDeclarationFix(private val formFileName: String) : LocalQuickFix {

        override fun getFamilyName() = SolarBundle.message("intention.family.name.rename.form.name")

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement
            runWriteAction {
                ElementManipulators.handleContentChange(element, formFileName)
            }
        }

    }

}