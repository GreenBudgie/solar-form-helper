package com.solanteq.solar.plugin

import com.intellij.psi.PsiFile
import com.solanteq.solar.plugin.l10n.field.L10nFieldDeclarationProvider
import com.solanteq.solar.plugin.l10n.group.L10nGroupDeclarationProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class L10nTest : FormTestBase() {

    override fun getTestDataSuffix() = "l10n"

    // Module

    @Test
    fun `test l10n module reference`() {
        fixture.createForm("testForm", "{}", "test")

        createL10nFileAndConfigure("l10n",
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
    fun `test l10n module completion`(l10nKey: String) {
        fixture.createForm("testForm", "{}", "test")
        fixture.createForm("testForm2", "{}", "test")
        fixture.createForm("testForm", "{}", "test2")
        fixture.createForm("testForm", "{}", "test3")

        createL10nFileAndConfigure("l10n",
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
    fun `test l10n type completion`(l10nKey: String) {
        fixture.createForm("testForm", "{}", "test")

        createL10nFileAndConfigure("l10n",
            l10nKey to "some l10n"
        )

        assertCompletionsContainsExact("form", "dd")
    }

    // Form

    @Test
    fun `test l10n reference to form`() {
        fixture.configureByForms("testForm1.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.<caret>testForm1.randomText" to "Form Name!"
        )

        assertReferencedElementNameEquals("testForm1.json")
    }

    @Test
    fun `test l10n form completion`() {
        fixture.configureByForms(
            "testForm1.json",
            "testForm2.json",
            module = "test"
        )

        fixture.createForm("confusingForm", "{}", "notTest")

        createL10nFileAndConfigure("l10n",
            "test.form.<caret>" to "Form Name!"
        )

        assertCompletionsContainsExact("testForm1", "testForm2")
    }

    @Test
    fun `test l10n form rename`() {
        fixture.configureByForms("testForm1.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.<caret>testForm1" to "Form Name!"
        )

        fixture.renameElementAtCaretUsingHandler("renamed.json")
        assertJsonStringLiteralValueEquals("test.form.renamed")
    }

    // Group

    @Test
    fun `test l10n reference to group`() {
        fixture.configureByForms("testForm1.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testForm1.<caret>group1.randomText" to "Group Name!"
        )

        assertReferencedSymbolNameEquals("group1")
    }

    @Test
    fun `test l10n group completion`() {
        fixture.configureByForms("testForm1.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testForm1.<caret>" to "Group Name!"
        )

        assertCompletionsContainsExact("group1", "group2")
    }

    @Test
    fun `test l10n group reference rename`() {
        val formFile = fixture.createForm("testForm", """
            {
              "groups": [
                {
                  "name": "group1"
                }
              ]
            }
        """.trimIndent(), "test")

        createL10nFileAndConfigure("l10n",
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
        fixture.createFormAndConfigure("testForm", """
            {
              "groups": [
                {
                  "name": "<caret>group1"
                }
              ]
            }
        """.trimIndent(), "test")

        val l10nFile = createL10nFile("l10n",
            "test.form.testForm.group1" to "Group Name!"
        )

        val expectedL10nText = generateL10nFileText(
            "test.form.testForm.renamed" to "Group Name!"
        )

        renameFormSymbolDeclaration(L10nGroupDeclarationProvider(), "renamed")
        assertJsonStringLiteralValueEquals("renamed")

        fixture.openFileInEditor(l10nFile.virtualFile)
        fixture.checkResult(expectedL10nText)
    }

    @Test
    fun `test l10n reference to group in included form`() {
        fixture.createForm("topLevelForm", """
            {
              "groups": "json://includes/forms/test/includedForm.json"
            }
        """.trimIndent(), "test")

        fixture.createIncludedForm("includedForm", "test", """
            [
              {
                "name": "groupName"
              }
            ]
        """.trimIndent())

        createL10nFileAndConfigure("l10n",
            "test.form.topLevelForm.<caret>groupName" to "Group name"
        )

        assertReferencedSymbolNameEquals("groupName")
    }

    // Fake fields (fields that are not backed by fields in data classes)

    @Test
    fun `test l10n reference to fake field`() {
        fixture.configureByForms("testForm1.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testForm1.group1.<caret>field1" to "Field Name!"
        )

        assertReferencedSymbolNameEquals("field1")
    }

    @Test
    fun `test l10n reference to nested fake field`() {
        fixture.configureByForms("testForm1.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testForm1.group2.field.<caret>nestedField.randomText" to "Field Name!"
        )

        assertReferencedSymbolNameEquals("nestedField")
    }

    @Test
    fun `test l10n fake field rename from reference`() {
        fixture.configureByForms("testForm1.json", module = "test")

        createL10nFileAndConfigure("l10n",
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
        val l10nFile = createL10nFile("l10n",
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

        val expectedL10nText = generateL10nFileText(
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

        createL10nFileAndConfigure("l10n",
            "test.form.testForm1.group1.<caret>" to "Field Name!"
        )

        assertCompletionsContainsExact("field1", "field2")
    }

    @Test
    fun `test l10n fake nested field first part completion`() {
        fixture.configureByForms("testForm1.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testForm1.group2.<caret>" to "Field Name!"
        )

        assertCompletionsContainsExact("field")
    }

    @Test
    fun `test l10n fake nested field second part completion`() {
        fixture.configureByForms("testForm1.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testForm1.group2.field.<caret>" to "Nested Field Name!"
        )

        assertCompletionsContainsExact("nestedField")
    }

    @Test
    fun `test l10n reference to fake field in included form`() {
        fixture.createForm("topLevelForm", """
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

        createL10nFileAndConfigure("l10n",
            "test.form.topLevelForm.groupName.<caret>fieldName" to "Field name"
        )

        assertReferencedSymbolNameEquals("fieldName")
    }

    // Real fields (fields that are backed by fields in data classes)

    @Test
    fun `test l10n real field reference`() {
        prepareRealFieldsTest()

        createL10nFileAndConfigure("l10n",
            "test.form.fieldsForm.group1.<caret>realField" to "Field Name!"
        )

        assertReferencedElementNameEquals("realField")
    }

    @Test
    fun `test l10n real field rename from reference`() {
        prepareRealFieldsTest()

        createL10nFileAndConfigure("l10n",
            "test.form.fieldsForm.group1.<caret>realField" to "Field Name!"
        )

        fixture.renameElementAtCaret("renamed")
        assertJsonStringLiteralValueEquals("test.form.fieldsForm.group1.renamed")
    }

    @Test
    fun `test l10n fake field reference when source exists`() {
        prepareRealFieldsTest()

        createL10nFileAndConfigure("l10n",
            "test.form.fieldsForm.group1.<caret>fakeField" to "Field Name!"
        )

        assertReferencedSymbolNameEquals("fakeField")
    }

    @Test
    fun `test l10n real nested field reference`() {
        prepareRealFieldsTest()

        createL10nFileAndConfigure("l10n",
            "test.form.fieldsForm.group2.realFieldWithNested.<caret>realNestedField" to "Field Name!"
        )

        assertReferencedElementNameEquals("realNestedField")
    }

    @Test
    fun `test l10n real and fake fields completion`() {
        prepareRealFieldsTest()

        createL10nFileAndConfigure("l10n",
            "test.form.fieldsForm.group1.<caret>" to "Field Name!"
        )

        assertCompletionsContainsExact("realField", "fakeField")
    }

    @Test
    fun `test l10n real nested field completion first part`() {
        prepareRealFieldsTest()

        createL10nFileAndConfigure("l10n",
            "test.form.fieldsForm.group2.<caret>" to "Field Name!"
        )

        assertCompletionsContainsExact("realFieldWithNested")
    }

    @Test
    fun `test l10n real nested field completion second part`() {
        prepareRealFieldsTest()

        createL10nFileAndConfigure("l10n",
            "test.form.fieldsForm.group2.realFieldWithNested.<caret>" to "Field Name!"
        )

        assertCompletionsContainsExact("realNestedField")
    }

    private fun generateL10nFileText(vararg l10ns: Pair<String, String>): String {
        if(l10ns.isEmpty()) error("You need to provide at least one l10n entry")

        val l10nJsonEntries = l10ns.joinToString { (l10nKey, l10nValue) ->
            "\"$l10nKey\": \"$l10nValue\",\n"
        }.dropLast(2)

        return """
            {
            $l10nJsonEntries
            }
        """.trimIndent()
    }

    /**
     * Creates a new localization file with specified l10n entries and places it into a proper directory
     *
     * @param fileName name of a file without `json` extension
     * @param l10ns Key and value of localization without quotes, e.g. ("test.form.group.field", "A field!")
     * @param isRussian Whether to create this file in ru-RU directory
     */
    private fun createL10nFile(
        fileName: String,
        vararg l10ns: Pair<String, String>,
        isRussian: Boolean = true
    ): PsiFile {
        if(l10ns.isEmpty()) error("You need to provide at least one l10n entry")

        val realFileName = "$fileName.json"
        val languagePath = if(isRussian) "ru-RU" else "en-US"

        return fixture.addFileToProject(
            "main/resources/config/l10n/$languagePath/$realFileName",
            generateL10nFileText(*l10ns)
        )
    }

    private fun createL10nFileAndConfigure(
        fileName: String,
        vararg l10ns: Pair<String, String>,
        isRussian: Boolean = true
    ): PsiFile {
        val psiL10nFile = createL10nFile(fileName, *l10ns, isRussian = isRussian)
        fixture.configureFromExistingVirtualFile(psiL10nFile.virtualFile)
        return fixture.file
    }

    private fun prepareRealFieldsTest() {
        fixture.configureByFiles(
            "TestService.kt",
            "TestServiceImpl.kt",
            "DataClass.kt",
            "NestedDataClass.kt",
        )

        fixture.configureByForms("fieldsForm.json", module = "test")
    }

}