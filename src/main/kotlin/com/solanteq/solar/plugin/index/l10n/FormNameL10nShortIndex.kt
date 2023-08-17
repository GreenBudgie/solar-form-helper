package com.solanteq.solar.plugin.index.l10n

import com.intellij.json.psi.JsonFile
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.solanteq.solar.plugin.file.L10nFileType
import com.solanteq.solar.plugin.l10n.FormL10n
import com.solanteq.solar.plugin.l10n.search.FormL10nSearch

val FORM_L10N_SHORT_INDEX_NAME = ID.create<String, Void>("FormNameL10nShortIndex")

/**
 * An index that stores files that contain l10ns for the specific form, ignoring
 * the latter groups, fields, etc.
 *
 * Keys are stored in the l10n format, for example: "moduleName.form.formName"
 *
 * For example, if the file contains l10n "module.form.test.group.field", it will be stored
 * as "module.form.test" and point to the containing file
 */
class FormNameL10nShortIndex : ScalarIndexExtension<String>() {

    override fun getName() = FORM_L10N_SHORT_INDEX_NAME

    override fun getIndexer() = DataIndexer<String, Void, FileContent> { fileContent ->
        val file = fileContent.psiFile as? JsonFile ?: return@DataIndexer emptyMap()
        val properties = FormL10nSearch.findL10nPropertiesInFile(file)
        val moduleAndFormNameSet = mutableSetOf<String>()
        properties.forEach {
            val key = it.name
            if(!FormL10n.isFormL10n(key)) {
                return@forEach
            }
            val split = key.split(".")
            if(split.size < 3) {
                return@forEach
            }
            val moduleName = split[0]
            val formName = split[2]
            moduleAndFormNameSet += "$moduleName.form.$formName"
        }
        moduleAndFormNameSet.associateWith { null }
    }

    override fun getKeyDescriptor(): EnumeratorStringDescriptor = EnumeratorStringDescriptor.INSTANCE

    override fun getVersion() = 1

    override fun getInputFilter() = FileBasedIndex.InputFilter { it.fileType == L10nFileType }

    override fun dependsOnFileContent() = true

}