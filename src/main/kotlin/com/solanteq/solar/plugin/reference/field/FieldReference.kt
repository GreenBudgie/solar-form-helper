package com.solanteq.solar.plugin.reference.field

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
        return fieldProperty.containingClass?.javaPsi?.allFields ?: emptyArray()
    }

    override fun resolve() = fieldProperty.referencedField?.sourcePsi

}