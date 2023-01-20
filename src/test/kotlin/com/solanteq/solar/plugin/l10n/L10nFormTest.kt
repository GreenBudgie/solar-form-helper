package com.solanteq.solar.plugin.l10n

import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.base.configureByForms
import com.solanteq.solar.plugin.base.createForm
import org.junit.jupiter.api.Test

class L10nFormTest : LightPluginTestBase() {

    override fun getTestDataSuffix() = "l10n"

    @Test
    fun `test l10n reference to form`() {
        fixture.configureByForms("testForm1.json", module = "test")

        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.<caret>testForm1.randomText" to "Form Name!"
        )

        assertReferencedElementNameEquals("testForm1.json")
    }

    @Test
    fun `test l10n form completion`() {
        fixture.configureByForms(
            "testForm1.json",
            "testForm2.json",
            module = "test"
        )

        fixture.createForm("confusingForm", "{}", "notTest")

        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.<caret>" to "Form Name!"
        )

        assertCompletionsContainsExact("testForm1", "testForm2")
    }

    @Test
    fun `test l10n form rename`() {
        fixture.configureByForms("testForm1.json", module = "test")

        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.<caret>testForm1" to "Form Name!"
        )

        fixture.renameElementAtCaretUsingHandler("renamed.json")
        assertJsonStringLiteralValueEquals("test.form.renamed")
    }

}