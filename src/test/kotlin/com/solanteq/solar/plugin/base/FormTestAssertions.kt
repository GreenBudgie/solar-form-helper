package com.solanteq.solar.plugin.base

import com.intellij.find.usages.api.UsageSearcher
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.SearchScope
import com.intellij.refactoring.rename.api.RenameUsageSearcher
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.solanteq.solar.plugin.symbol.FormSymbol
import org.junit.jupiter.api.Assertions.*

inline fun <reified T : PsiReference> CodeInsightTestFixture.assertNoReferenceAtCaret() {
    val reference = file.findReferenceAt(caretOffset)
    assertTrue(reference == null || !T::class.isInstance(reference))
}

fun CodeInsightTestFixture.assertReferencedElementNameEquals(expectedName: String) {
    val reference = file.findReferenceAt(caretOffset)
    assertNotNull(reference)

    val referencedElement = reference!!.resolve() as? PsiNamedElement

    assertNotNull(referencedElement)
    assertEquals(expectedName, referencedElement!!.name)
}

fun CodeInsightTestFixture.assertReferencedSymbolNameEquals(expectedName: String) {
    val reference = getFormSymbolReferenceAtCaret()
    val referencedSymbol = reference.resolveReference().firstOrNull()

    assertNotNull(referencedSymbol)
    assertEquals(expectedName, referencedSymbol!!.targetName)
}

fun CodeInsightTestFixture.assertCompletionsContainsExact(
    vararg expectedCompletions: String
) {
    val expectedCompletionsList = listOf(*expectedCompletions)
    val actualCompletionsList = completeBasic().map { it.lookupString }
    assertIterableEquals(expectedCompletionsList.sorted(), actualCompletionsList.sorted())
}

fun CodeInsightTestFixture.assertJsonStringLiteralValueEquals(expectedValue: String) {
    val stringLiteral = getJsonStringLiteralAtCaret()
    assertEquals(expectedValue, stringLiteral.value)
}

/**
 * Finds usages and rename usages of the [symbol] in the specified [scope]
 * and tests if their lengths are equal to the [expectedSize].
 *
 * Also checks whether all usages have the same text.
 */
fun CodeInsightTestFixture.assertSymbolUsagesAndRenameUsagesSizeEquals(
    symbol: FormSymbol,
    usageSearcher: UsageSearcher,
    renameUsageSearcher: RenameUsageSearcher,
    scope: SearchScope,
    expectedSize: Int
) {
    val usages = findUsagesOfSymbol(symbol, usageSearcher, scope)
    val renameUsages = findRenameUsagesOfSymbol(symbol, renameUsageSearcher, scope)

    assertEquals(expectedSize, usages.size, "Symbol usages differ in length")
    assertEquals(expectedSize, renameUsages.size, "Symbol rename usages differ in length")

    val allUsages = usages + renameUsages
    val doUsagesHaveSameText = allUsages.all {
        val textInRange = it.range.substring(it.file.text)
        textInRange == symbol.targetName
    }
    assertTrue(doUsagesHaveSameText, "The text of some usages or rename usages differ")
}

/**
 * Checks whether line markers are present on specified [lines]
 */
fun CodeInsightTestFixture.assertContainsLineMarkersAtLines(vararg lines: Int) {
    val actualLines = getLineMarkers().keys
    assertTrue(actualLines.containsAll(lines.toList()))
}

/**
 * Checks whether line markers are present on specified [lines] and no more line markers are present in this file
 */
fun CodeInsightTestFixture.assertContainsLineMarkersAtLinesAndNoMore(vararg lines: Int) {
    val expectedLines = lines.sorted()
    val actualLines = getLineMarkers().keys.sorted()
    assertIterableEquals(expectedLines, actualLines)
}