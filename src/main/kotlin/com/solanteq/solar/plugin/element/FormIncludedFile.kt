package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.solanteq.solar.plugin.element.base.AbstractFormElement
import com.solanteq.solar.plugin.element.base.FormFile
import com.solanteq.solar.plugin.element.creator.FormElementCreator
import com.solanteq.solar.plugin.file.IncludedFormFileType
import com.solanteq.solar.plugin.index.JsonIncludeDeclarationSearch
import com.solanteq.solar.plugin.search.FormGraphSearch
import com.solanteq.solar.plugin.util.restrictedByFormFiles
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.kotlin.idea.base.util.minus
import org.jetbrains.kotlin.idea.core.util.toPsiFile

/**
 * Represents an included form file.
 *
 * Each included form is referenced via [FormJsonInclude] element from root or other included forms.
 */
class FormIncludedFile(
    sourceElement: JsonFile
) : AbstractFormElement<JsonFile>(sourceElement), FormFile {

    override val containingForm = this

    /**
     * Returns relative path of this form including its name with extension, or null if there is
     * no virtual file is found.
     * Relative path is a path until `forms` directory, for example:
     * `.../includes/forms/bo/tariff/includedForm.json` -> `bo/tariff/includedForm.json`
     *
     * Can be used as a key for index search.
     */
    val relativePath by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val virtualFile = virtualFile ?: return@lazy null
        return@lazy getRelativePathByIncludedFormVirtualFile(virtualFile)
    }

    /**
     * Finds [FormJsonInclude] elements that have a reference to this form.
     * Uses all scope to search for dependencies.
     *
     * Does not return declarations in the same file to prevent infinite recursion.
     */
    fun findDeclarations(): List<FormJsonInclude> {
        val containingFile = containingFile ?: return emptyList()
        val relativePath = relativePath ?: return emptyList()
        val baseSearchScope = project.allScope()
            .restrictedByFormFiles()
            .minus(GlobalSearchScope.fileScope(containingFile)) as GlobalSearchScope

        val filesToSearch = JsonIncludeDeclarationSearch.getFilesContainingDeclaration(
            relativePath, baseSearchScope
        )

        val effectiveFileScope = GlobalSearchScope.filesScope(project, filesToSearch)

        val references = ProgressManager.getInstance().runProcess<Collection<PsiReference>>({
            ReferencesSearch.search(containingFile, effectiveFileScope).findAll()
        }, EmptyProgressIndicator())

        val referencedJsonElements = references.mapNotNull {
            it.element as? JsonStringLiteral
        }

        return referencedJsonElements.mapNotNull { FormJsonInclude.createFrom(it) }
    }

    /**
     * All root forms that included form can lead to.
     *
     * Traverses up recursively through all included forms until the root form is found.
     */
    val allRootForms: List<FormRootFile> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val virtualFile = virtualFile ?: return@lazy emptyList()
        val rootFormFiles = FormGraphSearch.findTopmostRootForms(project, virtualFile)

        return@lazy rootFormFiles.mapNotNull {
            FormRootFile.createFrom(it.toPsiFile(project) as? JsonFile)
        }
    }

    companion object : FormElementCreator<FormIncludedFile, JsonFile>() {

        override fun doCreate(sourceElement: JsonFile): FormIncludedFile? {
            if(sourceElement.fileType == IncludedFormFileType) {
                return FormIncludedFile(sourceElement)
            }
            return null
        }

        /**
         * Returns relative path of the form virtual file including its name with extension,
         * or null if there is no virtual file is found.
         * Relative path is a path until `forms` directory, for example:
         * `.../includes/forms/bo/tariff/includedForm.json` -> `bo/tariff/includedForm.json`
         *
         * Can be used as a key for index search.
         *
         * This method does not check whether this file is an included form. You should check it beforehand.
         */
        fun getRelativePathByIncludedFormVirtualFile(virtualFile: VirtualFile): String? {
            val pathBuilder = StringBuilder(virtualFile.name)
            var parentDirectory = virtualFile.parent ?: return null
            while (parentDirectory.name != "forms") {
                pathBuilder.insert(0, "${parentDirectory.name}/")
                parentDirectory = parentDirectory.parent ?: return null
            }
            return pathBuilder.toString()
        }

    }

}