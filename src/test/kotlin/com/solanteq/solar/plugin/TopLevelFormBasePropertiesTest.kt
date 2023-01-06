package com.solanteq.solar.plugin

import com.solanteq.solar.plugin.inspection.InvalidFormModuleDeclarationInspection
import com.solanteq.solar.plugin.inspection.InvalidFormNameDeclarationInspection
import org.junit.jupiter.api.Test

class TopLevelFormBasePropertiesTest : FormTestBase() {

    @Test
    fun `test form name reference`() {
        fixture.createFormAndConfigure("testForm", """
            {
              "name": "<caret>testForm"
            }
        """.trimIndent(), "abc")

        assertReferencedElementNameEquals("testForm.json")
    }

    @Test
    fun `test form module reference`() {
        fixture.createFormAndConfigure("testForm", """
            {
              "module": "<caret>abc"
            }
        """.trimIndent(), "abc")

        assertReferencedElementNameEquals("abc")
    }

    @Test
    fun `test form name rename`() {
        fixture.createFormAndConfigure("testForm", """
            {
              "name": "<caret>testForm"
            }
        """.trimIndent(), "abc")

        fixture.renameElementAtCaretUsingHandler("newName.json")
        assertJsonStringLiteralValueEquals("newName")
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

    @Test
    fun `test invalid form module declaration inspection`() {
        fixture.createFormAndConfigure("testForm", """
            {
              "module": "<error>abcd</error>"
            }
        """.trimIndent(), "abc")

        fixture.enableInspections(InvalidFormModuleDeclarationInspection::class.java)
        fixture.checkHighlighting()
    }

    @Test
    fun `test invalid form name declaration inspection`() {
        fixture.createFormAndConfigure("testForm", """
            {
              "name": "<error>invalidFormName</error>"
            }
        """.trimIndent(), "abc")

        fixture.enableInspections(InvalidFormNameDeclarationInspection::class.java)
        fixture.checkHighlighting()
    }

}