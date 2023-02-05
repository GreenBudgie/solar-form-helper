package com.solanteq.solar.plugin.base

import com.intellij.find.usages.api.UsageSearchParameters
import com.intellij.find.usages.api.UsageSearcher
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.psi.PsiSymbolDeclarationProvider
import com.intellij.model.psi.PsiSymbolReferenceHints
import com.intellij.model.psi.PsiSymbolReferenceService
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.SearchScope
import com.intellij.refactoring.rename.api.RenameUsageSearchParameters
import com.intellij.refactoring.rename.api.RenameUsageSearcher
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.RunInEdt
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolDeclaration
import com.solanteq.solar.plugin.symbol.FormSymbolReference
import com.solanteq.solar.plugin.symbol.FormSymbolUsage
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
        val expectedCompletionsList = listOf(*expectedCompletions)
        val actualCompletionsList = fixture.completeBasic().map { it.lookupString }
        Assertions.assertIterableEquals(expectedCompletionsList.sorted(), actualCompletionsList.sorted())
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

    protected fun findUsagesOfSymbol(symbol: FormSymbol,
                                     usageSearcher: UsageSearcher,
                                     scope: SearchScope): List<FormSymbolUsage> {
        val parameters = object : UsageSearchParameters {
            override val searchScope = scope
            override val target = symbol
            override fun areValid() = true
            override fun getProject() = fixture.project
        }

        val queries = usageSearcher.collectSearchRequests(parameters)
        return queries.flatMap { it.findAll() }.filterIsInstance<FormSymbolUsage>()
    }

    protected fun findRenameUsagesOfSymbol(symbol: FormSymbol,
                                           usageSearcher: RenameUsageSearcher,
                                           scope: SearchScope): List<FormSymbolUsage> {
        val parameters = object : RenameUsageSearchParameters {
            override val searchScope = scope
            override val target = symbol
            override fun areValid() = true
            override fun getProject() = fixture.project
        }

        val queries = usageSearcher.collectSearchRequests(parameters)
        return queries.flatMap { it.findAll() }.filterIsInstance<FormSymbolUsage>()
    }

    /**
     * Finds usages and rename usages of the [symbol] in the specified [scope]
     * and tests if their lengths are equal to the [expectedSize].
     *
     * Also checks whether all usages have the same text.
     */
    protected fun assertSymbolUsagesAndRenameUsagesSizeEquals(symbol: FormSymbol,
                                                              usageSearcher: UsageSearcher,
                                                              renameUsageSearcher: RenameUsageSearcher,
                                                              scope: SearchScope,
                                                              expectedSize: Int) {
        val usages = findUsagesOfSymbol(symbol, usageSearcher, scope)
        val renameUsages = findRenameUsagesOfSymbol(symbol, renameUsageSearcher, scope)

        Assertions.assertEquals(expectedSize, usages.size, "Symbol usages differ in length")
        Assertions.assertEquals(expectedSize, renameUsages.size, "Symbol rename usages differ in length")

        val allUsages = usages + renameUsages
        val doUsagesHaveSameText = allUsages.all {
            val textInRange = it.range.substring(it.file.text)
            textInRange == symbol.targetName
        }
        Assertions.assertTrue(doUsagesHaveSameText, "The text of some usages or rename usages differ")
    }

}