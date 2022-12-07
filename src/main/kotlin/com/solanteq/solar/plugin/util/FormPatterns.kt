package com.solanteq.solar.plugin.util

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonPsiUtil
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.file.IncludedFormFileType
import com.solanteq.solar.plugin.file.TopLevelFormFileType

/**
 * Constructs a pattern to check if the specified json element is inside the form file
 */
inline fun <reified T : JsonElement> inForm(): PsiElementPattern.Capture<out T> {
    val basePattern = PlatformPatterns.psiElement(T::class.java)
    return basePattern.andOr(
        basePattern.inFile(PlatformPatterns.psiFile().withFileType(StandardPatterns.`object`(TopLevelFormFileType))),
        basePattern.inFile(PlatformPatterns.psiFile().withFileType(StandardPatterns.`object`(IncludedFormFileType)))
    )
}

/**
 * Constructs a pattern to check if the specified json element is inside the top level form file
 */
inline fun <reified T : JsonElement> inTopLevelForm(): PsiElementPattern.Capture<out T> =
    PlatformPatterns
        .psiElement(T::class.java)
        .inFile(PlatformPatterns.psiFile().withFileType(StandardPatterns.`object`(TopLevelFormFileType)))

/**
 * Constructs a pattern to check if the specified json element is inside the included form file
 */
inline fun <reified T : JsonElement> inIncludedForm(): PsiElementPattern.Capture<out T> =
    PlatformPatterns
        .psiElement(T::class.java)
        .inFile(PlatformPatterns.psiFile().withFileType(StandardPatterns.`object`(IncludedFormFileType)))

/**
 * Extends the pattern to check whether the element is a json property value with one of the specified keys.
 *
 * Example:
 *
 * Pattern `isPropertyValueWithKey("request")` passes for value in property:
 * ```
 * "request": "lty.service.find"
 * ```
 */
fun <T : JsonElement> PsiElementPattern.Capture<out T>.isPropertyValueWithKey(vararg applicableKeys: String) =
    withCondition("isPropertyValueWithKey") { it.isPropertyValueWithKey(*applicableKeys) }

/**
 * Extends the pattern to check whether the element is inside a json object
 * that is a value of property with one of specified keys.
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
fun <T : JsonElement> PsiElementPattern.Capture<out T>.isInObjectWithKey(vararg applicableKeys: String) =
    withCondition("isInObjectWithKey") { it.isInObjectWithKey(*applicableKeys) }

/**
 * Extends the pattern to check whether the element is inside a json array
 * that is a value of property with one of specified keys.
 *
 * Example for `isObjectInArrayWithKey("fields")`:
 * ```
 * "fields": [ //false
 *   { //true
 *     "name": "fieldName" //true
 *   }, //true
 *   "jsonInclude", //true
 * ],
 * "boilerplateKey": "boilerplateValue" //false
 * ```
 */
fun <T : JsonElement> PsiElementPattern.Capture<out T>.isInArrayWithKey(vararg applicableKeys: String) =
    withCondition("isInArrayWithKey") { it.isInArrayWithKey(*applicableKeys) }

/**
 * Extends the pattern to check whether the element is located directly at top-level json object.
 *
 * Example:
 * ```
 * { //false (top-level object itself is not considered to be located at top-level json object ._.)
 *   "name": "hi", //true
 *   "module": "test", //true
 *   "someArray": [ //true for property key
 *     { //false
 *       "someKey": "someValue" //false
 *     }
 *   ],
 *   "someObject": { //true for property key
 *
 *   }
 * }
 * ```
 */
fun <T : JsonElement> PsiElementPattern.Capture<out T>.isAtTopLevelObject() =
    withCondition("isAtTopLevelObject") { it.isAtTopLevelObject() }

/**
 * A pattern condition for [JsonPsiUtil.isPropertyKey]
 */
fun <T : JsonElement> PsiElementPattern.Capture<out T>.isPropertyKey() =
    withCondition("isPropertyKey") {
        JsonPsiUtil.isPropertyKey(it)
    }

/**
 * A pattern condition for [JsonPsiUtil.isPropertyValue]
 */
fun <T : JsonElement> PsiElementPattern.Capture<out T>.isPropertyValue() =
    withCondition("isPropertyValue") {
        JsonPsiUtil.isPropertyValue(it)
    }

/**
 * A helper method to implement custom pattern condition
 */
inline fun <T : PsiElement> PsiElementPattern.Capture<out T>.withCondition(
    debugMethodName: String = "customCondition",
    crossinline condition: (element: T) -> Boolean
) = with(
    object : PatternCondition<T>(debugMethodName) {

        override fun accepts(element: T, context: ProcessingContext?) = condition(element)

    }
)