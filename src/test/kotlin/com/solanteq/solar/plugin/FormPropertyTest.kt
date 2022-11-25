package com.solanteq.solar.plugin

import com.intellij.testFramework.RunsInEdt
import org.junit.jupiter.api.Test

@RunsInEdt
class FormPropertyTest : FormTestBase() {

    override fun getTestDataSuffix() = "form"

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

        assertReferencedElementName("testFormWithGroup.json")
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

        fixture.configureByFormText("""
            {
              "form": "<caret>"
            }
        """.trimIndent()
        )

        assertCompletionsContainsExact(
            "testFormNoGroup",
            "test.testFormWithGroup",
            "test2.testFormWithGroup"
        )
    }

}