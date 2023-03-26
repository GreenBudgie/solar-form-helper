package com.solanteq.solar.plugin.index

import com.intellij.json.psi.JsonFile
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.solanteq.solar.plugin.file.L10nFileType
import com.solanteq.solar.plugin.l10n.FormL10n
import com.solanteq.solar.plugin.l10n.search.FormL10nSearch
import com.solanteq.solar.plugin.util.valueAsStringOrEmpty

/**
 * An index that stores all form l10n key-value pairs from all l10n files.
 *
 * **This index only includes [FormL10n]s**
 */
class FormL10nIndex : FileBasedIndexExtension<String, String>() {

    override fun getName() = NAME

    override fun getIndexer() = DataIndexer<String, String, FileContent> { fileContent ->
        val file = fileContent.psiFile as? JsonFile ?: return@DataIndexer emptyMap()
        val properties = FormL10nSearch.findL10nPropertiesInFile(file)
        val formL10nProperties = properties.filter { FormL10n.isFormL10n(it) }
        val keyValuePairs = formL10nProperties.map { it.name to it.valueAsStringOrEmpty() }
        keyValuePairs.toMap()
    }

    override fun getKeyDescriptor(): EnumeratorStringDescriptor =
        EnumeratorStringDescriptor.INSTANCE

    override fun getValueExternalizer(): EnumeratorStringDescriptor =
        EnumeratorStringDescriptor.INSTANCE

    override fun getVersion() = 1

    override fun getInputFilter() = FileBasedIndex.InputFilter { it.fileType == L10nFileType }

    override fun dependsOnFileContent() = true

    companion object {

        val NAME = ID.create<String, String>("FormL10nIndex")

    }


}