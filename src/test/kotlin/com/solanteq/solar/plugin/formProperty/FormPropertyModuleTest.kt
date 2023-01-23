package com.solanteq.solar.plugin.formProperty

import com.solanteq.solar.plugin.base.*
import org.junit.jupiter.api.Test

class FormPropertyModuleTest : LightPluginTestBase() {

    @Test
    fun `test form module reference`() {
        fixture.createForm(
            "testForm",
            "{}",
            "test"
        )

        fixture.configureByFormText("""
            {
              "form": "<caret>test.testForm"
            }
        """.trimIndent()
        )

        assertReferencedElementNameEquals("test")
    }

    @Test
    fun `test form module completion`() {
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
            "test3",
            "{}"
        )

        fixture.createFormAndConfigure("testForm", """
            {
              "form": "<caret>"
            }
        """.trimIndent(), "test")

        assertCompletionsContainsExact(
            "test",
            "test2"
        )
    }

    @Test
    fun `test form module completion when form name is already filled in`() {
        fixture.createForm(
            "testForm1",
            "{}",
            "test"
        )

        fixture.createFormAndConfigure("testForm", """
            {
              "form": "<caret>.testForm"
            }
        """.trimIndent(), "test")

        assertCompletionsContainsExact("test")
    }

    @Test
    fun `test form module rename`() {
        fixture.createForm(
            "testForm",
            "{}",
            "test"
        )

        fixture.configureByFormText("""
            {
              "form": "<caret>test.testForm"
            }
        """.trimIndent())

        fixture.renameElementAtCaret("test2")
        assertJsonStringLiteralValueEquals("test2.testForm")
    }

}