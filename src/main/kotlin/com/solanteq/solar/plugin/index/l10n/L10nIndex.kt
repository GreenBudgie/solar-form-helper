package com.solanteq.solar.plugin.index.l10n

import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.solanteq.solar.plugin.file.L10nFileType
import com.solanteq.solar.plugin.l10n.FormL10n

val L10N_INDEX_NAME = ID.create<L10nIndexKey, String>("L10nIndex")

/**
 * An index that stores all l10n key-value pairs from all l10n files.
 *
 * Key string is constructed from two parts: l10n locale and key itself like this:
 *
 * **This index does not include [FormL10n]s.**
 */
class L10nIndex : FileBasedIndexExtension<L10nIndexKey, String>() {

    override fun getName() = L10N_INDEX_NAME

    override fun getIndexer() = DataIndexer<L10nIndexKey, String, FileContent> { fileContent ->
        L10nIndexUtil.buildL10nIndexMap(fileContent) {
            !FormL10n.isFormL10n(it)
        }
    }

    override fun getKeyDescriptor() = L10nKeyDescriptor

    override fun getValueExternalizer(): EnumeratorStringDescriptor =
        EnumeratorStringDescriptor.INSTANCE

    override fun getVersion() = 1

    override fun getInputFilter() = FileBasedIndex.InputFilter { it.fileType == L10nFileType }

    override fun dependsOnFileContent() = true

}