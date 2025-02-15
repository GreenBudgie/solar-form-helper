package com.solanteq.solar.plugin.formProperty

import com.solanteq.solar.plugin.base.*
import org.junit.jupiter.api.Test

class FormPropertyNameTest : LightPluginTestBase() {

    @Test
    fun `test form name reference`() = with(fixture) {
        createForm(
            "testForm",
            "test",
            "{}"
        )

        configureByFormText("""
            {
              "form": "test.<caret>testForm"
            }
        """.trimIndent()
        )

        assertReferencedElementNameEquals("testForm.json")
    }

    @Test
    fun `test form name completion`() = with(fixture) {
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
            "test",
            "{}"
        )

        createFormAndConfigure(
            "testForm", "test3", """
                {
                  "form": "test.<caret>"
                }
            """.trimIndent()
        )

        assertCompletionsContainsExact(
            "testForm1",
            "testForm2"
        )
    }

    @Test
    fun `test completions do not contain this form`() = with(fixture) {
        createForm(
            "testForm1",
            "test",
            "{}"
        )

        createFormAndConfigure(
            "testForm", "test", """
                {
                  "form": "test.<caret>"
                }
            """.trimIndent()
        )

        assertCompletionsContainsExact("testForm1")
    }

    @Test
    fun `test form name rename`() = with(fixture) {
        createForm(
            "testForm",
            "test",
            "{}"
        )

        configureByFormText("""
            {
              "form": "test.testForm<caret>"
            }
        """.trimIndent())

        renameElementAtCaretUsingHandler("testForm2.json")
        assertJsonStringLiteralValueEquals("test.testForm2")
    }

}