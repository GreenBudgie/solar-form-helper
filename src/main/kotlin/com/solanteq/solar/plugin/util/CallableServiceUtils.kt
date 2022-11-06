package com.solanteq.solar.plugin.util

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UMethod

fun UMethod.isCallableMethod() = javaPsi.isCallableMethod()

fun PsiMethod.isCallableMethod() =
    findSuperMethods().any {
        it.hasAnnotation("com.solanteq.solar.commons.annotations.Callable")
    }

fun UClass.isCallableServiceInterface() = javaPsi.isCallableServiceInterface()

fun PsiClass.isCallableServiceInterface() =
    isInterface && hasAnnotation("com.solanteq.solar.commons.annotations.CallableService")

fun UClass.isCallableServiceClassImpl() = javaPsi.isCallableServiceClassImpl()

fun PsiClass.isCallableServiceClassImpl() = supers.any { it.isCallableServiceInterface() }

