package com.solanteq.solar.plugin.l10n

import com.intellij.json.psi.JsonObject
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture
import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.l10n.generator.L10nGenerator
import com.solanteq.solar.plugin.l10n.generator.L10nPlacement
import org.junit.jupiter.api.Test

class L10nGeneratorTest : LightPluginTestBase() {

    @Test
    fun `generate l10n - empty file`() = with(fixture) {
        val file = createL10nFileWithTextAndConfigure("l10n", "")

        generateAndCheck(
            L10nPlacement.endOfFile(file), """
            {
              "key": "value"
            }
            """.trimIndent()
        )
    }

    @Test
    fun `generate l10n - empty file with top-level object`() = with(fixture) {
        val file = createL10nFileWithTextAndConfigure("l10n", "{}")

        generateAndCheck(
            L10nPlacement.endOfFile(file), """
            {
              "key": "value"
            }
            """.trimIndent()
        )
    }

    @Test
    fun `generate l10n - file with one property, insert at the end`() = with(fixture) {
        val file = createL10nFileWithTextAndConfigure(
            "l10n", """
            {
              "key1": "value1"
            }
            """.trimIndent()
        )

        generateAndCheck(
            L10nPlacement.endOfFile(file), """
            {
              "key1": "value1",
              "key": "value"
            }
            """.trimIndent()
        )
    }

    @Test
    fun `generate l10n - file with one property, insert at the start`() = with(fixture) {
        val file = createL10nFileWithTextAndConfigure(
            "l10n", """
            {
              "key1": "value1"
            }
            """.trimIndent()
        )

        generateAndCheck(
            L10nPlacement.startOfFile(file), """
            {
              "key": "value",
              "key1": "value1"
            }
            """.trimIndent()
        )
    }

    @Test
    fun `generate l10n - file with one property, insert after first`() = with(fixture) {
        val file = createL10nFileWithTextAndConfigure(
            "l10n", """
            {
              "key1": "value1"
            }
        """.trimIndent()
        )

        val topLevelObject = file.topLevelValue as JsonObject
        val firstProperty = topLevelObject.propertyList.first()

        generateAndCheck(
            L10nPlacement.after(file, firstProperty), """
            {
              "key1": "value1",
              "key": "value"
            }
        """.trimIndent()
        )
    }

    @Test
    fun `generate l10n - file with one property, insert before first`() = with(fixture) {
        val file = createL10nFileWithTextAndConfigure(
            "l10n", """
            {
              "key1": "value1"
            }
        """.trimIndent()
        )

        val topLevelObject = file.topLevelValue as JsonObject
        val firstProperty = topLevelObject.propertyList.first()

        generateAndCheck(
            L10nPlacement.before(file, firstProperty), """
            {
              "key": "value",
              "key1": "value1"
            }
        """.trimIndent()
        )
    }

    @Test
    fun `generate l10n - file with two properties, insert at the end`() = with(fixture) {
        val file = createL10nFileWithTextAndConfigure(
            "l10n", """
            {
              "key1": "value1",
              "key2": "value2"
            }
        """.trimIndent()
        )

        generateAndCheck(
            L10nPlacement.endOfFile(file), """
            {
              "key1": "value1",
              "key2": "value2",
              "key": "value"
            }
        """.trimIndent()
        )
    }

    @Test
    fun `generate l10n - file with two properties, insert after first`() = with(fixture) {
        val file = createL10nFileWithTextAndConfigure(
            "l10n", """
            {
              "key1": "value1",
              "key2": "value2"
            }
        """.trimIndent()
        )

        val topLevelObject = file.topLevelValue as JsonObject
        val firstProperty = topLevelObject.propertyList.first()

        generateAndCheck(
            L10nPlacement.after(file, firstProperty), """
            {
              "key1": "value1",
              "key": "value",
              "key2": "value2"
            }
        """.trimIndent()
        )
    }

    @Test
    fun `generate l10n - file with two properties, insert after last`() = with(fixture) {
        val file = createL10nFileWithTextAndConfigure(
            "l10n", """
            {
              "key1": "value1",
              "key2": "value2"
            }
        """.trimIndent()
        )

        val topLevelObject = file.topLevelValue as JsonObject
        val lastProperty = topLevelObject.propertyList.last()

        generateAndCheck(
            L10nPlacement.after(file, lastProperty), """
            {
              "key1": "value1",
              "key2": "value2",
              "key": "value"
            }
        """.trimIndent()
        )
    }

    @Test
    fun `generate l10n - file with two properties, insert before first`() = with(fixture) {
        val file = createL10nFileWithTextAndConfigure(
            "l10n", """
            {
              "key1": "value1",
              "key2": "value2"
            }
        """.trimIndent()
        )

        val topLevelObject = file.topLevelValue as JsonObject
        val firstProperty = topLevelObject.propertyList.first()

        generateAndCheck(
            L10nPlacement.before(file, firstProperty), """
            {
              "key": "value",
              "key1": "value1",
              "key2": "value2"
            }
        """.trimIndent()
        )
    }

    @Test
    fun `generate l10n - file with two properties, insert before last`() = with(fixture) {
        val file = createL10nFileWithTextAndConfigure(
            "l10n", """
            {
              "key1": "value1",
              "key2": "value2"
            }
        """.trimIndent()
        )

        val topLevelObject = file.topLevelValue as JsonObject
        val lastProperty = topLevelObject.propertyList.last()

        generateAndCheck(
            L10nPlacement.before(file, lastProperty), """
            {
              "key1": "value1",
              "key": "value",
              "key2": "value2"
            }
        """.trimIndent()
        )
    }

    private fun JavaCodeInsightTestFixture.generateAndCheck(
        placement: L10nPlacement,
        expectedText: String,
    ) {
        L10nGenerator.generateL10n(
            key = "key",
            value = "value",
            placement
        )

        PsiTestUtil.checkFileStructure(file)
        checkResult(expectedText)
    }

}