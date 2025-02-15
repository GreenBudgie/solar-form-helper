package com.solanteq.solar.plugin.l10n

import com.solanteq.solar.plugin.base.JavaPluginTestBase
import com.solanteq.solar.plugin.base.assertCompletionsContainsExact
import com.solanteq.solar.plugin.base.assertJsonStringLiteralValueEquals
import com.solanteq.solar.plugin.base.assertReferencedElementNameEquals
import com.solanteq.solar.plugin.base.assertReferencedSymbolNameEquals
import com.solanteq.solar.plugin.base.configureByRootForms
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class L10nRealFieldTest : JavaPluginTestBase() {

    override fun getTestDataSuffix() = "l10n/realFields"

    @BeforeEach
    fun setup(): Unit = with(fixture) {
        configureByFiles(
            "TestService.kt",
            "TestServiceImpl.kt",
            "DataClass.kt",
            "NestedDataClass.kt",
        )

        configureByRootForms("test", "fieldsForm.json")
    }

    @Test
    fun `test l10n real field reference`() = with(fixture) {
        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.fieldsForm.group1.<caret>realField" to "Field Name!"
        )

        assertReferencedElementNameEquals("realField")
    }

    @Test
    fun `test l10n real field rename from reference`() = with(fixture) {
        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.fieldsForm.group1.<caret>realField" to "Field Name!"
        )

        renameElementAtCaret("renamed")
        assertJsonStringLiteralValueEquals("test.form.fieldsForm.group1.renamed")
    }

    @Test
    fun `test l10n fake field reference when source exists`() = with(fixture) {
        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.fieldsForm.group1.<caret>fakeField" to "Field Name!"
        )

        assertReferencedSymbolNameEquals("fakeField")
    }

    @Test
    fun `test l10n real nested field reference`() = with(fixture) {
        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.fieldsForm.group2.realFieldWithNested.<caret>realNestedField" to "Field Name!"
        )

        assertReferencedElementNameEquals("realNestedField")
    }

    @Test
    fun `test l10n real and fake fields completion`() = with(fixture) {
        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.fieldsForm.group1.<caret>" to "Field Name!"
        )

        assertCompletionsContainsExact("realField", "fakeField")
    }

    @Test
    fun `test l10n real nested field completion first part`() = with(fixture) {
        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.fieldsForm.group2.<caret>" to "Field Name!"
        )

        assertCompletionsContainsExact("realFieldWithNested")
    }

    @Test
    fun `test l10n real nested field completion second part`() = with(fixture) {
        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.fieldsForm.group2.realFieldWithNested.<caret>" to "Field Name!"
        )

        assertCompletionsContainsExact("realNestedField")
    }

}