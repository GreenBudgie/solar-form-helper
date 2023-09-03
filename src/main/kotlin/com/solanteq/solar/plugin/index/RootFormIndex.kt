package com.solanteq.solar.plugin.index

import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.solanteq.solar.plugin.file.RootFormFileType

/**
 * An index that stores root forms by their respective key as `module.name`
 */
class RootFormIndex : ScalarIndexExtension<String>() {

    override fun getName() = ROOT_FORM_INDEX_NAME

    override fun getIndexer() = DataIndexer<String, Void, FileContent> { fileContent ->
        val file = fileContent.file
        val name = file.nameWithoutExtension
        val module = file.parent?.name ?: return@DataIndexer emptyMap()
        val formName = "$module.$name"
        return@DataIndexer mapOf(formName to null)
    }

    override fun getKeyDescriptor(): EnumeratorStringDescriptor =
        EnumeratorStringDescriptor.INSTANCE

    override fun getVersion() = 1

    override fun getInputFilter() = FileBasedIndex.InputFilter { it.fileType == RootFormFileType }

    override fun dependsOnFileContent() = false

}

val ROOT_FORM_INDEX_NAME = ID.create<String, Void>("RootFormIndex")