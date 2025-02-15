package com.solanteq.solar.plugin.l10n

import com.solanteq.solar.plugin.base.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class L10nFormTest : LightPluginTestBase() {

    override fun getTestDataSuffix() = "l10n"

    @Test
    fun `test l10n reference to form`() = with(fixture) {
        configureByRootForms("test", "testForm1.json")

        createL10nFileAndConfigure(
            "l10n",
            "test.form.<caret>testForm1.randomText" to "Form Name!"
        )

        assertReferencedElementNameEquals("testForm1.json")
    }

    @Test
    fun `test l10n form completion`() = with(fixture) {
        configureByRootForms(
            "test",
            "testForm1.json",
            "testForm2.json"
        )

        createForm("confusingForm", "notTest", "{}")

        createL10nFileAndConfigure(
            "l10n",
            "test.form.<caret>" to "Form Name!"
        )

        assertCompletionsContainsExact("testForm1", "testForm2")
    }

    @Test
    fun `test l10n form rename`() = with(fixture) {
        configureByRootForms("test", "testForm1.json")

        createL10nFileAndConfigure(
            "l10n",
            "test.form.<caret>testForm1" to "Form Name!"
        )

        renameElementAtCaretUsingHandler("renamed.json")
        assertJsonStringLiteralValueEquals("test.form.renamed")
    }

    @Test
    fun `test l10n line marker for form`() = with(fixture) {
        configureByRootForms("test", "testForm1.json")
        createL10nFile(
            "l10n",
            "test.form.testForm1" to "Form Name!",
            locale = L10nLocale.RU
        )
        createL10nFile(
            "l10n",
            "test.form.testForm1" to "Form Name!",
            locale = L10nLocale.EN
        )

        assertContainsLineMarkersAtLinesAndNoMore(2)
    }

    @Test
    fun `test no l10n line markers for form if not l10ns exist`() = with(fixture) {
        configureByRootForms("test", "testForm1.json")

        assertEquals(0, getLineMarkers().size)
    }

}