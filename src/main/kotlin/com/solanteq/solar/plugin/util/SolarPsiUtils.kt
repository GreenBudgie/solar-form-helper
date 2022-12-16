package com.solanteq.solar.plugin.util

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotationMemberValue
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.kotlin.idea.KotlinLanguage

const val SERVICE_ANNOTATION_FQ_NAME = "org.springframework.stereotype.Service"
const val CALLABLE_SERVICE_ANNOTATION_FQ_NAME = "com.solanteq.solar.commons.annotations.CallableService"
const val CALLABLE_ANNOTATION_FQ_NAME = "com.solanteq.solar.commons.annotations.Callable"

fun Project.isSolarProject() =
    JavaPsiFacade.getInstance(this).findClass(
        "com.solanteq.solar.bridge.Adapter",
        GlobalSearchScope.allScope(this)
    ) != null

fun PsiAnnotationMemberValue.evaluateToString(): String? {
    return JavaPsiFacade.getInstance(project)
        .constantEvaluationHelper.computeConstantExpression(this) as? String ?: return null
}

fun Project.uastModificationTracker() =
    PsiModificationTracker.getInstance(this).forLanguages {
        it is KotlinLanguage || it is JavaLanguage
    }