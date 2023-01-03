package com.solanteq.solar.plugin.l10n

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.solanteq.solar.plugin.file.L10nFileType
import com.solanteq.solar.plugin.l10n.field.L10nFieldPsiReferenceProvider
import com.solanteq.solar.plugin.l10n.form.L10nFormPsiReferenceProvider
import com.solanteq.solar.plugin.l10n.module.L10nModulePsiReferenceProvider
import com.solanteq.solar.plugin.util.isPropertyKey

class L10nReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerAllProviders(
            L10nModulePsiReferenceProvider,
            L10nFormPsiReferenceProvider,
            L10nFieldPsiReferenceProvider
        )
    }

    private fun PsiReferenceRegistrar.registerAllProviders(vararg providers: PsiReferenceProvider) {
        providers.forEach {
            registerReferenceProvider(l10nPropertyPattern, it)
        }
    }

    companion object {

        val l10nPropertyPattern =
            PlatformPatterns.psiElement(JsonStringLiteral::class.java).inFile(
                PlatformPatterns.psiFile().withFileType(StandardPatterns.`object`(L10nFileType))
            ).isPropertyKey()

    }

}