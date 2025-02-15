package com.solanteq.solar.plugin.l10n

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture
import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.l10n.generator.L10nGenerator
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class L10nGeneratorTest : LightPluginTestBase() {

    @Test
    fun `generate l10n - empty file`() = with(fixture) {
        val file = createL10nFileAndConfigure("l10n", "")

        generateAndCheck(
            file, """
            {
              "key": "value"
            }
            """.trimIndent()
        )
    }

    @Test
    fun `generate l10n - empty file with top-level object`() = with(fixture) {
        val file = createL10nFileAndConfigure("l10n", "{}")

        generateAndCheck(
            file, """
            {
              "key": "value"
            }
            """.trimIndent()
        )
    }

    @Test
    fun `generate l10n - file with one property, insert at the end`() = with(fixture) {
        val file = createL10nFileAndConfigure(
            "l10n", """
            {
              "key1": "value1"
            }
            """.trimIndent()
        )

        generateAndCheck(
            file,
            placement = L10nGenerator.Placement.endOfFile(),
            expectedText = """
            {
              "key1": "value1",
              "key": "value"
            }
            """.trimIndent()
        )
    }

    @Test
    fun `generate l10n - file with one property, insert at the start`() = with(fixture) {
        val file = createL10nFileAndConfigure(
            "l10n", """
            {
              "key1": "value1"
            }
            """.trimIndent()
        )

        generateAndCheck(
            file,
            placement = L10nGenerator.Placement.startOfFile(),
            expectedText = """
            {
              "key": "value",
              "key1": "value1"
            }
            """.trimIndent()
        )
    }

    @Test
    fun `generate l10n - file with one property, insert after first`() = with(fixture) {
        val file = createL10nFileAndConfigure(
            "l10n", """
            {
              "key1": "value1"
            }
        """.trimIndent()
        )

        val topLevelObject = file.topLevelValue as JsonObject
        val firstProperty = topLevelObject.propertyList.first()

        generateAndCheck(
            file,
            placement = L10nGenerator.Placement.after(firstProperty),
            expectedText = """
            {
              "key1": "value1",
              "key": "value"
            }
        """.trimIndent()
        )
    }

    @Test
    fun `generate l10n - file with one property, insert before first`() = with(fixture) {
        val file = createL10nFileAndConfigure(
            "l10n", """
            {
              "key1": "value1"
            }
        """.trimIndent()
        )

        val topLevelObject = file.topLevelValue as JsonObject
        val firstProperty = topLevelObject.propertyList.first()

        generateAndCheck(
            file,
            placement = L10nGenerator.Placement.before(firstProperty),
            expectedText = """
            {
              "key": "value",
              "key1": "value1"
            }
        """.trimIndent()
        )
    }

    @Test
    fun `generate l10n - file with two properties, insert at the end`() = with(fixture) {
        val file = createL10nFileAndConfigure(
            "l10n", """
            {
              "key1": "value1",
              "key2": "value2"
            }
        """.trimIndent()
        )

        generateAndCheck(
            file, """
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
        val file = createL10nFileAndConfigure(
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
            file,
            placement = L10nGenerator.Placement.after(firstProperty),
            expectedText = """
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
        val file = createL10nFileAndConfigure(
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
            file,
            placement = L10nGenerator.Placement.after(lastProperty),
            expectedText = """
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
        val file = createL10nFileAndConfigure(
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
            file,
            placement = L10nGenerator.Placement.before(firstProperty),
            expectedText = """
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
        val file = createL10nFileAndConfigure(
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
            file,
            placement = L10nGenerator.Placement.before(lastProperty),
            expectedText = """
            {
              "key1": "value1",
              "key": "value",
              "key2": "value2"
            }
        """.trimIndent()
        )
    }

    private fun JavaCodeInsightTestFixture.generateAndCheck(
        file: JsonFile,
        expectedText: String,
        placement: L10nGenerator.Placement = L10nGenerator.Placement.endOfFile()
    ) {
        L10nGenerator.generateL10n(
            key = "key",
            value = "value",
            file,
            placement
        )

        PsiTestUtil.checkFileStructure(file)
        checkResult(expectedText)
    }

}