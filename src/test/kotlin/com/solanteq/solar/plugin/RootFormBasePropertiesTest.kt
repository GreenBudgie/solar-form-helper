package com.solanteq.solar.plugin

import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.base.createFormAndConfigure
import com.solanteq.solar.plugin.inspection.InvalidFormModuleDeclarationInspection
import com.solanteq.solar.plugin.inspection.InvalidFormNameDeclarationInspection
import org.junit.jupiter.api.Test

class RootFormBasePropertiesTest : LightPluginTestBase() {

    @Test
    fun `test form name reference`() {
        fixture.createFormAndConfigure(
            "testForm", "abc", """
                {
                  "name": "<caret>testForm"
                }
            """.trimIndent()
        )

        assertReferencedElementNameEquals("testForm.json")
    }

    @Test
    fun `test form module reference`() {
        fixture.createFormAndConfigure(
            "testForm", "abc", """
                {
                  "module": "<caret>abc"
                }
            """.trimIndent()
        )

        assertReferencedElementNameEquals("abc")
    }

    @Test
    fun `test form name rename`() {
        fixture.createFormAndConfigure(
            "testForm", "abc", """
                {
                  "name": "<caret>testForm"
                }
            """.trimIndent()
        )

        fixture.renameElementAtCaretUsingHandler("newName.json")
        assertJsonStringLiteralValueEquals("newName")
    }

    @Test
    fun `test form name completion`() {
        fixture.createFormAndConfigure(
            "testForm", "abc", """
                {
                  "name": "<caret>"
                }
            """.trimIndent()
        )

        assertCompletionsContainsExact("testForm")
    }

    @Test
    fun `test form module completion`() {
        fixture.createFormAndConfigure(
            "testForm", "abc", """
                {
                  "module": "<caret>"
                }
            """.trimIndent()
        )

        assertCompletionsContainsExact("abc")
    }

    @Test
    fun `test invalid form module declaration inspection`() {
        fixture.createFormAndConfigure(
            "testForm", "abc", """
                {
                  "module": "<error>abcd</error>"
                }
            """.trimIndent()
        )

        fixture.enableInspections(InvalidFormModuleDeclarationInspection::class.java)
        fixture.checkHighlighting()
    }

    @Test
    fun `test invalid form name declaration inspection`() {
        fixture.createFormAndConfigure(
            "testForm", "abc", """
                {
                  "name": "<error>invalidFormName</error>"
                }
            """.trimIndent()
        )

        fixture.enableInspections(InvalidFormNameDeclarationInspection::class.java)
        fixture.checkHighlighting()
    }

}