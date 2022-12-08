package com.solanteq.solar.plugin.reference.l10n

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReferenceBase

abstract class L10nReference(
    element: JsonStringLiteral,
    textRange: TextRange
) : PsiReferenceBase<JsonStringLiteral>(element, textRange, false)