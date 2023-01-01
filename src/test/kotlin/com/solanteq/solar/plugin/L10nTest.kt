package com.solanteq.solar.plugin

import com.intellij.psi.PsiFile
import org.junit.jupiter.api.Disabled
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

        testJsonStringLiteralRename("renamed.json", "test.form.renamed")
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
    fun `test l10n group rename`() {
        fixture.configureByForms("testForm1.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testForm1.<caret>group1" to "Group Name!"
        )

        testSymbolReferenceInStringLiteralRename(
            "renamed",
            "test.form.testForm1.renamed"
        )
    }

    // Field

    @Test
    @Disabled("Not yet implemented")
    fun `test l10n reference to field`() {
        fixture.configureByForms("testForm1.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testForm.group1.<caret>field1" to "Field Name!"
        )

        assertReferencedElementNameEquals("field1")
    }

    @Test
    @Disabled("Not yet implemented")
    fun `test l10n reference to nested field`() {
        fixture.configureByForms("testForm1.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testForm.group2.field.<caret>nestedField.randomText" to "Field Name!"
        )

        assertReferencedElementNameEquals("nestedField")
    }

    @Test
    @Disabled("Not yet implemented")
    fun `test l10n field completion`() {
        fixture.configureByForms("testForm1.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testFormGroups.group1.<caret>" to "Field Name!"
        )

        assertCompletionsContainsExact("field1", "field2")
    }

    @Test
    @Disabled("Not yet implemented")
    fun `test l10n nested field first part completion`() {
        fixture.configureByForms("testForm1.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testForm1.group2.<caret>" to "Field Name!"
        )

        assertCompletionsContainsExact("field")
    }

    @Test
    @Disabled("Not yet implemented")
    fun `test l10n nested field second part completion`() {
        fixture.configureByForms("testForm1.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testForm1.group2.field.<caret>" to "Nested Field Name!"
        )

        assertCompletionsContainsExact("nestedField")
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

        val l10nJsonEntries = l10ns.joinToString { (l10nKey, l10nValue) ->
            "\"$l10nKey\": \"$l10nValue\",\n"
        }.dropLast(2)

        return fixture.addFileToProject(
            "main/resources/config/l10n/$languagePath/$realFileName",
            """
                {
                $l10nJsonEntries
                }
            """.trimIndent()
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

}