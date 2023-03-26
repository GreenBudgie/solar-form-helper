package com.solanteq.solar.plugin.index

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.solanteq.solar.plugin.file.L10nFileType
import com.solanteq.solar.plugin.l10n.FormL10n
import com.solanteq.solar.plugin.l10n.search.FormL10nSearch
import org.jetbrains.kotlin.utils.keysToMap

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

    override fun getName() = NAME

    override fun getIndexer() = DataIndexer<String, Void, FileContent> { fileContent ->
        val file = fileContent.psiFile as? JsonFile ?: return@DataIndexer emptyMap()
        val properties = FormL10nSearch.findL10nPropertiesInFile(file)
        val moduleAndFormNameSet = mutableSetOf<String>()
        properties.forEach {
            if(!FormL10n.isFormL10n(it)) {
                return@forEach
            }
            val keyElement = it.nameElement as JsonStringLiteral
            val split = keyElement.value.split(".")
            if(split.size < 3) {
                return@forEach
            }
            val moduleName = split[0]
            val formName = split[2]
            moduleAndFormNameSet.add("$moduleName.form.$formName")
        }
        moduleAndFormNameSet.keysToMap { null }
    }

    override fun getKeyDescriptor(): EnumeratorStringDescriptor =
        EnumeratorStringDescriptor.INSTANCE

    override fun getVersion() = 1

    override fun getInputFilter() = FileBasedIndex.InputFilter { it.fileType == L10nFileType }

    override fun dependsOnFileContent() = true

    companion object {

        val NAME = ID.create<String, Void>("FormNameL10nShortIndex")

        /**
         * Returns the list of [VirtualFile]s that contain at least one l10n key
         */
        fun getFilesContainingFormL10n(key: String,
                                       scope: GlobalSearchScope): Collection<VirtualFile> =
            FileBasedIndex.getInstance().getContainingFiles(
                NAME,
                key,
                scope
            )

    }

}