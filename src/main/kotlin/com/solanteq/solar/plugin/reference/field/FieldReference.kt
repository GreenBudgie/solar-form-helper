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
        val applicableClasses = fieldProperty.applicableClasses
        val fields = applicableClasses.flatMap { it.allFields.toList() }
        return fields
            .distinct()
            .map { field ->
                var lookup = LookupElementBuilder
                    .create(field.name)
                    .withIcon(field.getIcon(0))
                    .withTypeText(field.type.presentableText)
                field.containingClass?.let {
                    lookup = lookup.withTailText(" in ${it.name}")
                }
                return@map lookup
            }.toTypedArray()
    }

    override fun resolve() = fieldProperty.referencedField

}