package com.solanteq.solar.plugin.reference.request

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.solanteq.solar.plugin.element.FormRequest
import com.solanteq.solar.plugin.search.DropdownSearch

class DropdownReference(
    element: JsonStringLiteral,
    range: TextRange,
    private val requestElement: FormRequest?,
    val isExplicit: Boolean
) : PsiReferenceBase<JsonStringLiteral>(element, range, true) {

    override fun handleElementRename(newElementName: String): PsiElement {
        return super.handleElementRename(newElementName.replaceFirstChar { it.lowercaseChar() })
    }

    override fun getVariants() = findAllDropdownLookups(element.project).toTypedArray()

    override fun resolve() = requestElement?.referencedDropdown

    private fun findAllDropdownLookups(project: Project): List<LookupElementBuilder> {
        val dropdownMap = DropdownSearch.findAllDropdownEnums(project)
        val lookups = dropdownMap.map { (fullName, psiClass) ->
            LookupElementBuilder
                .create("$fullName.findAll")
                .withIcon(psiClass.getIcon(0))
        }
        return lookups
    }

}