package com.solanteq.solar.plugin.reference.include

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.solanteq.solar.plugin.asset.Icons
import com.solanteq.solar.plugin.element.FormJsonInclude
import com.solanteq.solar.plugin.search.FormModuleSearch
import com.solanteq.solar.plugin.search.FormSearch
import com.solanteq.solar.plugin.util.convert
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.kotlin.idea.core.util.toPsiDirectory
import org.jetbrains.kotlin.idea.core.util.toPsiFile

class JsonIncludeReference(
    element: JsonStringLiteral,
    textRange: TextRange,
    private val referencedVirtualFile: VirtualFile?,
    /**
     * Nesting level of this file or directory (reversed)
     * - 0 for form file
     * - 1 for parent directory
     * - 2 for parent directory of parent directory
     * and so on
     */
    private val pathIndex: Int,
    val jsonIncludeElement: FormJsonInclude
) : PsiReferenceBase<JsonStringLiteral>(element, textRange)  {

    override fun bindToElement(element: PsiElement): PsiElement {
        return element
    }

    override fun getVariants(): Array<LookupElementBuilder> {
        val pathChain = jsonIncludeElement.pathChain.dropLast(pathIndex + 1).convert()
        val path = pathChain.strings.joinToString("/")
        val directories = findApplicableDirectories(pathChain.size)
        val forms = FormSearch.findIncludedForms(element.project.allScope())
        val virtualFiles = directories + forms
        return virtualFiles
            .filter { isPathApplicable(path, it) }
            .map { createLookup(it) }
            .toTypedArray()
    }

    override fun resolve(): PsiElement? {
        referencedVirtualFile ?: return null
        return if(referencedVirtualFile.isDirectory) {
            referencedVirtualFile.toPsiDirectory(element.project)
        } else {
            referencedVirtualFile.toPsiFile(element.project)
        }
    }

    fun isDirectoryReference() = referencedVirtualFile?.isDirectory == true

    private fun findApplicableDirectories(pathDepth: Int): List<VirtualFile> {
        val baseDirectories = FormModuleSearch.findProjectIncludedFormBaseDirectories(element.project) +
                FormModuleSearch.findLibrariesIncludedFormDirectories(element.project)
        var currentDirectories = baseDirectories
        repeat(pathDepth) {
            currentDirectories = currentDirectories
                .flatMap { it.children.toList() }
                .filter { it.isDirectory }
        }
        return currentDirectories
    }

    private fun isPathApplicable(path: String, virtualFile: VirtualFile): Boolean {
        val virtualFilePath = virtualFile.path
        val relativePath = virtualFilePath.substring(0, virtualFilePath.length - virtualFile.name.length - 1)
        if(path.isEmpty()) {
            return relativePath.endsWith("includes/forms")
        }
        return relativePath.endsWith(path)
    }

    private fun createLookup(virtualFile: VirtualFile): LookupElementBuilder {
        val baseLookup = LookupElementBuilder.create(virtualFile.name)
        if(virtualFile.isDirectory) {
            val directory = virtualFile.toPsiDirectory(element.project) ?: return baseLookup
            return baseLookup.withIcon(directory.getIcon(0))
        }
        return baseLookup.withIcon(Icons.INCLUDED_FORM_ICON)
    }

}