package com.solanteq.solar.plugin.l10n

import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.base.configureByRootForms
import com.solanteq.solar.plugin.base.setUpIncludedForms
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.l10n.generator.FormL10nGenerator
import com.solanteq.solar.plugin.l10n.generator.L10nPlacement
import com.solanteq.solar.plugin.l10n.search.FormL10nSearch
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.RepeatedTest
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
        assertEquals(L10nPlacement.endOfFile(l10nFile), placement)
    }

    @Test
    fun `find best placement - root form - form element - place at the end of prioritized file`(): Unit = with(fixture) {
        val formFile = configureByRootForms("test", "rootForm.json")
        val l10nFile = createL10nFile("l10n", L10nLocale.EN)
        createL10nFile("form_l10n", L10nLocale.EN)

        val form = FormRootFile.createFromOrThrow(formFile)
        val l10nEntry = form.l10nEntries.first { it.locale == L10nLocale.EN }

        val placement = FormL10nGenerator.findBestPlacement(form, l10nEntry, l10nFile)
        assertEquals(L10nPlacement.endOfFile(l10nFile), placement)
    }

    @Test
    fun `find best placement - root form - form element - place in file with correct locale`(): Unit = with(fixture) {
        val formFile = configureByRootForms("test", "rootForm.json")
        val l10nFile = createL10nFile("l10n", L10nLocale.EN)
        createL10nFile("l10n", L10nLocale.RU)

        val form = FormRootFile.createFromOrThrow(formFile)
        val l10nEntry = form.l10nEntries.first { it.locale == L10nLocale.EN }

        val placement = FormL10nGenerator.findBestPlacement(form, l10nEntry)
        assertEquals(L10nPlacement.endOfFile(l10nFile), placement)
    }

    @Test
    fun `find best placement - root form - form element - place in file with correct locale (RU locale)`(): Unit =
        with(fixture) {
            val formFile = configureByRootForms("test", "rootForm.json")
            createL10nFile("l10n", L10nLocale.EN)
            val l10nFile = createL10nFile("l10n", L10nLocale.RU)

            val form = FormRootFile.createFromOrThrow(formFile)
            val l10nEntry = form.l10nEntries.first { it.locale == L10nLocale.RU }

            val placement = FormL10nGenerator.findBestPlacement(form, l10nEntry)
            assertEquals(L10nPlacement.endOfFile(l10nFile), placement)
        }

    @Test
    fun `find best placement - root form - form element - place at the end of an empty file - prioritize file name with module`(): Unit =
        with(fixture) {
            val formFile = configureByRootForms("test", "rootForm.json")
            createL10nFile("l10n", L10nLocale.EN)
            createL10nFile("zzz", L10nLocale.EN)
            val l10nFile = createL10nFile("test_l10n", L10nLocale.EN)
            createL10nFile("l10n_test", L10nLocale.EN)
            createL10nFile("aaa", L10nLocale.EN)

            val form = FormRootFile.createFromOrThrow(formFile)
            val l10nEntry = form.l10nEntries.first { it.locale == L10nLocale.EN }

            val placement = FormL10nGenerator.findBestPlacement(form, l10nEntry)
            assertEquals(L10nPlacement.endOfFile(l10nFile), placement)
        }

    @Test
    fun `find best placement - root form - form element - place at the end of an empty file - prioritize file name with form string`(): Unit =
        with(fixture) {
            val formFile = configureByRootForms("test", "rootForm.json")
            createL10nFile("l10n", L10nLocale.EN)
            createL10nFile("l10n_test", L10nLocale.EN)
            createL10nFile("f123", L10nLocale.EN)
            val l10nFile = createL10nFile("forms_l10n", L10nLocale.EN)
            createL10nFile("aaa", L10nLocale.EN)

            val form = FormRootFile.createFromOrThrow(formFile)
            val l10nEntry = form.l10nEntries.first { it.locale == L10nLocale.EN }

            val placement = FormL10nGenerator.findBestPlacement(form, l10nEntry)
            assertEquals(L10nPlacement.endOfFile(l10nFile), placement)
        }

    @Test
    fun `find best placement - root form - group element - place at the end of an empty file`(): Unit = with(fixture) {
        val formFile = configureByRootForms("test", "rootForm.json")
        val l10nFile = createL10nFile("l10n", L10nLocale.EN)

        val group = FormRootFile.createFromOrThrow(formFile).allGroups.first()
        val l10nEntry = group.l10nEntries.first { it.locale == L10nLocale.EN }

        val placement = FormL10nGenerator.findBestPlacement(group, l10nEntry)
        assertEquals(L10nPlacement.endOfFile(l10nFile), placement)
    }

    @Test
    fun `find best placement - root form - field element - place at the end of an empty file`(): Unit = with(fixture) {
        val formFile = configureByRootForms("test", "rootForm.json")
        val l10nFile = createL10nFile("l10n", L10nLocale.EN)

        val field = FormRootFile.createFromOrThrow(formFile).allGroups.first().fields.first()
        val l10nEntry = field.l10nEntries.first { it.locale == L10nLocale.EN }

        val placement = FormL10nGenerator.findBestPlacement(field, l10nEntry)
        assertEquals(L10nPlacement.endOfFile(l10nFile), placement)
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
            L10nPlacement.before(l10nFile, FormL10nSearch.findL10nPropertiesInFile(l10nFile).first()),
            placement
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
                L10nPlacement.before(l10nFile, FormL10nSearch.findL10nPropertiesInFile(l10nFile).last()),
                placement
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
            L10nPlacement.after(l10nFile, FormL10nSearch.findL10nPropertiesInFile(l10nFile).first()),
            placement
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
            L10nPlacement.after(l10nFile, FormL10nSearch.findL10nPropertiesInFile(l10nFile).last()),
            placement
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
                L10nPlacement.before(l10nFile, FormL10nSearch.findL10nPropertiesInFile(l10nFile).last()),
                placement
            )
        }

    @Test
    fun `find best placement - root form - first field element - place before second group l10n`(): Unit =
        with(fixture) {
            val formFile = configureByRootForms("test", "rootForm.json")
            val l10nFile = createL10nFile(
                "l10n",
                L10nLocale.EN,
                "test.form.rootForm.group2" to "value",
                "test.form.rootForm.group2.field2" to "value",
            )

            val field = FormRootFile.createFromOrThrow(formFile).allGroups.first().fields.first()
            val l10nEntry = field.l10nEntries.first { it.locale == L10nLocale.EN }

            val placement = FormL10nGenerator.findBestPlacement(field, l10nEntry)
            assertEquals(
                L10nPlacement.before(l10nFile, FormL10nSearch.findL10nPropertiesInFile(l10nFile).first()),
                placement
            )
        }

    @Test
    fun `find best placement - root form - field element in second group - place after first group l10n`(): Unit =
        with(fixture) {
            val formFile = configureByRootForms("test", "rootForm.json")
            val l10nFile = createL10nFile(
                "l10n",
                L10nLocale.EN,
                "test.form.rootForm.group1" to "value",
            )

            val field = FormRootFile.createFromOrThrow(formFile).allGroups.last().fields.first()
            val l10nEntry = field.l10nEntries.first { it.locale == L10nLocale.EN }

            val placement = FormL10nGenerator.findBestPlacement(field, l10nEntry)
            assertEquals(
                L10nPlacement.after(l10nFile, FormL10nSearch.findL10nPropertiesInFile(l10nFile).first()),
                placement
            )
        }

    @Test
    fun `find best placement - root form - field element in second group - place before second field l10n`(): Unit =
        with(fixture) {
            val formFile = configureByRootForms("test", "rootForm.json")
            val l10nFile = createL10nFile(
                "l10n",
                L10nLocale.EN,
                "test.form.rootForm.group2.field2" to "value",
            )

            val field = FormRootFile.createFromOrThrow(formFile).allGroups.last().fields.first()
            val l10nEntry = field.l10nEntries.first { it.locale == L10nLocale.EN }

            val placement = FormL10nGenerator.findBestPlacement(field, l10nEntry)
            assertEquals(
                L10nPlacement.before(l10nFile, FormL10nSearch.findL10nPropertiesInFile(l10nFile).first()),
                placement
            )
        }

    @Test
    fun `find best placement - included form with several parents - place after correct root form`(): Unit =
        with(fixture) {
            setUpIncludedForms("test", "includedFields.json", "includedGroup.json")
            val rootFormWithIncludesFile = configureByRootForms(
                "test",
                "rootFormWithIncludes.json",
                "rootFormWithIncludes2.json"
            )
            val l10nFile = createL10nFile(
                "l10n",
                L10nLocale.EN,
                "test.form.rootFormWithIncludes" to "value",
                "test.form.rootFormWithIncludes2" to "value",
            )

            val rootFormWithIncludes = FormRootFile.createFromOrThrow(rootFormWithIncludesFile)
            val field = rootFormWithIncludes.allGroups.first().fields.first()
            val l10nEntry = L10nEntry(
                rootFormWithIncludes,
                "test.form.rootFormWithIncludes.group1.field1",
                L10nLocale.EN
            )

            val placement = FormL10nGenerator.findBestPlacement(field, l10nEntry)
            assertEquals(
                L10nPlacement.after(l10nFile, FormL10nSearch.findL10nPropertiesInFile(l10nFile).first()),
                placement
            )
        }

    @Test
    fun `find best placement - included form with several parents - place after correct root form (different files)`(): Unit =
        with(fixture) {
            setUpIncludedForms("test", "includedFields.json", "includedGroup.json")
            val rootFormWithIncludesFile = configureByRootForms(
                "test",
                "rootFormWithIncludes.json",
                "rootFormWithIncludes2.json"
            )
            val l10nFile = createL10nFile(
                "l10n",
                L10nLocale.EN,
                "test.form.rootFormWithIncludes" to "value",
            )
            createL10nFile(
                "l10n_2",
                L10nLocale.EN,
                "test.form.rootFormWithIncludes2" to "value",
            )

            val rootFormWithIncludes = FormRootFile.createFromOrThrow(rootFormWithIncludesFile)
            val field = rootFormWithIncludes.allGroups.first().fields.first()
            val l10nEntry = L10nEntry(
                rootFormWithIncludes,
                "test.form.rootFormWithIncludes.group1.field1",
                L10nLocale.EN
            )

            val placement = FormL10nGenerator.findBestPlacement(field, l10nEntry)
            assertEquals(
                L10nPlacement.after(l10nFile, FormL10nSearch.findL10nPropertiesInFile(l10nFile).first()),
                placement
            )
        }

    @RepeatedTest(10)
    fun `find best placement - included form with several parents - place before second group`(): Unit =
        with(fixture) {
            setUpIncludedForms("test", "includedFields.json", "includedGroup.json")
            val rootFormWithIncludesFile = configureByRootForms(
                "test",
                "rootFormWithIncludes.json",
                "rootFormWithIncludes2.json"
            )
            val l10nFile = createL10nFile(
                "l10n_2",
                L10nLocale.EN,
                "test.form.rootFormWithIncludes2" to "value",
                "test.form.rootFormWithIncludes2.group1" to "value",
                "test.form.rootFormWithIncludes.group2" to "value",
                "test.form.rootFormWithIncludes2.group2" to "value",
                "test.form.rootFormWithIncludes" to "value",
                "test.form.rootFormWithIncludes.group2.field1" to "value",
            )

            val rootFormWithIncludes = FormRootFile.createFromOrThrow(rootFormWithIncludesFile)
            val field = rootFormWithIncludes.allGroups.first().fields.first()
            val l10nEntry = L10nEntry(
                rootFormWithIncludes,
                "test.form.rootFormWithIncludes.group1.field1",
                L10nLocale.EN
            )

            val placement = FormL10nGenerator.findBestPlacement(field, l10nEntry)
            assertEquals(
                L10nPlacement.before(l10nFile, FormL10nSearch.findL10nPropertiesInFile(l10nFile)[2]),
                placement
            )
        }

    @Test
    fun `find best placement - included form with several parents - place before second group (different files)`(): Unit =
        with(fixture) {
            setUpIncludedForms("test", "includedFields.json", "includedGroup.json")
            val rootFormWithIncludesFile = configureByRootForms(
                "test",
                "rootFormWithIncludes.json",
                "rootFormWithIncludes2.json"
            )
            val l10nFile = createL10nFile(
                "l10n_2",
                L10nLocale.EN,
                "test.form.rootFormWithIncludes2" to "value",
                "test.form.rootFormWithIncludes.group2" to "value",
                "test.form.rootFormWithIncludes.group2.field1" to "value",
            )
            createL10nFile(
                "l10n",
                L10nLocale.EN,
                "test.form.rootFormWithIncludes2.group1" to "value",
                "test.form.rootFormWithIncludes2.group2" to "value",
                "test.form.rootFormWithIncludes" to "value",
            )


            val rootFormWithIncludes = FormRootFile.createFromOrThrow(rootFormWithIncludesFile)
            val field = rootFormWithIncludes.allGroups.first().fields.first()
            val l10nEntry = L10nEntry(
                rootFormWithIncludes,
                "test.form.rootFormWithIncludes.group1.field1",
                L10nLocale.EN
            )

            val placement = FormL10nGenerator.findBestPlacement(field, l10nEntry)
            assertEquals(
                L10nPlacement.before(l10nFile, FormL10nSearch.findL10nPropertiesInFile(l10nFile)[1]),
                placement
            )
        }

    @RepeatedTest(10)
    fun `find best placement - included form with several parents - place before correct field`(): Unit =
        with(fixture) {
            setUpIncludedForms("test", "includedFields.json", "includedGroup.json")
            val rootFormWithIncludesFile = configureByRootForms(
                "test",
                "rootFormWithIncludes.json",
                "rootFormWithIncludes2.json"
            )
            val l10nFile = createL10nFile(
                "l10n",
                L10nLocale.EN,
                "test.form.rootFormWithIncludes2.group2.field2" to "value",
                "test.form.rootFormWithIncludes.group2.field2" to "value",
            )

            val rootFormWithIncludes = FormRootFile.createFromOrThrow(rootFormWithIncludesFile)
            val field = rootFormWithIncludes.allGroups.first().fields.first()
            val l10nEntry = L10nEntry(
                rootFormWithIncludes,
                "test.form.rootFormWithIncludes.group1.field1",
                L10nLocale.EN
            )

            val placement = FormL10nGenerator.findBestPlacement(field, l10nEntry)
            assertEquals(
                L10nPlacement.before(l10nFile, FormL10nSearch.findL10nPropertiesInFile(l10nFile).last()),
                placement
            )
        }

    @RepeatedTest(10)
    fun `find best placement - included form with several parents - place after correct field (different files)`(): Unit =
        with(fixture) {
            setUpIncludedForms("test", "includedFields.json", "includedGroup.json")
            val rootFormWithIncludesFile = configureByRootForms(
                "test",
                "rootFormWithIncludes.json",
                "rootFormWithIncludes2.json"
            )
            createL10nFile(
                "l10n_2",
                L10nLocale.EN,
                "test.form.rootFormWithIncludes2.group2.field2" to "value",
            )
            val l10nFile = createL10nFile(
                "l10n",
                L10nLocale.EN,
                "test.form.rootFormWithIncludes.group2.field2" to "value",
            )

            val rootFormWithIncludes = FormRootFile.createFromOrThrow(rootFormWithIncludesFile)
            val field = rootFormWithIncludes.allGroups.first().fields.first()
            val l10nEntry = L10nEntry(
                rootFormWithIncludes,
                "test.form.rootFormWithIncludes.group1.field1",
                L10nLocale.EN
            )

            val placement = FormL10nGenerator.findBestPlacement(field, l10nEntry)
            assertEquals(
                L10nPlacement.before(l10nFile, FormL10nSearch.findL10nPropertiesInFile(l10nFile).first()),
                placement
            )
        }

}