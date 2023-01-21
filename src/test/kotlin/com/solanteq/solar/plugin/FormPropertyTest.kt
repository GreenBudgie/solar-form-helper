package com.solanteq.solar.plugin

import com.solanteq.solar.plugin.base.*
import org.junit.jupiter.api.Test

class FormPropertyTest : LightPluginTestBase() {

    @Test
    fun `test form reference with group`() {
        fixture.createForm(
            "testFormNoGroup",
            "{}"
        )
        fixture.createForm(
            "testFormWithGroup",
            "{}",
            "test"
        )
        fixture.createForm(
            "testFormWithGroup",
            "{}",
            "test2"
        )

        fixture.configureByFormText("""
            {
              "form": "<caret>test.testFormWithGroup"
            }
        """.trimIndent()
        )

        assertReferencedElementNameEquals("testFormWithGroup.json")
    }

    @Test
    fun `test form completion`() {
        fixture.createForm(
            "testFormNoGroup",
            "{}"
        )
        fixture.createForm(
            "testFormWithGroup",
            "{}",
            "test"
        )
        fixture.createForm(
            "testFormWithGroup",
            "{}",
            "test2"
        )

        fixture.createIncludedForm(
            "includedForm",
            "test2",
            "{}"
        )

        fixture.createFormAndConfigure("testForm", """
            {
              "form": "<caret>"
            }
        """.trimIndent(), "test")

        assertCompletionsContainsExact(
            "testFormNoGroup",
            "test.testFormWithGroup",
            "test2.testFormWithGroup"
        )
    }

    @Test
    fun `test form rename`() {
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