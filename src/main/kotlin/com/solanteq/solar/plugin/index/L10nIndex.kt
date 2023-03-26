package com.solanteq.solar.plugin.index

import com.intellij.json.psi.JsonFile
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.solanteq.solar.plugin.file.L10nFileType
import com.solanteq.solar.plugin.l10n.FormL10n
import com.solanteq.solar.plugin.l10n.search.L10nSearch
import com.solanteq.solar.plugin.util.valueAsStringOrEmpty

/**
 * An index that stores all l10n key-value pairs from all l10n files.
 *
 * **This index does not include [FormL10n]s.**
 */
class L10nIndex : FileBasedIndexExtension<String, String>() {

    override fun getName() = NAME

    override fun getIndexer() = DataIndexer<String, String, FileContent> { fileContent ->
        val file = fileContent.psiFile as? JsonFile ?: return@DataIndexer emptyMap()
        val properties = L10nSearch.findL10nPropertiesInFile(file)
        val l10nProperties = properties.filterNot { FormL10n.isFormL10n(it) }
        val keyValuePairs = l10nProperties.map { it.name to it.valueAsStringOrEmpty() }
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

        val NAME = ID.create<String, String>("L10nIndex")

        /**
         * Returns all localization values associated with the given key,
         * or empty list if this key is not present in any file or invalid
         */
        fun getLocalizationsByKey(key: String, scope: GlobalSearchScope): List<String> =
            FileBasedIndex.getInstance().getValues(NAME, key, scope)

        /**
         * Returns all files that contain the specified localization key in the given scope
         */
        fun getFilesContainingKey(key: String, scope: GlobalSearchScope): Collection<VirtualFile> =
            FileBasedIndex.getInstance().getContainingFiles(NAME, key, scope)

    }


}