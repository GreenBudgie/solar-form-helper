package com.solanteq.solar.plugin

import com.intellij.psi.PsiFile
import org.junit.jupiter.api.Test

class L10nTest : FormTestBase() {

    override fun getTestDataSuffix() = "l10n"

    @Test
    fun `test l10n reference to form`() {
        fixture.configureByForms("testFormGroups.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.test<caret>FormGroups.randomText" to "Form Name!"
        )

        assertReferencedElementName("testFormGroups")
    }

    @Test
    fun `test l10n reference to group`() {
        fixture.configureByForms("testFormGroups.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testFormGroups.<caret>group1.randomText" to "Group Name!"
        )

        assertReferencedElementName("group1")
    }

    @Test
    fun `test l10n reference to field`() {
        fixture.configureByForms("testFormGroups.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testForm.group1.<caret>field1" to "Field Name!"
        )

        assertReferencedElementName("field1")
    }

    @Test
    fun `test l10n reference to field with groupRows`() {
        fixture.configureByForms("testFormGroupRows.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testForm.group1.<caret>field2" to "Field Name!!"
        )

        assertReferencedElementName("field2")
    }

    @Test
    fun `test l10n reference to nested field`() {
        fixture.configureByForms("testFormGroups.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testForm.group2.field.<caret>nestedField.randomText" to "Field Name!"
        )

        assertReferencedElementName("nestedField")
    }

    @Test
    fun `test l10n form completion`() {
        fixture.configureByForms(
            "testFormGroups.json",
            "testFormGroupRows.json",
            module = "test"
        )

        fixture.createForm("confusingForm", "{}", "notTest")

        createL10nFileAndConfigure("l10n",
            "test.form.<caret>" to "Form Name!"
        )

        assertCompletionsContainsExact("testFormGroups", "testFormGroupRows")
    }

    @Test
    fun `test l10n group completion`() {
        fixture.configureByForms("testFormGroups.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testFormGroups.<caret>" to "Group Name!"
        )

        assertCompletionsContainsExact("group1", "group2")
    }

    @Test
    fun `test l10n groupRows completion`() {
        fixture.configureByForms("testFormGroupRows.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testFormGroupRows.<caret>" to "Group Name!"
        )

        assertCompletionsContainsExact("group1", "group2")
    }

    @Test
    fun `test l10n field completion`() {
        fixture.configureByForms("testFormGroups.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testFormGroups.group1.<caret>" to "Field Name!"
        )

        assertCompletionsContainsExact("field1", "field2")
    }

    @Test
    fun `test l10n nested field first part completion`() {
        fixture.configureByForms("testFormGroups.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testFormGroups.group2.<caret>" to "Field Name!"
        )

        assertCompletionsContainsExact("field")
    }

    @Test
    fun `test l10n nested field second part completion`() {
        fixture.configureByForms("testFormGroups.json", module = "test")

        createL10nFileAndConfigure("l10n",
            "test.form.testFormGroups.group2.field.<caret>" to "Nested Field Name!"
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