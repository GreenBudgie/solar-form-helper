package com.solanteq.solar.plugin.util

import com.intellij.json.psi.*
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.util.findParentOfType
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.file.FormFileType
import com.solanteq.solar.plugin.file.IncludedFormFileType

/**
 * Constructs a pattern to check if the specified json element is inside the form file
 */
fun <T : JsonElement> inForm(psiElementClass: Class<out T>): PsiElementPattern.Capture<out T> {
    val basePattern = PlatformPatterns.psiElement(psiElementClass)
    return basePattern.andOr(
        basePattern.inFile(PlatformPatterns.psiFile().withFileType(StandardPatterns.`object`(FormFileType))),
        basePattern.inFile(PlatformPatterns.psiFile().withFileType(StandardPatterns.`object`(IncludedFormFileType)))
    )
}

/**
 * Extends the pattern to check whether the element is a json value with one of the specified keys.
 *
 * Example:
 *
 * Pattern `isValueWithKey("request")` passes for
 * `"request": "lty.service.find"`, where `"lty.service.find"` is a value
 */
fun PsiElementPattern.Capture<out JsonStringLiteral>.isValueWithKey(vararg applicableKeys: String) = with(
    object : PatternCondition<JsonStringLiteral>("isValueWithKey") {
        override fun accepts(element: JsonStringLiteral, context: ProcessingContext?): Boolean {
            if(!JsonPsiUtil.isPropertyValue(element)) return false
            val parentJsonProperty = element.parent as? JsonProperty ?: return false
            return parentJsonProperty.name in applicableKeys
        }
    }
)

/**
 * Extends the pattern to check whether the element is inside a json object with one of specified keys.
 *
 * Example:
 *
 * Consider the following json structure:
 * ```
 * "request": {
 *      "name": "lty.service.findById",
 *      "group": "lty",
 *      "params": [
 *          {
 *              "name": "id",
 *              "value": "id"
 *          }
 *      ]
 * }
 * ```
 * Pattern `isElementInsideObject("request")` will pass for every json element inside `"request"` object,
 * excluding `"name": "id"` and `"value": "id"` in params because they are inside their own unnamed object.
 */
fun PsiElementPattern.Capture<out JsonStringLiteral>.isInsideObjectWithKey(vararg applicableKeys: String) = with(
    object : PatternCondition<JsonStringLiteral>("isInsideObjectWithKey") {
        override fun accepts(element: JsonStringLiteral, context: ProcessingContext?): Boolean {
            val firstJsonObjectParent = element.findParentOfType<JsonObject>() ?: return false
            val jsonObjectProperty = firstJsonObjectParent.parent as? JsonProperty ?: return false
            return jsonObjectProperty.name in applicableKeys
        }
    }
)
















