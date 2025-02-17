package com.solanteq.solar.plugin.l10n

import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.base.configureByRootForms
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.l10n.generator.FormL10nGenerator
import com.solanteq.solar.plugin.l10n.generator.L10nPlacement
import com.solanteq.solar.plugin.l10n.search.FormL10nSearch
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author nbundin
 * @since %CURRENT_VERSION%
 */
class FormL10nGeneratorTest : LightPluginTestBase() {

    override fun getTestDataSuffix() = "l10n/formGenerator"

    @Test
    fun `find best placement - root form - form element - place at the end of an empty file`(): Unit = with(fixture) {
        val formFile = configureByRootForms("test", "rootForm.json")
        val l10nFile = createL10nFile("l10n", L10nLocale.EN)

        val form = FormRootFile.createFromOrThrow(formFile)
        val l10nEntry = form.l10nEntries.first { it.locale == L10nLocale.EN }

        val placement = FormL10nGenerator.findBestPlacement(form, l10nEntry)
        assertEquals(placement, L10nPlacement.endOfFile(l10nFile))
    }

    @Test
    fun `find best placement - root form - group element - place at the end of an empty file`(): Unit = with(fixture) {
        val formFile = configureByRootForms("test", "rootForm.json")
        val l10nFile = createL10nFile("l10n", L10nLocale.EN)

        val group = FormRootFile.createFromOrThrow(formFile).allGroups.first()
        val l10nEntry = group.l10nEntries.first { it.locale == L10nLocale.EN }

        val placement = FormL10nGenerator.findBestPlacement(group, l10nEntry)
        assertEquals(placement, L10nPlacement.endOfFile(l10nFile))
    }

    @Test
    fun `find best placement - root form - field element - place at the end of an empty file`(): Unit = with(fixture) {
        val formFile = configureByRootForms("test", "rootForm.json")
        val l10nFile = createL10nFile("l10n", L10nLocale.EN)

        val field = FormRootFile.createFromOrThrow(formFile).allGroups.first().fields.first()
        val l10nEntry = field.l10nEntries.first { it.locale == L10nLocale.EN }

        val placement = FormL10nGenerator.findBestPlacement(field, l10nEntry)
        assertEquals(placement, L10nPlacement.endOfFile(l10nFile))
    }

    @Test
    fun `find best placement - root form - form element - place before group l10n`(): Unit = with(fixture) {
        val formFile = configureByRootForms("test", "rootForm.json")
        val l10nFile = createL10nFile(
            "l10n",
            L10nLocale.EN,
            "test.form.rootForm.group1" to "value"
        )

        val form = FormRootFile.createFromOrThrow(formFile)
        val l10nEntry = form.l10nEntries.first { it.locale == L10nLocale.EN }

        val placement = FormL10nGenerator.findBestPlacement(form, l10nEntry)
        assertEquals(
            placement,
            L10nPlacement.before(l10nFile, FormL10nSearch.findL10nPropertiesInFile(l10nFile).first())
        )
    }

    @Test
    fun `find best placement - root form - form element - place before group l10n with another dummy l10n`(): Unit =
        with(fixture) {
            val formFile = configureByRootForms("test", "rootForm.json")
            val l10nFile = createL10nFile(
                "l10n",
                L10nLocale.EN,
                "test.form.rootForm.something" to "value",
                "test.form.rootForm.group1" to "value"
            )

            val form = FormRootFile.createFromOrThrow(formFile)
            val l10nEntry = form.l10nEntries.first { it.locale == L10nLocale.EN }

            val placement = FormL10nGenerator.findBestPlacement(form, l10nEntry)
            assertEquals(
                placement,
                L10nPlacement.before(l10nFile, FormL10nSearch.findL10nPropertiesInFile(l10nFile).last())
            )
        }

    @Test
    fun `find best placement - root form - first group element - place after form l10n`(): Unit = with(fixture) {
        val formFile = configureByRootForms("test", "rootForm.json")
        val l10nFile = createL10nFile(
            "l10n",
            L10nLocale.EN,
            "test.form.rootForm" to "value"
        )

        val group = FormRootFile.createFromOrThrow(formFile).allGroups.first()
        val l10nEntry = group.l10nEntries.first { it.locale == L10nLocale.EN }

        val placement = FormL10nGenerator.findBestPlacement(group, l10nEntry)
        assertEquals(
            placement,
            L10nPlacement.after(l10nFile, FormL10nSearch.findL10nPropertiesInFile(l10nFile).first())
        )
    }

    @Test
    fun `find best placement - root form - first field element - place after group l10n`(): Unit = with(fixture) {
        val formFile = configureByRootForms("test", "rootForm.json")
        val l10nFile = createL10nFile(
            "l10n",
            L10nLocale.EN,
            "test.form.rootForm" to "value",
            "test.form.rootForm.group1" to "value",
        )

        val field = FormRootFile.createFromOrThrow(formFile).allGroups.first().fields.first()
        val l10nEntry = field.l10nEntries.first { it.locale == L10nLocale.EN }

        val placement = FormL10nGenerator.findBestPlacement(field, l10nEntry)
        assertEquals(
            placement,
            L10nPlacement.after(l10nFile, FormL10nSearch.findL10nPropertiesInFile(l10nFile).last())
        )
    }

    @Test
    fun `find best placement - root form - first field element - place after group l10n and before second field l10n`(): Unit =
        with(fixture) {
            val formFile = configureByRootForms("test", "rootForm.json")
            val l10nFile = createL10nFile(
                "l10n",
                L10nLocale.EN,
                "test.form.rootForm" to "value",
                "test.form.rootForm.group1" to "value",
                "test.form.rootForm.group1.field2" to "value",
            )

            val field = FormRootFile.createFromOrThrow(formFile).allGroups.first().fields.first()
            val l10nEntry = field.l10nEntries.first { it.locale == L10nLocale.EN }

            val placement = FormL10nGenerator.findBestPlacement(field, l10nEntry)
            assertEquals(
                placement,
                L10nPlacement.before(l10nFile, FormL10nSearch.findL10nPropertiesInFile(l10nFile).last())
            )
        }

}