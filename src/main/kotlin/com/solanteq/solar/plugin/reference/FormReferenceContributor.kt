package com.solanteq.solar.plugin.reference

import com.intellij.json.psi.*
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.file.FormFileType
import com.solanteq.solar.plugin.file.IncludedFormFileType
import com.solanteq.solar.plugin.util.inFormFilePattern

class FormReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        val pattern = inFormFilePattern(JsonStringLiteral::class.java)

        registrar.registerReferenceProvider(pattern.isValueWithKey("request"), RequestReferenceProvider)
    }

    private fun PsiElementPattern.Capture<out JsonStringLiteral>.isValueWithKey(key: String) = with(
        object : PatternCondition<JsonStringLiteral>("isValueWithKey") {
            override fun accepts(element: JsonStringLiteral, context: ProcessingContext?): Boolean {
                if(!JsonPsiUtil.isPropertyValue(element)) return false
                val parentJsonProperty = element.parent as? JsonProperty ?: return false
                return parentJsonProperty.name == key
            }
        }
    )


}