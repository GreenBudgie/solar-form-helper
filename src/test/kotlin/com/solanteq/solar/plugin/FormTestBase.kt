package com.solanteq.solar.plugin

import com.intellij.jarRepository.RemoteRepositoryDescription
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.psi.PsiSymbolDeclarationProvider
import com.intellij.model.psi.PsiSymbolReferenceHints
import com.intellij.model.psi.PsiSymbolReferenceService
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.psi.PsiNamedElement
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase5
import com.intellij.testFramework.fixtures.MavenDependencyUtil
import com.intellij.testFramework.junit5.RunInEdt
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolDeclaration
import com.solanteq.solar.plugin.symbol.FormSymbolReference
import org.junit.jupiter.api.Assertions

@RunInEdt
abstract class FormTestBase : LightJavaCodeInsightFixtureTestCase5(DEFAULT_DESCRIPTOR) {

    final override fun getTestDataPath() = "src/test/testData/${getTestDataSuffix()}"

    open fun getTestDataSuffix() = ""

    protected fun assertReferencedElementNameEquals(expectedName: String) {
        val reference = fixture.file.findReferenceAt(fixture.caretOffset)
        Assertions.assertNotNull(reference)

        val referencedElement = reference!!.resolve() as? PsiNamedElement

        Assertions.assertNotNull(referencedElement)
        Assertions.assertEquals(expectedName, referencedElement!!.name)
    }

    protected fun assertReferencedSymbolNameEquals(expectedName: String) {
        val reference = getFormSymbolReferenceAtCaret()
        val referencedSymbol = reference.resolveReference().firstOrNull()

        Assertions.assertNotNull(referencedSymbol)
        Assertions.assertEquals(expectedName, referencedSymbol!!.targetName)
    }

    protected fun assertCompletionsContainsExact(
        vararg expectedCompletions: String
    ) {
        val actualCompletions = fixture.completeBasic().map { it.lookupString }
        Assertions.assertEquals(expectedCompletions.size, actualCompletions.size)
        Assertions.assertTrue(
            actualCompletions.containsAll(listOf(*expectedCompletions))
        )
    }

    protected fun assertJsonStringLiteralValueEquals(expectedValue: String) {
        val stringLiteral = getJsonStringLiteralAtCaret()
        Assertions.assertEquals(expectedValue, stringLiteral.value)
    }

    protected fun renameFormSymbolDeclaration(
        declarationProvider: PsiSymbolDeclarationProvider,
        renameTo: String
    ) {
        val symbol = getFormSymbolDeclarationAtCaret(declarationProvider)
        fixture.renameTarget(symbol, renameTo)
    }

    protected fun renameFormSymbolReference(renameTo: String) {
        val reference = getFormSymbolReferenceAtCaret()
        val referencedSymbol = reference.resolveReference().firstOrNull()
        Assertions.assertNotNull(referencedSymbol)
        fixture.renameTarget(referencedSymbol!!, renameTo)
    }

    protected fun getJsonStringLiteralAtCaret(): JsonStringLiteral {
        val stringLiteral = fixture.file.findElementAt(fixture.caretOffset)?.parent as? JsonStringLiteral
        Assertions.assertNotNull(stringLiteral)
        return stringLiteral!!
    }

    protected fun getFormSymbolDeclarationAtCaret(
        declarationProvider: PsiSymbolDeclarationProvider
    ): FormSymbol {
        val elementAtCaret = getJsonStringLiteralAtCaret()
        val elementAbsoluteTextRangeStart = elementAtCaret.textRange.startOffset
        val offset = fixture.caretOffset - elementAbsoluteTextRangeStart
        val declaration = declarationProvider
            .getDeclarations(elementAtCaret, offset)
            .firstOrNull() as? FormSymbolDeclaration
        Assertions.assertNotNull(declaration)
        return declaration!!.symbol
    }

    protected fun getFormSymbolReferenceAtCaret(): FormSymbolReference {
        val elementAtCaret = getJsonStringLiteralAtCaret()
        val elementAbsoluteTextRangeStart = elementAtCaret.textRange.startOffset
        val offset = fixture.caretOffset - elementAbsoluteTextRangeStart

        val reference = PsiSymbolReferenceService.getService().getReferences(
            elementAtCaret,
            PsiSymbolReferenceHints.offsetHint(offset)
        ).firstOrNull() as? FormSymbolReference

        Assertions.assertNotNull(reference)
        return reference!!
    }

    companion object {

        private val DEFAULT_DESCRIPTOR = object : DefaultLightProjectDescriptor() {

            override fun configureModule(module: Module, model: ModifiableRootModel, contentEntry: ContentEntry) {
                MavenDependencyUtil.addFromMaven(model, "org.springframework:spring-context:5.3.23")
                withSolarDependency(model, "com.solanteq.solar:solar-commons:3.3.5.1.RELEASE")
                super.configureModule(module, model, contentEntry)
            }

            private fun withSolarDependency(model: ModifiableRootModel, dependency: String) {
                MavenDependencyUtil.addFromMaven(
                    model,
                    dependency,
                    true,
                    DependencyScope.COMPILE,
                    listOf(
                        RemoteRepositoryDescription(
                            "solar",
                            "SOLAR Repository",
                            "https://karjala.solanteq.com/content/repositories/releases/"
                        )
                    )
                )
            }

            override fun getSdk() = IdeaTestUtil.getMockJdk18()

        }

    }

}