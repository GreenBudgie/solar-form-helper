package com.solanteq.solar.plugin

import org.junit.jupiter.api.Test

class TopLevelFormBasePropertiesTest : FormTestBase() {

    @Test
    fun `test form name reference`() {
        fixture.createFormAndConfigure("testForm", """
            {
              "name": "<caret>testForm"
            }
        """.trimIndent(), "abc")

        assertReferencedElementName("testForm.json")
    }

    @Test
    fun `test form module reference`() {
        fixture.createFormAndConfigure("testForm", """
            {
              "module": "<caret>abc"
            }
        """.trimIndent(), "abc")

        assertReferencedElementName("abc")
    }

    @Test
    fun `test form name rename`() {
        fixture.createFormAndConfigure("testForm", """
            {
              "name": "<caret>testForm"
            }
        """.trimIndent(), "abc")

        testJsonStringLiteralRename("newName.json", "newName")
    }

    @Test
    fun `test form name completion`() {
        fixture.createFormAndConfigure("testForm", """
            {
              "name": "<caret>"
            }
        """.trimIndent(), "abc")

        assertCompletionsContainsExact("testForm")
    }

    @Test
    fun `test form module completion`() {
        fixture.createFormAndConfigure("testForm", """
            {
              "module": "<caret>"
            }
        """.trimIndent(), "abc")

        assertCompletionsContainsExact("abc")
    }

}