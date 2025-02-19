package com.solanteq.solar.plugin.l10n.generator

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.file.L10nFileType
import com.solanteq.solar.plugin.l10n.L10nEntry
import com.solanteq.solar.plugin.l10n.L10nLocale
import com.solanteq.solar.plugin.l10n.search.FormL10nSearch
import com.solanteq.solar.plugin.l10n.withSameFormAndLocaleAs
import com.solanteq.solar.plugin.search.FormSearch
import org.jetbrains.kotlin.idea.base.util.fileScope
import org.jetbrains.kotlin.idea.base.util.minus
import org.jetbrains.kotlin.idea.base.util.projectScope
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.kotlin.idea.util.projectStructure.getModule

object FormL10nGenerator {

    /**
     * Finds best placement for [element] localization.
     *
     * How it works:
     * - At first, we try to find localizations of other [FormLocalizableElement]s in this form. It traverses the
     * tree of parents/children of this element and tries to find already existing localization for them.
     * If it's found - we place new l10n property before or after the closest element, depending on where in
     * tree it has been found.
     * - If no elements in current form are localized, we search for localizations of other forms
     * (using form name short index) with the same `module`. Forms within the same artifact are prioritized.
     * New localization property will be placed at the end of the file.
     * - If file is still not found, we try to find a file by its name. We search for files that contain the form
     * `module` in its name, and prioritize files with `form(s)` in name.
     * New localization property will be placed at the end of the file.
     * - If nothing found, try to find **any** l10n file in the same module as the current file or all root forms.
     * - At last, if still nothing is found, returns the placement at the end of [preferredL10nFile]. If it is not
     * provided, returns null.
     *
     * @param element Element to search the best placement for
     * @param entry L10n entry
     * @param preferredL10nFile Optional l10n file, usually selected by user from UI, that overrides the
     * search scope. If not null, the method will only search for placement in provided file.
     */
    fun findBestPlacement(
        element: FormLocalizableElement<*>,
        entry: L10nEntry,
        preferredL10nFile: JsonFile? = null,
    ): L10nPlacement? {
        val placementByRelatedElements = tryFindPlacementForParent(element, element, entry, preferredL10nFile)
        if (placementByRelatedElements != null) {
            return placementByRelatedElements
        }

        if (preferredL10nFile != null) {
            return L10nPlacement.endOfFile(preferredL10nFile)
        }

        val placementByFormsInSameModule = findPlacementByFormsInSameModule(element, entry)
        if (placementByFormsInSameModule != null) {
            return placementByRelatedElements
        }

        return findPlacementByL10nFileName(element, entry)
    }

    private fun findPlacementByL10nFileName(element: FormLocalizableElement<*>, entry: L10nEntry): L10nPlacement? {
        val moduleNames = element.containingRootForms.mapNotNull { it.moduleName }
        val rootFormArtifacts = element.containingRootForms
            .mapNotNull { it.virtualFile }
            .mapNotNull { it.getModule(element.project) }
        val allModulesScope = rootFormArtifacts
            .map { it.moduleScope }
            .reduce { prevModuleScope, currentModuleScope -> prevModuleScope.uniteWith(currentModuleScope) }

        val relevantL10nFiles = FileTypeIndex.getFiles(L10nFileType, allModulesScope).filterOutWrongLocale(entry)

        if (relevantL10nFiles.isEmpty()) {
            return null
        }

        if (relevantL10nFiles.size == 1) {
            val file = relevantL10nFiles.first().toPsiFile(element.project) as? JsonFile ?: return null
            return L10nPlacement.endOfFile(file)
        }

        val l10nFileByModuleName = relevantL10nFiles.firstOrNull { file ->
            moduleNames.any { moduleName -> file.name.startsWith(moduleName) }
        }?.toPsiFile(element.project) as? JsonFile

        if (l10nFileByModuleName != null) {
            return L10nPlacement.endOfFile(l10nFileByModuleName)
        }

        val l10nFileByFormStringInName = relevantL10nFiles.firstOrNull { file ->
            moduleNames.any { moduleName -> file.name.contains("form") }
        }?.toPsiFile(element.project) as? JsonFile

        if (l10nFileByFormStringInName != null) {
            return L10nPlacement.endOfFile(l10nFileByFormStringInName)
        }

        val file = relevantL10nFiles.first().toPsiFile(element.project) as? JsonFile ?: return null
        return L10nPlacement.endOfFile(file)
    }

    private fun findPlacementByFormsInSameModule(
        element: FormLocalizableElement<*>,
        entry: L10nEntry,
    ): L10nPlacement? {
        val project = element.project
        val elementFormModules = element.containingRootForms.mapNotNull { it.moduleName }
        val elementArtifacts = element.containingRootForms.mapNotNull { it.virtualFile?.getModule(project) }
        val containingFormsVirtualFiles = element.containingRootForms.mapNotNull { it.virtualFile }
        val containingFormsScope = GlobalSearchScope.filesScope(project, containingFormsVirtualFiles)
        val projectScopeWithoutContainingForms = project.projectScope().minus(containingFormsScope)

        val formsWithSameModule = FormSearch.findRootFormsInModules(
            projectScopeWithoutContainingForms,
            *elementFormModules.toTypedArray()
        ).filterOutWrongLocale(entry)

        val formsWithSameModulePrioritizedByArtifact = formsWithSameModule.prioritizeBy {
            it.getModule(project) in elementArtifacts
        }

        formsWithSameModulePrioritizedByArtifact.forEach { formFile ->
            val formPsiFile = formFile.toPsiFile(project) as? JsonFile ?: return@forEach
            val formElement = FormRootFile.createFrom(formPsiFile) ?: return@forEach
            val l10nFile = FormL10nSearch.findFileContainingFormL10n(formElement, entry.locale) ?: return@forEach
            val psiL10nFile = l10nFile.toPsiFile(project) as? JsonFile ?: return@forEach
            return L10nPlacement.endOfFile(psiL10nFile)
        }

        return null
    }

    private fun Collection<VirtualFile>.filterOutWrongLocale(entry: L10nEntry) = filter {
        L10nLocale.getByFile(it) == entry.locale
    }

    private fun tryFindPlacementForParent(
        originalElement: FormLocalizableElement<*>,
        element: FormElement<*>,
        originalEntry: L10nEntry,
        preferredL10nFile: JsonFile?,
        processedElements: MutableSet<FormElement<*>> = mutableSetOf()
    ): L10nPlacement? {
        if (element in processedElements) {
            return null
        }
        processedElements += element

        val isOriginalElement = originalElement == element
        if (!isOriginalElement) {
            val l10nProperty = tryFindL10nProperty(element, originalEntry, preferredL10nFile)
            if (l10nProperty != null) {
                return L10nPlacement.after(l10nProperty.containingFile as JsonFile, l10nProperty)
            }
        }

        var processingElementWithNoParents = false
        val elementsToProcess = if (element.parents.isEmpty() && isOriginalElement) {
            processingElementWithNoParents = true
            listOf(element)
        } else {
            element.parents
        }

        elementsToProcess.forEach { parent ->
            val children = parent.children
            val indexOfCurrentElement = children.indexOf(element)
            if (!processingElementWithNoParents && indexOfCurrentElement == -1) {
                return@forEach
            }

            // Reverse the list since we should search from bottom to top. Bottom elements are closer to the current
            // element, so we consider them first
            val childrenBeforeElement = if (processingElementWithNoParents) {
                emptyList()
            } else {
                children.subList(0, indexOfCurrentElement).reversed()
            }
            childrenBeforeElement.forEach { child ->
                val placement =
                    tryFindPlacementForChild(child, originalEntry, preferredL10nFile, searchingAfterParent = false, processedElements)
                if (placement != null) {
                    return placement
                }
            }

            val childrenAfterElement = if (processingElementWithNoParents) {
                children
            } else {
                children.subList(indexOfCurrentElement + 1, children.size)
            }
            childrenAfterElement.forEach { child ->
                val placement =
                    tryFindPlacementForChild(child, originalEntry, preferredL10nFile, searchingAfterParent = true, processedElements)
                if (placement != null) {
                    return placement
                }
            }

            if (processingElementWithNoParents) {
                return null
            }

            val placement = tryFindPlacementForParent(originalElement, parent, originalEntry, preferredL10nFile, processedElements)
            if (placement != null) {
                return placement
            }
        }

        return null
    }

    private fun tryFindPlacementForChild(
        element: FormElement<*>,
        originalEntry: L10nEntry,
        preferredL10nFile: JsonFile?,
        searchingAfterParent: Boolean,
        processedElements: MutableSet<FormElement<*>>
    ): L10nPlacement? {
        if (element in processedElements) {
            return null
        }
        processedElements += element

        if (searchingAfterParent) {
            val l10nProperty = tryFindL10nProperty(element, originalEntry, preferredL10nFile)
            if (l10nProperty != null) {
                return L10nPlacement.before(l10nProperty.containingFile as JsonFile, l10nProperty)
            }
        }

        val childrenInCorrectOrder = if (searchingAfterParent) {
            element.children
        } else {
            element.children.reversed()
        }

        childrenInCorrectOrder.forEach { child ->
            val placement = tryFindPlacementForChild(child, originalEntry, preferredL10nFile, searchingAfterParent, processedElements)
            if (placement != null) {
                return placement
            }
        }

        if (!searchingAfterParent) {
            val l10nProperty = tryFindL10nProperty(element, originalEntry, preferredL10nFile)
            if (l10nProperty != null) {
                return L10nPlacement.after(l10nProperty.containingFile as JsonFile, l10nProperty)
            }
        }

        return null
    }

    private fun tryFindL10nProperty(
        element: FormElement<*>,
        originalEntry: L10nEntry,
        preferredL10nFile: JsonFile?,
    ): JsonProperty? {
        if (element !is FormLocalizableElement<*>) {
            return null
        }

        val entry = element.l10nEntries.withSameFormAndLocaleAs(originalEntry) ?: return null
        val searchQuery = FormL10nSearch.search(element.project, entry)
        if (preferredL10nFile != null) {
            searchQuery.inScope(preferredL10nFile.fileScope())
        }

        return searchQuery.findFirstProperty()
    }

    private fun <T> Collection<T>.prioritizeBy(condition: (T) -> Boolean): List<T> {
        val (trueList, falseList) = partition(condition)
        return trueList + falseList
    }

}