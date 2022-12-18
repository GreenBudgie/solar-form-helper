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
 * @see FormPsiUtils.isPropertyValueWithKey
 */
fun <T : JsonElement> PsiElementPattern.Capture<out T>.isPropertyValueWithKey(vararg applicableKeys: String) =
    withCondition("isPropertyValueWithKey") {
        FormPsiUtils.isPropertyValueWithKey(it, *applicableKeys)
    }

/**
 * @see FormPsiUtils.isInObjectWithKey
 */
fun <T : JsonElement> PsiElementPattern.Capture<out T>.isInObjectWithKey(vararg applicableKeys: String) =
    withCondition("isInObjectWithKey") {
        FormPsiUtils.isInObjectWithKey(it, *applicableKeys)
    }

/**
 * @see FormPsiUtils.isInArrayWithKey
 */
fun <T : JsonElement> PsiElementPattern.Capture<out T>.isInArrayWithKey(vararg applicableKeys: String) =
    withCondition("isInArrayWithKey") {
        FormPsiUtils.isInArrayWithKey(it, *applicableKeys)
    }

/**
 * @see FormPsiUtils.isAtTopLevelObject
 */
fun <T : JsonElement> PsiElementPattern.Capture<out T>.isAtTopLevelObject() =
    withCondition("isAtTopLevelObject") {
        FormPsiUtils.isAtTopLevelObject(it)
    }

/**
 * A pattern condition for [JsonPsiUtil.isPropertyKey]
 */
fun <T : JsonElement> PsiElementPattern.Capture<out T>.isPropertyKey() =
    withCondition("isPropertyKey") {
        JsonPsiUtil.isPropertyKey(it)
    }

/**
 * @see FormPsiUtils.isPropertyValue
 */
fun <T : JsonElement> PsiElementPattern.Capture<out T>.isPropertyValue() =
    withCondition("isPropertyValue") {
        FormPsiUtils.isPropertyValue(it)
    }

/**
 * A helper method to implement custom pattern conditions
 */
inline fun <T : PsiElement> PsiElementPattern.Capture<out T>.withCondition(
    debugMethodName: String = "customCondition",
    crossinline condition: (element: T) -> Boolean
) = with(
    object : PatternCondition<T>(debugMethodName) {

        override fun accepts(element: T, context: ProcessingContext?) = condition(element)

    }
)