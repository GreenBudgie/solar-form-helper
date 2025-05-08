package com.solanteq.solar.plugin

import com.solanteq.solar.plugin.base.*
import com.solanteq.solar.plugin.inspection.form.InvalidFormModuleDeclarationInspection
import com.solanteq.solar.plugin.inspection.form.InvalidFormNameDeclarationInspection
import org.junit.jupiter.api.Test

class RootFormBasePropertiesTest : LightPluginTestBase() {

    @Test
    fun `test form name reference`() = with(fixture) {
        createFormAndConfigure(
            "testForm", "abc", """
                {
                  "name": "<caret>testForm"
                }
            """.trimIndent()
        )

        assertReferencedElementNameEquals("testForm.json")
    }

    @Test
    fun `test form module reference`() = with(fixture) {
        createFormAndConfigure(
            "testForm", "abc", """
                {
                  "module": "<caret>abc"
                }
            """.trimIndent()
        )

        assertReferencedElementNameEquals("abc")
    }

    @Test
    fun `test form name rename`() = with(fixture) {
        createFormAndConfigure(
            "testForm", "abc", """
                {
                  "name": "<caret>testForm"
                }
            """.trimIndent()
        )

        renameElementAtCaretUsingHandler("newName.json")
        assertJsonStringLiteralValueEquals("newName")
    }

    @Test
    fun `test form name completion`() = with(fixture) {
        createFormAndConfigure(
            "testForm", "abc", """
                {
                  "name": "<caret>"
                }
            """.trimIndent()
        )

        assertCompletionsContainsExact("testForm")
    }

    @Test
    fun `test form module completion`() = with(fixture) {
        createFormAndConfigure(
            "testForm", "abc", """
                {
                  "module": "<caret>"
                }
            """.trimIndent()
        )

        assertCompletionsContainsExact("abc")
    }

    @Test
    fun `test invalid form module declaration inspection`(): Unit = with(fixture) {
        createFormAndConfigure(
            "testForm", "abc", """
                {
                  "module": "<error>abcd</error>"
                }
            """.trimIndent()
        )

        enableInspections(InvalidFormModuleDeclarationInspection::class.java)
        checkHighlighting()
    }

    @Test
    fun `test invalid form name declaration inspection`(): Unit = with(fixture) {
        createFormAndConfigure(
            "testForm", "abc", """
                {
                  "name": "<error>invalidFormName</error>"
                }
            """.trimIndent()
        )

        enableInspections(InvalidFormNameDeclarationInspection::class.java)
        checkHighlighting()
    }

}