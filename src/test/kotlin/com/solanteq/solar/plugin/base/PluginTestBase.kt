package com.solanteq.solar.plugin.base

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.psi.PsiSymbolDeclarationProvider
import com.intellij.model.psi.PsiSymbolReferenceHints
import com.intellij.model.psi.PsiSymbolReferenceService
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReference
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.RunInEdt
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolDeclaration
import com.solanteq.solar.plugin.symbol.FormSymbolReference
import org.junit.jupiter.api.Assertions

/**
 * A base class for all form-based test containing many useful methods and assertions within
 */
@RunInEdt
abstract class PluginTestBase {

    abstract val fixture: CodeInsightTestFixture

    val baseTestDataPath get() = "src/test/testData/${getTestDataSuffix()}"

    open fun getTestDataSuffix() = ""

    protected inline fun <reified T : PsiReference> assertNoReferenceAtCaret() {
        val reference = fixture.file.findReferenceAt(fixture.caretOffset)
        Assertions.assertTrue(reference == null || !T::class.isInstance(reference))
    }

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
        val symbol = getFormSymbolAtCaret(declarationProvider)
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
    ): FormSymbolDeclaration? {
        val elementAtCaret = getJsonStringLiteralAtCaret()
        val elementAbsoluteTextRangeStart = elementAtCaret.textRange.startOffset
        val offset = fixture.caretOffset - elementAbsoluteTextRangeStart
        return declarationProvider
            .getDeclarations(elementAtCaret, offset)
            .firstOrNull() as? FormSymbolDeclaration
    }

    protected fun getFormSymbolAtCaret(
        declarationProvider: PsiSymbolDeclarationProvider
    ): FormSymbol {
        val declaration = getFormSymbolDeclarationAtCaret(declarationProvider)
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

}