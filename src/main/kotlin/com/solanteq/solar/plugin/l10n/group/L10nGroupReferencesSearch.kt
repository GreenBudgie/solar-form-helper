package com.solanteq.solar.plugin.l10n.group

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.model.psi.PsiSymbolReferenceService
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.solanteq.solar.plugin.file.L10nFileType
import com.solanteq.solar.plugin.symbol.FormSymbol
import com.solanteq.solar.plugin.symbol.FormSymbolReference
import org.jetbrains.kotlin.idea.base.util.projectScope
import org.jetbrains.kotlin.idea.core.util.toPsiFile

object L10nGroupReferencesSearch {

    private val key = Key<CachedValue<List<FormSymbolReference>>>("solar.l10n.groupReference")

    fun findReferencesInAllScope(
        resolveTarget: FormSymbol
    ): List<FormSymbolReference> {
        return CachedValuesManager.getCachedValue(resolveTarget.element, key) {
            CachedValueProvider.Result(
                findReferences(resolveTarget),
                PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }

    private fun findReferences(
        resolveTarget: FormSymbol
    ): List<FormSymbolReference> {
        val l10nFiles = FileTypeIndex.getFiles(L10nFileType, resolveTarget.element.project.projectScope())
        return l10nFiles.flatMap {
            findReferencesInFile(it, resolveTarget)
        }
    }

    private fun findReferencesInFile(file: VirtualFile, resolveTarget: FormSymbol): List<FormSymbolReference> {
        val jsonFile = file.toPsiFile(resolveTarget.element.project) as? JsonFile ?: return emptyList()
        val topLevelObject = jsonFile.topLevelValue as? JsonObject ?: return emptyList()
        val propertyKeys = topLevelObject.propertyList.mapNotNull {
            it.nameElement as? JsonStringLiteral
        }
        return propertyKeys.mapNotNull {
            findReferenceInElement(it, resolveTarget)
        }
    }

    private fun findReferenceInElement(
        element: JsonStringLiteral,
        resolveTarget: FormSymbol
    ): FormSymbolReference? {
        val references = PsiSymbolReferenceService.getService().getReferences(element)
        val groupReferences = references.filterIsInstance<L10nGroupSymbolReference>()
        val applicableReferences = groupReferences.filter { reference ->
            reference.resolvesTo(resolveTarget)
        }
        return applicableReferences.firstOrNull()
    }

}