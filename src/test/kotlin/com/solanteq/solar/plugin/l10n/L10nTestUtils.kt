package com.solanteq.solar.plugin.l10n

import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture

object L10nTestUtils {

    fun generateL10nFileText(vararg l10ns: Pair<String, String>): String {
        if(l10ns.isEmpty()) error("You need to provide at least one l10n entry")

        val l10nJsonEntries = l10ns.joinToString() { (l10nKey, l10nValue) ->
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
    fun createL10nFile(
        fixture: CodeInsightTestFixture,
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

    fun createL10nFileAndConfigure(
        fixture: CodeInsightTestFixture,
        fileName: String,
        vararg l10ns: Pair<String, String>,
        isRussian: Boolean = true
    ): PsiFile {
        val psiL10nFile = createL10nFile(fixture, fileName, *l10ns, isRussian = isRussian)
        fixture.configureFromExistingVirtualFile(psiL10nFile.virtualFile)
        return fixture.file
    }

}