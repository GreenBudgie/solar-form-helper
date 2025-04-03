package com.solanteq.solar.plugin.index

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.solanteq.solar.plugin.util.getFormSolarName
import com.solanteq.solar.plugin.util.isForm
import com.solanteq.solar.plugin.util.valueAsStringOrNull

/**
 * An index that stores form files that contain specific root form declarations.
 * - Key: full name of a root form,
 * for example: `bo-csw.agreement`
 * - Value: a list of files that contain one or more declarations of this form
 */
class RootFormDeclarationIndex : ScalarIndexExtension<String>() {

    override fun getName() = ROOT_FORM_DECLARATION_INDEX_NAME

    override fun getIndexer() = DataIndexer<String, Void, FileContent> { fileContent ->
        val file = fileContent.psiFile as? JsonFile ?: return@DataIndexer emptyMap()
        val formDeclarationsMap = mutableMapOf<String, Void?>()
        PsiTreeUtil.processElements(file, JsonProperty::class.java) {
            if (it.name != "form") return@processElements true
            val formName = it.valueAsStringOrNull() ?: return@processElements true
            formDeclarationsMap[formName] = null
            true
        }
        formDeclarationsMap
    }

    override fun getKeyDescriptor(): EnumeratorStringDescriptor =
        EnumeratorStringDescriptor.INSTANCE

    override fun getVersion() = 1

    override fun getInputFilter() = FileBasedIndex.InputFilter { it.isForm() }

    override fun dependsOnFileContent() = true

}

val ROOT_FORM_DECLARATION_INDEX_NAME = ID.create<String, Void>("RootFormDeclarationsIndex")

object RootFormDeclarationSearch {

    /**
     * Returns the list of [VirtualFile]s that contain at least one root form declaration with [fullFormName].
     */
    fun getFilesContainingDeclaration(
        fullFormName: String,
        scope: GlobalSearchScope,
    ): Collection<VirtualFile> =
        FileBasedIndex.getInstance().getContainingFiles(
            ROOT_FORM_DECLARATION_INDEX_NAME,
            fullFormName,
            scope
        )

    /**
     * Returns the list of [VirtualFile]s that contain at least one root form declaration with [fullFormName].
     */
    fun getFilesContainingDeclaration(
        form: VirtualFile,
        scope: GlobalSearchScope,
    ): Collection<VirtualFile> =
        FileBasedIndex.getInstance().getContainingFiles(
            ROOT_FORM_DECLARATION_INDEX_NAME,
            form.getFormSolarName(),
            scope
        )
}