package com.solanteq.solar.plugin.l10n

import com.intellij.psi.search.GlobalSearchScope
import com.solanteq.solar.plugin.base.*
import com.solanteq.solar.plugin.l10n.field.L10nFieldDeclarationProvider
import com.solanteq.solar.plugin.l10n.field.L10nFieldRenameUsageSearcher
import com.solanteq.solar.plugin.l10n.field.L10nFieldUsageSearcher
import org.jetbrains.kotlin.idea.base.util.projectScope
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class L10nFakeFieldTest : LightPluginTestBase() {

    override fun getTestDataSuffix() = "l10n"

    @Test
    fun `test l10n reference to fake field`() = with(fixture) {
        configureByRootForms("test", "testForm1.json")

        createL10nFileAndConfigure(
            "l10n",
            "test.form.testForm1.group1.<caret>field1" to "Field Name!"
        )

        assertReferencedSymbolNameEquals("field1")
    }

    @Test
    fun `test l10n reference to nested fake field`() = with(fixture) {
        configureByRootForms("test", "testForm1.json")

        createL10nFileAndConfigure(
            "l10n",
            "test.form.testForm1.group2.field.<caret>nestedField.randomText" to "Field Name!"
        )

        assertReferencedSymbolNameEquals("nestedField")
    }

    @Test
    fun `test l10n fake field rename from reference`() = with(fixture) {
        configureByRootForms("test", "testForm1.json")

        createL10nFileAndConfigure(
            "l10n",
            "test.form.testForm1.group1.<caret>field1" to "Field Name!"
        )

        val reference = getFormSymbolReferenceAtCaret()
        val referencedSymbol = reference.resolveReference().firstOrNull()
        Assertions.assertNotNull(referencedSymbol)
        renameTarget(referencedSymbol!!, "renamed")
        assertJsonStringLiteralValueEquals("test.form.testForm1.group1.renamed")
    }

    @Test
    fun `test l10n fake field rename from declaration`() = with(fixture) {
        val l10nFile = createL10nFile(
            "l10n",
            "test.form.form.group.fakeField.field1" to "field1",
            "test.form.form.group.fakeField.field2" to "field2",
        )

        createFormAndConfigure(
            "form", "test", """
                {
                  "groups": [
                    {
                      "name": "group",
                      "rows": [
                        {
                          "fields": [
                            {
                              "name": "<caret>fakeField.field1"
                            },
                            {
                              "name": "fakeField.field2"
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()
        )

        val expectedFormText = """
            {
              "groups": [
                {
                  "name": "group",
                  "rows": [
                    {
                      "fields": [
                        {
                          "name": "renamed.field1"
                        },
                        {
                          "name": "renamed.field2"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val expectedL10nText = generateL10nFileText(
            "test.form.form.group.renamed.field1" to "field1",
            "test.form.form.group.renamed.field2" to "field2",
        )

        renameFormSymbolDeclaration(L10nFieldDeclarationProvider(), "renamed")
        checkResult(expectedFormText)

        openFileInEditor(l10nFile.virtualFile)
        checkResult(expectedL10nText)
    }

    @Test
    fun `test l10n fake field completion`() = with(fixture) {
        configureByRootForms("test", "testForm1.json")

        createL10nFileAndConfigure(
            "l10n",
            "test.form.testForm1.group1.<caret>" to "Field Name!"
        )

        assertCompletionsContainsExact("field1", "field2")
    }

    @Test
    fun `test l10n fake nested field first part completion`() = with(fixture) {
        configureByRootForms("test", "testForm1.json")

        createL10nFileAndConfigure(
            "l10n",
            "test.form.testForm1.group2.<caret>" to "Field Name!"
        )

        assertCompletionsContainsExact("field")
    }

    @Test
    fun `test l10n fake nested field second part completion`() = with(fixture) {
        configureByRootForms("test", "testForm1.json")

        createL10nFileAndConfigure(
            "l10n",
            "test.form.testForm1.group2.field.<caret>" to "Nested Field Name!"
        )

        assertCompletionsContainsExact("nestedField")
    }

    @Test
    fun `test l10n reference to fake field in included form`() = with(fixture) {
        createForm(
            "rootForm", "test", """
                {
                  "groups": "json://includes/forms/test/includedFormGroups.json"
                }
            """.trimIndent()
        )

        createIncludedForm(
            "includedFormGroups", "test", """
            [
              {
                "name": "groupName"
                "rows": [
                  {
                    "fields": "json://includes/forms/test/includedFormFields.json"
                  }
                ]
              }
            ]
        """.trimIndent()
        )

        createIncludedForm(
            "includedFormFields", "test", """
            [
              {
                "name": "fieldName"
              }
            ]
        """.trimIndent()
        )

        createL10nFileAndConfigure(
            "l10n",
            "test.form.rootForm.groupName.<caret>fieldName" to "Field name"
        )

        assertReferencedSymbolNameEquals("fieldName")
    }

    @Test
    fun `test l10n fake field rename in included form`() = with(fixture) {
        val formTextBefore = """
            {
              "name": "groupName"
              "rows": [
                {
                  "fields": [
                    {
                      "name": "fieldName"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()
        val formTextAfter = """
            {
              "name": "groupName"
              "rows": [
                {
                  "fields": [
                    {
                      "name": "renamed"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        createForm(
            "rootForm", "test", """
                {
                  "groups": [
                    "json://includes/forms/test/includedForm.json"
                  ]
                }
            """.trimIndent()
        )

        val form = createIncludedForm("includedForm", "test", formTextBefore)

        val l10nTextAfter = generateL10nFileText(
            "test.form.rootForm.groupName.renamed" to "Field name"
        )

        val l10n = createL10nFileAndConfigure(
            "l10n",
            "test.form.rootForm.groupName.<caret>fieldName" to "Field name"
        )

        renameFormSymbolReference("renamed")
        Assertions.assertEquals(formTextAfter, form.text)
        Assertions.assertEquals(l10nTextAfter, l10n.text)
    }

    @Test
    fun `test find usages in project scope`() = with(fixture) {
        configureByRootForms("test", "testForm1.json")

        createL10nFile(
            "l10n_2",
            "test.form.testForm1.group1.field1" to "Field Name!",
            "test.form.testForm1.group1.field1.randomText" to "Field Name!"
        )

        createL10nFileAndConfigure(
            "l10n",
            "test.form.testForm1.group1.<caret>field1" to "Field Name!"
        )

        val symbol = getFormSymbolReferenceAtCaret().resolveReference().first()

        assertSymbolUsagesAndRenameUsagesSizeEquals(
            symbol,
            L10nFieldUsageSearcher(),
            L10nFieldRenameUsageSearcher(),
            project.projectScope(),
            expectedSize = 4
        )
    }

    @Test
    fun `test find usages in file scope`() = with(fixture) {
        configureByRootForms("test", "testForm1.json")

        createL10nFile(
            "l10n_2",
            "test.form.testForm1.group1.field1" to "Field Name!"
        )

        val file = createL10nFileAndConfigure(
            "l10n",
            "test.form.testForm1.group1.field1" to "Field Name!",
            "test.form.testForm1.group1.<caret>field1.randomText" to "Field Name!"
        )

        val symbol = getFormSymbolReferenceAtCaret().resolveReference().first()

        assertSymbolUsagesAndRenameUsagesSizeEquals(
            symbol,
            L10nFieldUsageSearcher(),
            L10nFieldRenameUsageSearcher(),
            GlobalSearchScope.fileScope(file),
            expectedSize = 2
        )
    }

    @Test
    fun `test find usages in project scope for the same field in different included forms`() = with(fixture) {
        createForm(
            "rootForm", "test", """
                {
                  "groups": [
                    {
                      "name": "group1"
                      "rows": [
                        {
                          "fields": [
                            {
                              "name": "field1.field2"
                            },
                            "json://includes/forms/test/field.json",
                          ]
                        },
                        "json://includes/forms/test/row.json"
                      ]
                    },
                    "json://includes/forms/test2/group.json"
                  ]
                }
            """.trimIndent()
        )

        createIncludedForm(
            "field", "test", """
            {
              "name": "field1.field2.field3"
            }
        """.trimIndent()
        )

        createIncludedForm(
            "row", "test", """
            {
              "fields": [
                {
                  "name": "field1.field2.text..random."
                }
              ]
            }
        """.trimIndent()
        )

        createIncludedForm(
            "group", "test2", """
            {
              "name": "group2",
              "rows": [
                {
                  "fields": [
                    {
                      "name": "field1.field2.field3"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()
        )

        createL10nFile(
            "l10n",
            "test.form.rootForm.group2.field1.field2.field3" to "Field Name!",
            "test.form.rootForm.group1.field1.field2.randomText" to "Field Name!"
        )

        createL10nFileAndConfigure(
            "l10n_2",
            "test.form.rootForm.group1.field1.<caret>field2" to "Field Name!",
        )

        val symbol = getFormSymbolReferenceAtCaret().resolveReference().first()

        assertSymbolUsagesAndRenameUsagesSizeEquals(
            symbol,
            L10nFieldUsageSearcher(),
            L10nFieldRenameUsageSearcher(),
            project.projectScope(),
            expectedSize = 6
        )
    }

    @Test
    fun `test all declarations are found for fake field at the same nesting level`() = with(fixture) {
        createFormAndConfigure(
            "rootForm", "test", """
            {
              "groups": [
                {
                  "name": "group"
                  "rows": [
                    {
                      "fields": [
                        {
                          "name": "field.<caret>field2"
                        },
                        {
                          "name": "field.field2."
                        }
                      ]
                    }
                  ]
                },
                { 
                  "name": "group2",
                  "rows": [
                    {
                      "fields": [
                        {
                          "name": "field.field2.field3"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()
        )

        val symbol = getFormSymbolAtCaret(L10nFieldDeclarationProvider())

        assertSymbolUsagesAndRenameUsagesSizeEquals(
            symbol,
            L10nFieldUsageSearcher(),
            L10nFieldRenameUsageSearcher(),
            project.projectScope(),
            expectedSize = 3
        )
    }

    @Test
    fun `test no extra declarations are found for fake field at the same nesting level`() = with(fixture) {
        createFormAndConfigure(
            "rootForm", "test", """
            {
              "groups": [
                {
                  "name": "group"
                  "rows": [
                    {
                      "fields": [
                        {
                          "name": "field.<caret>field2"
                        },
                        {
                          "name": "anotherField.field2"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()
        )

        val symbol = getFormSymbolAtCaret(L10nFieldDeclarationProvider())

        assertSymbolUsagesAndRenameUsagesSizeEquals(
            symbol,
            L10nFieldUsageSearcher(),
            L10nFieldRenameUsageSearcher(),
            project.projectScope(),
            expectedSize = 1
        )
    }

    @Test
    fun `test no extra declarations are found for fake field at different nesting levels`() = with(fixture) {
        createFormAndConfigure(
            "rootForm", "test", """
            {
              "groups": [
                {
                  "name": "group"
                  "rows": [
                    {
                      "fields": [
                        {
                          "name": "field1.field2.<caret>field3"
                        },
                        {
                          "name": "field1.field3"
                        },
                        {
                          "name": "field1.field2.anotherField.field3"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()
        )

        val symbol = getFormSymbolAtCaret(L10nFieldDeclarationProvider())

        assertSymbolUsagesAndRenameUsagesSizeEquals(
            symbol,
            L10nFieldUsageSearcher(),
            L10nFieldRenameUsageSearcher(),
            project.projectScope(),
            expectedSize = 1
        )
    }

}