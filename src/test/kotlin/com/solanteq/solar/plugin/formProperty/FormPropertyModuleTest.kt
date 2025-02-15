package com.solanteq.solar.plugin.formProperty

import com.solanteq.solar.plugin.base.*
import org.junit.jupiter.api.Test

class FormPropertyModuleTest : LightPluginTestBase() {

    @Test
    fun `test form module reference`() = with(fixture) {
        createForm(
            "testForm",
            "test",
            "{}"
        )

        configureByFormText("""
            {
              "form": "<caret>test.testForm"
            }
        """.trimIndent()
        )

        assertReferencedElementNameEquals("test")
    }

    @Test
    fun `test form module completion`() = with(fixture) {
        createForm(
            "testForm1",
            "test",
            "{}"
        )
        createForm(
            "testForm2",
            "test",
            "{}"
        )
        createForm(
            "testForm3",
            "test2",
            "{}"
        )

        createIncludedForm(
            "includedForm",
            "test3",
            "{}"
        )

        createFormAndConfigure(
            "testForm", "test", """
                {
                  "form": "<caret>"
                }
            """.trimIndent()
        )

        assertCompletionsContainsExact(
            "test",
            "test2"
        )
    }

    @Test
    fun `test form module completion when form name is already filled in`() = with(fixture) {
        createForm(
            "testForm1",
            "test",
            "{}"
        )

        createFormAndConfigure(
            "testForm", "test", """
                {
                  "form": "<caret>.testForm"
                }
            """.trimIndent()
        )

        assertCompletionsContainsExact("test")
    }

    @Test
    fun `test form module rename`() = with(fixture) {
        createForm(
            "testForm",
            "test",
            "{}"
        )

        configureByFormText("""
            {
              "form": "<caret>test.testForm"
            }
        """.trimIndent())

        renameElementAtCaret("test2")
        assertJsonStringLiteralValueEquals("test2.testForm")
    }

}