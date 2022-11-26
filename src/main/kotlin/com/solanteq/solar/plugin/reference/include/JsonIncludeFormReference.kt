package com.solanteq.solar.plugin.reference.include

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReferenceBase
import com.solanteq.solar.plugin.asset.Assets
import com.solanteq.solar.plugin.element.FormJsonInclude
import com.solanteq.solar.plugin.util.findIncludedForms
import org.jetbrains.kotlin.idea.core.util.toPsiFile

class JsonIncludeFormReference(
    element: JsonStringLiteral,
    textRange: TextRange,
    private val jsonIncludeElement: FormJsonInclude
) : PsiReferenceBase<JsonStringLiteral>(element, textRange)  {

    override fun getVariants(): Array<LookupElementBuilder> {
        val path = jsonIncludeElement.pathWithoutFormName ?: return emptyArray()
        return findIncludedForms(element.project)
            .filter {
                it.path
                    .substring(0, it.path.length - it.name.length - 1)
                    .endsWith(path)
            }
            .map {
                LookupElementBuilder
                    .create(it.name)
                    .withIcon(Assets.INCLUDED_FORM_ICON)
            }.toTypedArray()
    }

    override fun resolve() = jsonIncludeElement.referencedFormFile?.toPsiFile(element.project)

}