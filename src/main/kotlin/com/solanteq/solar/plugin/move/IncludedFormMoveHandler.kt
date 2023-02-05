package com.solanteq.solar.plugin.move

import com.intellij.json.psi.JsonFile
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFileHandler
import com.intellij.refactoring.util.MoveRenameUsageInfo
import com.intellij.usageView.UsageInfo
import com.solanteq.solar.plugin.file.IncludedFormFileType
import com.solanteq.solar.plugin.reference.include.JsonIncludeReference
import com.solanteq.solar.plugin.util.isIncludedFormDirectory
import com.solanteq.solar.plugin.util.restrictedByFormFiles
import com.solanteq.solar.plugin.util.textRangeWithoutQuotes
import org.jetbrains.kotlin.idea.base.util.projectScope

class IncludedFormMoveHandler : MoveFileHandler() {

    override fun canProcessElement(element: PsiFile) = element.fileType == IncludedFormFileType

    override fun prepareMovedFile(
        file: PsiFile,
        moveDestination: PsiDirectory,
        oldToNewMap: MutableMap<PsiElement, PsiElement>
    ) {}

    override fun findUsages(
        psiFile: PsiFile,
        newParent: PsiDirectory,
        searchInComments: Boolean,
        searchInNonJavaFiles: Boolean
    ): List<UsageInfo> {
        if(!newParent.isIncludedFormDirectory()) return emptyList()
        val scope = psiFile.project.projectScope().restrictedByFormFiles()
        return ReferencesSearch.search(psiFile, scope)
            .filterIsInstance<JsonIncludeReference>()
            .map { MoveRenameUsageInfo(it, psiFile) }
    }

    override fun retargetUsages(usageInfos: List<UsageInfo>, oldToNewMap: Map<PsiElement, PsiElement>) {
        usageInfos
            .filterIsInstance<MoveRenameUsageInfo>()
            .forEach {
                val reference = it.reference as? JsonIncludeReference ?: return@forEach
                val movedFile = it.referencedElement as? JsonFile
                    ?: it.upToDateReferencedElement as? JsonFile ?: return@forEach
                retargetReference(reference, movedFile)
            }
    }

    override fun updateMovedFile(file: PsiFile) {}

    private fun retargetReference(reference: JsonIncludeReference, movedFile: JsonFile) {
        val parentDirectory = movedFile.parent ?: return
        if(!parentDirectory.isIncludedFormDirectory()) return

        val newPath = getNewPath(parentDirectory, "")
        val newFullPath = newPath + movedFile.name
        val prefix = reference.jsonIncludeElement.type.prefix
        val newStringLiteralText = prefix + newFullPath

        val element = reference.element
        ElementManipulators.getManipulator(element).handleContentChange(
            element,
            element.textRangeWithoutQuotes,
            newStringLiteralText
        )
    }

    private fun getNewPath(directory: PsiDirectory?, resultPath: String): String {
        if(directory == null || directory.name == "forms") return resultPath
        return getNewPath(directory.parent, "${directory.name}/$resultPath")
    }

}