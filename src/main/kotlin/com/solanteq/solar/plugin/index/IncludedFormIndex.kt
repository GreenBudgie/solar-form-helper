package com.solanteq.solar.plugin.index

import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.solanteq.solar.plugin.element.FormIncludedFile
import com.solanteq.solar.plugin.file.IncludedFormFileType

/**
 * An index that stores included forms by their relative path like `bo/tariff/includedForm.json`
 */
class IncludedFormIndex : ScalarIndexExtension<String>() {

    override fun getName() = INCLUDED_FORM_INDEX_NAME

    override fun getIndexer() = DataIndexer<String, Void, FileContent> { fileContent ->
        val file = fileContent.file
        val relativePath = FormIncludedFile.getRelativePathByIncludedFormVirtualFile(file)
            ?: return@DataIndexer emptyMap()
        return@DataIndexer mapOf(relativePath to null)
    }

    override fun getKeyDescriptor(): EnumeratorStringDescriptor =
        EnumeratorStringDescriptor.INSTANCE

    override fun getVersion() = 1

    override fun getInputFilter() = FileBasedIndex.InputFilter { it.fileType == IncludedFormFileType }

    override fun dependsOnFileContent() = false

}

val INCLUDED_FORM_INDEX_NAME = ID.create<String, Void>("IncludedFormIndex")