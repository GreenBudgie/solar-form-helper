package com.solanteq.solar.plugin.reference.field

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReferenceBase
import com.solanteq.solar.plugin.element.FormField

class FieldReference(
    element: JsonStringLiteral,
    textRange: TextRange,
    private val fieldProperty: FormField.FieldProperty
) : PsiReferenceBase<JsonStringLiteral>(element, textRange) {

    override fun getVariants(): Array<out Any> {
        val fields = fieldProperty.containingClass?.javaPsi?.allFields ?: return emptyArray()
        return fields.map {
            LookupElementBuilder
                .create(it.name)
                .withIcon(it.getIcon(0))
                .withTailText(": TODO")
                .withTypeText("TODO!")
        }.toTypedArray()
    }

    override fun resolve() = fieldProperty.referencedField?.sourcePsi

}