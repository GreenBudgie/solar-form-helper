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
    fun `test l10n reference to fake field`() {
        fixture.configureByForms("testForm1.json", module = "test")

        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.testForm1.group1.<caret>field1" to "Field Name!"
        )

        assertReferencedSymbolNameEquals("field1")
    }

    @Test
    fun `test l10n reference to nested fake field`() {
        fixture.configureByForms("testForm1.json", module = "test")

        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.testForm1.group2.field.<caret>nestedField.randomText" to "Field Name!"
        )

        assertReferencedSymbolNameEquals("nestedField")
    }

    @Test
    fun `test l10n fake field rename from reference`() {
        fixture.configureByForms("testForm1.json", module = "test")

        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.testForm1.group1.<caret>field1" to "Field Name!"
        )

        val reference = getFormSymbolReferenceAtCaret()
        val referencedSymbol = reference.resolveReference().firstOrNull()
        Assertions.assertNotNull(referencedSymbol)
        fixture.renameTarget(referencedSymbol!!, "renamed")
        assertJsonStringLiteralValueEquals("test.form.testForm1.group1.renamed")
    }

    @Test
    fun `test l10n fake field rename from declaration`() {
        val l10nFile = L10nTestUtils.createL10nFile(fixture, "l10n",
            "test.form.form.group.fakeField.field1" to "field1",
            "test.form.form.group.fakeField.field2" to "field2",
        )

        fixture.createFormAndConfigure("form", """
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
        """.trimIndent(), "test")

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

        val expectedL10nText = L10nTestUtils.generateL10nFileText(
            "test.form.form.group.renamed.field1" to "field1",
            "test.form.form.group.renamed.field2" to "field2",
        )

        renameFormSymbolDeclaration(L10nFieldDeclarationProvider(), "renamed")
        fixture.checkResult(expectedFormText)

        fixture.openFileInEditor(l10nFile.virtualFile)
        fixture.checkResult(expectedL10nText)
    }

    @Test
    fun `test l10n fake field completion`() {
        fixture.configureByForms("testForm1.json", module = "test")

        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.testForm1.group1.<caret>" to "Field Name!"
        )

        assertCompletionsContainsExact("field1", "field2")
    }

    @Test
    fun `test l10n fake nested field first part completion`() {
        fixture.configureByForms("testForm1.json", module = "test")

        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.testForm1.group2.<caret>" to "Field Name!"
        )

        assertCompletionsContainsExact("field")
    }

    @Test
    fun `test l10n fake nested field second part completion`() {
        fixture.configureByForms("testForm1.json", module = "test")

        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.testForm1.group2.field.<caret>" to "Nested Field Name!"
        )

        assertCompletionsContainsExact("nestedField")
    }

    @Test
    fun `test l10n reference to fake field in included form`() {
        fixture.createForm("rootForm", """
            {
              "groups": "json://includes/forms/test/includedFormGroups.json"
            }
        """.trimIndent(), "test")

        fixture.createIncludedForm("includedFormGroups", "test", """
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
        """.trimIndent())

        fixture.createIncludedForm("includedFormFields", "test", """
            [
              {
                "name": "fieldName"
              }
            ]
        """.trimIndent())

        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.rootForm.groupName.<caret>fieldName" to "Field name"
        )

        assertReferencedSymbolNameEquals("fieldName")
    }

    @Test
    fun `test find usages in project scope`() {
        fixture.configureByForms("testForm1.json", module = "test")

        L10nTestUtils.createL10nFile(fixture, "l10n_2",
            "test.form.testForm1.group1.field1" to "Field Name!",
            "test.form.testForm1.group1.field1.randomText" to "Field Name!"
        )

        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.testForm1.group1.<caret>field1" to "Field Name!"
        )

        val symbol = getFormSymbolReferenceAtCaret().resolveReference().first()

        assertSymbolUsagesAndRenameUsagesSizeEquals(
            symbol,
            L10nFieldUsageSearcher(),
            L10nFieldRenameUsageSearcher(),
            fixture.project.projectScope(),
            expectedSize = 4
        )
    }

    @Test
    fun `test find usages in file scope`() {
        fixture.configureByForms("testForm1.json", module = "test")

        L10nTestUtils.createL10nFile(fixture, "l10n_2",
            "test.form.testForm1.group1.field1" to "Field Name!"
        )

        val file = L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
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

}