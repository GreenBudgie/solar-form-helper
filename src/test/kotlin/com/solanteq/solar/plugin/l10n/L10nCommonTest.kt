package com.solanteq.solar.plugin.l10n

import com.solanteq.solar.plugin.base.*
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.l10n.field.L10nFieldDeclarationProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class L10nCommonTest : LightPluginTestBase() {

    @Test
    fun `test l10n module reference`() = with(fixture) {
        createForm("testForm", "test", "{}")

        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "<caret>test.form.testForm.randomGroup" to "Group Name!"
        )

        assertReferencedElementNameEquals("test")
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "<caret>",
        "<caret>.",
        "<caret>.form.testForm"
    ])
    fun `test l10n module completion`(l10nKey: String) = with(fixture) {
        createForm("testForm", "test", "{}")
        createForm("testForm2", "test", "{}")
        createForm("testForm", "test2", "{}")
        createForm("testForm", "test3", "{}")

        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            l10nKey to "some l10n"
        )

        assertCompletionsContainsExact("test", "test2", "test3")
    }

    // Type

    @ParameterizedTest
    @ValueSource(strings = [
        ".<caret>",
        ".<caret>.",
        "test.<caret>.form.testForm"
    ])
    fun `test l10n type completion`(l10nKey: String) = with(fixture) {
        createForm("testForm", "test", "{}")

        L10nTestUtils.createL10nFileAndConfigure(this, "l10n",
            l10nKey to "some l10n"
        )

        assertCompletionsContainsExact("form", "dd")
    }

    @Test
    fun `test field declaration does not exist in request name inside field`() = with(fixture) {
        createFormAndConfigure(
            "testForm", "test", """
                {
                  "groups": [
                    {
                      "rows": [
                        {
                          "fields": [
                            {
                              "name": "field",
                              "source": {
                                "name": "test.test<caret>Service.findData"
                              }
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()
        )

        val declaration = getFormSymbolDeclarationAtCaret(L10nFieldDeclarationProvider())
        Assertions.assertNull(declaration)
    }

    @Test
    fun `test no extra l10n`() = with(fixture) {
        val form = createFormAndConfigure(
            "testForm", "test", """
                {
                  "name": "testForm",
                  "module": "test",
                  "groups": [
                    {
                      "name": "group"
                    }
                  ]
                }
            """.trimIndent()
        )

        L10nTestUtils.createL10nFile(this, "l10n",
            "test.form.testForm.group" to "Group l10n"
        )

        val rootFormElement = FormRootFile.createFromOrThrow(form)

        Assertions.assertTrue(rootFormElement.getL10nValues().isEmpty())
    }

}