package com.solanteq.solar.plugin.index

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.solanteq.solar.plugin.element.FormJsonInclude
import com.solanteq.solar.plugin.util.isForm

/**
 * An index that stores JSON files that contain specific json include declarations.
 * - Key: full name and relative path of included form file with extension,
 * for example: `crm/includedForm.json`
 * - Value: a list of files that contain one or more declarations of this form
 */
class JsonIncludeDeclarationIndex : ScalarIndexExtension<String>() {

    override fun getName() = JSON_INCLUDE_DECLARATION_INDEX_NAME

    override fun getIndexer() = DataIndexer<String, Void, FileContent> { fileContent ->
        val file = fileContent.psiFile as? JsonFile ?: return@DataIndexer emptyMap()
        val jsonIncludesMap = mutableMapOf<String, Void?>()
        PsiTreeUtil.processElements(file, JsonStringLiteral::class.java) {
            val jsonInclude = FormJsonInclude.createFrom(it) ?: return@processElements true
            if(jsonInclude.formName == null) return@processElements true
            jsonIncludesMap[jsonInclude.path] = null
            true
        }
        jsonIncludesMap
    }

    override fun getKeyDescriptor(): EnumeratorStringDescriptor =
        EnumeratorStringDescriptor.INSTANCE

    override fun getVersion() = 1

    override fun getInputFilter() = FileBasedIndex.InputFilter { it.isForm() }

    override fun dependsOnFileContent() = true

}

val JSON_INCLUDE_DECLARATION_INDEX_NAME = ID.create<String, Void>("JsonIncludeDeclarationsIndex")

object JsonIncludeDeclarationSearch {

    /**
     * Returns the list of [VirtualFile]s that contain at least one json include declaration
     * that leads to included form with [includedFormRelativePath].
     */
    fun getFilesContainingDeclaration(includedFormRelativePath: String,
                                      scope: GlobalSearchScope
    ): Collection<VirtualFile> =
        FileBasedIndex.getInstance().getContainingFiles(
            JSON_INCLUDE_DECLARATION_INDEX_NAME,
            includedFormRelativePath,
            scope
        )
}