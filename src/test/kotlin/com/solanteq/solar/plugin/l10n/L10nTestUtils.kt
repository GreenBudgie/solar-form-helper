package com.solanteq.solar.plugin.l10n

import com.intellij.json.psi.JsonFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture

private const val L10N_DIRECTORY = "main/resources/config/l10n/"

fun generateL10nFileText(vararg l10ns: Pair<String, String>): String {
    if (l10ns.isEmpty()) {
        return "{\n}"
    }

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
 * Creates a new localization file with specified text and places it into a proper directory
 *
 * @param fileName name of a file without `json` extension
 * @param text Text to insert into l10n file
 * @param locale Whether to create this file in ru-RU directory
 */
fun CodeInsightTestFixture.createL10nFile(
    fileName: String,
    locale: L10nLocale = L10nLocale.RU,
    text: String,
): JsonFile {
    return addL10nFileToProject(
        fileName,
        locale,
        text
    )
}

/**
 * Creates a new localization file with specified l10n entries and places it into a proper directory
 *
 * @param fileName name of a file without `json` extension
 * @param l10ns Key and value of localization without quotes, e.g. ("test.form.group.field", "A field!")
 * @param locale Whether to create this file in ru-RU directory
 */
fun CodeInsightTestFixture.createL10nFile(
    fileName: String,
    vararg l10ns: Pair<String, String>,
    locale: L10nLocale = L10nLocale.RU,
): JsonFile {
    return addL10nFileToProject(
        fileName,
        locale,
        generateL10nFileText(*l10ns)
    )
}

fun CodeInsightTestFixture.createL10nFileAndConfigure(
    fileName: String,
    vararg l10ns: Pair<String, String>,
    locale: L10nLocale = L10nLocale.RU,
): JsonFile {
    val psiL10nFile = createL10nFile(fileName, *l10ns, locale = locale)
    configureFromExistingVirtualFile(psiL10nFile.virtualFile)
    return file as JsonFile
}

fun CodeInsightTestFixture.addL10nFile(l10nFilePath: String, locale: L10nLocale) {
    copyFileToProject(
        l10nFilePath,
        getL10nFileDirectory(locale, l10nFilePath)
    )
}

private fun CodeInsightTestFixture.addL10nFileToProject(
    fileName: String,
    locale: L10nLocale,
    text: String,
): JsonFile {
    val realFileName = "$fileName.json"

    return addFileToProject(
        getL10nFileDirectory(locale, realFileName),
        text
    ) as JsonFile
}

private fun getL10nFileDirectory(
    locale: L10nLocale,
    filePath: String,
): String {
    val directory = locale.directoryName
    return "$L10N_DIRECTORY$directory/$filePath"
}