package com.solanteq.solar.plugin.l10n

import com.solanteq.solar.plugin.base.JavaPluginTestBase
import com.solanteq.solar.plugin.base.configureByForms
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class L10nRealFieldTest : JavaPluginTestBase() {

    override fun getTestDataSuffix() = "l10n/realFields"

    @BeforeEach
    fun setup() {
        fixture.configureByFiles(
            "TestService.kt",
            "TestServiceImpl.kt",
            "DataClass.kt",
            "NestedDataClass.kt",
        )

        fixture.configureByForms("fieldsForm.json", module = "test")
    }

    @Test
    fun `test l10n real field reference`() {
        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.fieldsForm.group1.<caret>realField" to "Field Name!"
        )

        assertReferencedElementNameEquals("realField")
    }

    @Test
    fun `test l10n real field rename from reference`() {
        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.fieldsForm.group1.<caret>realField" to "Field Name!"
        )

        fixture.renameElementAtCaret("renamed")
        assertJsonStringLiteralValueEquals("test.form.fieldsForm.group1.renamed")
    }

    @Test
    fun `test l10n fake field reference when source exists`() {
        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.fieldsForm.group1.<caret>fakeField" to "Field Name!"
        )

        assertReferencedSymbolNameEquals("fakeField")
    }

    @Test
    fun `test l10n real nested field reference`() {
        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.fieldsForm.group2.realFieldWithNested.<caret>realNestedField" to "Field Name!"
        )

        assertReferencedElementNameEquals("realNestedField")
    }

    @Test
    fun `test l10n real and fake fields completion`() {
        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.fieldsForm.group1.<caret>" to "Field Name!"
        )

        assertCompletionsContainsExact("realField", "fakeField")
    }

    @Test
    fun `test l10n real nested field completion first part`() {
        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.fieldsForm.group2.<caret>" to "Field Name!"
        )

        assertCompletionsContainsExact("realFieldWithNested")
    }

    @Test
    fun `test l10n real nested field completion second part`() {
        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.fieldsForm.group2.realFieldWithNested.<caret>" to "Field Name!"
        )

        assertCompletionsContainsExact("realNestedField")
    }

}