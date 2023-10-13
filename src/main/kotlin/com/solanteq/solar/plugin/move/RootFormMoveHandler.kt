package com.solanteq.solar.plugin.move

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFileHandler
import com.intellij.refactoring.util.MoveRenameUsageInfo
import com.intellij.usageView.UsageInfo
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.file.IncludedFormFileType
import com.solanteq.solar.plugin.file.L10nFileType
import com.solanteq.solar.plugin.file.RootFormFileType
import com.solanteq.solar.plugin.l10n.module.L10nModulePsiReference
import com.solanteq.solar.plugin.reference.form.FormModuleReference
import com.solanteq.solar.plugin.util.isRootFormModule
import org.jetbrains.kotlin.idea.base.util.projectScope

class RootFormMoveHandler : MoveFileHandler() {

    override fun canProcessElement(element: PsiFile) = element.fileType == RootFormFileType

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
        if(!newParent.isRootFormModule()) return emptyList()
        val scope = GlobalSearchScope.getScopeRestrictedByFileTypes(
            psiFile.project.projectScope(),
            RootFormFileType,
            IncludedFormFileType,
            L10nFileType
        )
        return ReferencesSearch.search(psiFile, scope).mapNotNull { fileReference ->
            val stringLiteral = fileReference.element as? JsonStringLiteral ?: return@mapNotNull null
            stringLiteral.references.forEach {
                if(it is FormModuleReference || it is L10nModulePsiReference) {
                    return@mapNotNull MoveRenameUsageInfo(it, psiFile)
                }
            }
            null
        }
    }

    override fun retargetUsages(usageInfos: List<UsageInfo>, oldToNewMap: Map<PsiElement, PsiElement>) {
        usageInfos
            .filterIsInstance<MoveRenameUsageInfo>()
            .forEach {
                val reference = it.reference ?: return@forEach
                val newDirectory = it.referencedElement?.parent
                    ?: it.upToDateReferencedElement?.parent ?: return@forEach
                reference.bindToElement(newDirectory)
            }
    }

    override fun updateMovedFile(file: PsiFile) {
        val jsonFile = file as? JsonFile ?: return
        val directory = file.parent ?: return
        if(!directory.isRootFormModule()) return
        val newModuleName = directory.name
        val formRootElement = FormRootFile.createFrom(jsonFile) ?: return
        val moduleProperty = formRootElement.moduleProperty ?: return
        val modulePropertyValue = moduleProperty.value as? JsonStringLiteral ?: return
        ElementManipulators.handleContentChange(modulePropertyValue, newModuleName)
    }

}