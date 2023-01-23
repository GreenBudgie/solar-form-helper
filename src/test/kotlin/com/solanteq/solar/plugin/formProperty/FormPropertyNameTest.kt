package com.solanteq.solar.plugin.formProperty

import com.solanteq.solar.plugin.base.*
import org.junit.jupiter.api.Test

class FormPropertyNameTest : LightPluginTestBase() {

    @Test
    fun `test form name reference`() {
        fixture.createForm(
            "testForm",
            "{}",
            "test"
        )

        fixture.configureByFormText("""
            {
              "form": "test.<caret>testForm"
            }
        """.trimIndent()
        )

        assertReferencedElementNameEquals("testForm.json")
    }

    @Test
    fun `test form name completion`() {
        fixture.createForm(
            "testForm1",
            "{}",
            "test"
        )
        fixture.createForm(
            "testForm2",
            "{}",
            "test"
        )
        fixture.createForm(
            "testForm3",
            "{}",
            "test2"
        )

        fixture.createIncludedForm(
            "includedForm",
            "test",
            "{}"
        )

        fixture.createFormAndConfigure("testForm", """
            {
              "form": "test.<caret>"
            }
        """.trimIndent(), "test3")

        assertCompletionsContainsExact(
            "testForm1",
            "testForm2"
        )
    }

    @Test
    fun `test completions do not contain this form`() {
        fixture.createForm(
            "testForm1",
            "{}",
            "test"
        )

        fixture.createFormAndConfigure("testForm", """
            {
              "form": "test.<caret>"
            }
        """.trimIndent(), "test")

        assertCompletionsContainsExact("testForm1")
    }

    @Test
    fun `test form name rename`() {
        fixture.createForm(
            "testForm",
            "{}",
            "test"
        )

        fixture.configureByFormText("""
            {
              "form": "test.testForm<caret>"
            }
        """.trimIndent())

        fixture.renameElementAtCaretUsingHandler("testForm2.json")
        assertJsonStringLiteralValueEquals("test.testForm2")
    }

}