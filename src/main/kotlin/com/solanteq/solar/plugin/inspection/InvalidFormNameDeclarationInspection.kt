package com.solanteq.solar.plugin.inspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.JsonElementVisitor
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.project.Project
import com.intellij.psi.ElementManipulators
import com.solanteq.solar.plugin.file.TopLevelFormFileType
import com.solanteq.solar.plugin.util.isAtTopLevelObject
import com.solanteq.solar.plugin.util.textRangeWithoutQuotes
import org.jetbrains.kotlin.idea.util.application.runWriteAction

class InvalidFormNameDeclarationInspection : FormInspection() {

    override fun buildVisitor(holder: ProblemsHolder) = Visitor(holder)

    class Visitor(private val holder: ProblemsHolder) : JsonElementVisitor() {

        override fun visitProperty(property: JsonProperty) {
            if(property.containingFile.fileType != TopLevelFormFileType) return
            if(!property.isAtTopLevelObject()) return
            if(property.name != "name") return

            val propertyValue = property.value as? JsonStringLiteral ?: return
            val formFileName = property.containingFile?.originalFile?.virtualFile?.nameWithoutExtension ?: return
            val declaredFormName = propertyValue.value

            if(formFileName == declaredFormName) return

            holder.registerProblem(
                propertyValue,
                "Declared form name form file name are not the same",
                ProblemHighlightType.ERROR,
                propertyValue.textRangeWithoutQuotes,
                RenameFormNameDeclarationFix(formFileName)
            )
        }

    }

    class RenameFormNameDeclarationFix(private val formFileName: String) : LocalQuickFix {

        override fun getFamilyName() = "Rename element"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement
            runWriteAction {
                ElementManipulators.handleContentChange(element, formFileName)
            }
        }

    }

}