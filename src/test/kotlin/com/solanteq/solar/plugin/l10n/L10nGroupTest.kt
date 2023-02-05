package com.solanteq.solar.plugin.l10n

import com.intellij.psi.search.GlobalSearchScope
import com.solanteq.solar.plugin.base.*
import com.solanteq.solar.plugin.l10n.group.L10nGroupDeclarationProvider
import com.solanteq.solar.plugin.l10n.group.L10nGroupRenameUsageSearcher
import com.solanteq.solar.plugin.l10n.group.L10nGroupUsageSearcher
import org.jetbrains.kotlin.idea.base.util.projectScope
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class L10nGroupTest : LightPluginTestBase() {

    override fun getTestDataSuffix() = "l10n"

    @Test
    fun `test l10n reference to group`() {
        fixture.configureByForms("testForm1.json", module = "test")

        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.testForm1.<caret>group1.randomText" to "Group Name!"
        )

        assertReferencedSymbolNameEquals("group1")
    }

    @Test
    fun `test l10n group completion`() {
        fixture.configureByForms("testForm1.json", module = "test")

        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.testForm1.<caret>" to "Group Name!"
        )

        assertCompletionsContainsExact("group1", "group2")
    }

    @Test
    fun `test l10n group reference rename`() {
        val formFile = fixture.createForm(
            "testForm", "test", """
                {
                  "groups": [
                    {
                      "name": "group1"
                    }
                  ]
                }
            """.trimIndent()
        )

        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.testForm.<caret>group1" to "Group Name!"
        )

        val expectedFormText = """
            {
              "groups": [
                {
                  "name": "renamed"
                }
              ]
            }
        """.trimIndent()

        renameFormSymbolReference("renamed")
        assertJsonStringLiteralValueEquals("test.form.testForm.renamed")
        fixture.openFileInEditor(formFile.virtualFile)
        fixture.checkResult(expectedFormText)
    }

    @Test
    fun `test l10n group declaration rename`() {
        fixture.createFormAndConfigure(
            "testForm", "test", """
                {
                  "groups": [
                    {
                      "name": "<caret>group1"
                    }
                  ]
                }
            """.trimIndent()
        )

        val l10nFile = L10nTestUtils.createL10nFile(fixture, "l10n",
            "test.form.testForm.group1" to "Group Name!"
        )

        val expectedL10nText = L10nTestUtils.generateL10nFileText(
            "test.form.testForm.renamed" to "Group Name!"
        )

        renameFormSymbolDeclaration(L10nGroupDeclarationProvider(), "renamed")
        assertJsonStringLiteralValueEquals("renamed")

        fixture.openFileInEditor(l10nFile.virtualFile)
        fixture.checkResult(expectedL10nText)
    }

    @Test
    fun `test l10n reference to group in included form`() {
        fixture.createForm(
            "rootForm", "test", """
                {
                  "groups": "json://includes/forms/test/includedForm.json"
                }
            """.trimIndent()
        )

        fixture.createIncludedForm("includedForm", "test", """
            [
              {
                "name": "groupName"
              }
            ]
        """.trimIndent())

        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.rootForm.<caret>groupName" to "Group name"
        )

        assertReferencedSymbolNameEquals("groupName")
    }

    @Test
    fun `test l10n group rename in included form`() {
        val formTextBefore = """
            {
              "name": "groupName"
            }
        """.trimIndent()
        val formTextAfter = """
            {
              "name": "renamed"
            }
        """.trimIndent()

        fixture.createForm(
            "rootForm", "test", """
                {
                  "groups": [
                    "json://includes/forms/test/includedForm.json"
                  ]
                }
            """.trimIndent()
        )

        val form = fixture.createIncludedForm("includedForm", "test", formTextBefore)

        val l10nTextAfter = L10nTestUtils.generateL10nFileText(
            "test.form.rootForm.renamed" to "Group name"
        )

        val l10n = L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.rootForm.<caret>groupName" to "Group name"
        )

        renameFormSymbolReference("renamed")
        Assertions.assertEquals(formTextAfter, form.text)
        Assertions.assertEquals(l10nTextAfter, l10n.text)
    }

    @Test
    fun `test find usages in project scope`() {
        fixture.configureByForms("testForm1.json", module = "test")

        L10nTestUtils.createL10nFile(fixture, "l10n_2",
            "test.form.testForm1.group1" to "Group Name22!",
            "test.form.testForm1.group1.randomText3" to "Group Name!",
        )

        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.testForm1.<caret>group1.randomText" to "Group Name!",
        )

        val symbol = getFormSymbolReferenceAtCaret().resolveReference().first()

        assertSymbolUsagesAndRenameUsagesSizeEquals(
            symbol,
            L10nGroupUsageSearcher(),
            L10nGroupRenameUsageSearcher(),
            fixture.project.projectScope(),
            expectedSize = 4
        )
    }

    @Test
    fun `test find usages in l10n file scope`() {
        fixture.configureByForms("testForm1.json", module = "test")

        L10nTestUtils.createL10nFile(fixture, "l10n_2",
            "test.form.testForm1.group" to "Group Name22!",
            "test.form.testForm1.group.text" to "Group Name22!",
        )

        val file = L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.testForm1.<caret>group1.randomText" to "Group Name!",
            "test.form.testForm1.group1.randomText2" to "Group Name!",
        )

        val symbol = getFormSymbolReferenceAtCaret().resolveReference().first()
        assertSymbolUsagesAndRenameUsagesSizeEquals(
            symbol,
            L10nGroupUsageSearcher(),
            L10nGroupRenameUsageSearcher(),
            GlobalSearchScope.fileScope(file),
            expectedSize = 2
        )
    }

}