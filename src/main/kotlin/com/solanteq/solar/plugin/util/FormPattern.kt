package com.solanteq.solar.plugin.util

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import com.solanteq.solar.plugin.file.FormFileType
import com.solanteq.solar.plugin.file.IncludedFormFileType

fun <T : PsiElement> inFormFilePattern(psiElementClass: Class<out T>): PsiElementPattern.Capture<out T> {
    val basePattern = PlatformPatterns.psiElement(psiElementClass)
    return basePattern.andOr(
        basePattern.inFile(PlatformPatterns.psiFile().withFileType(StandardPatterns.`object`(FormFileType))),
        basePattern.inFile(PlatformPatterns.psiFile().withFileType(StandardPatterns.`object`(IncludedFormFileType)))
    )
}

