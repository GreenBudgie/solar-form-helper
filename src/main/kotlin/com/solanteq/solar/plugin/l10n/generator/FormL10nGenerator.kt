package com.solanteq.solar.plugin.l10n.generator

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.l10n.L10nEntry
import com.solanteq.solar.plugin.l10n.search.FormL10nSearch
import com.solanteq.solar.plugin.l10n.withSameFormAndLocaleAs
import org.jetbrains.kotlin.idea.base.util.fileScope

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
     * (using form name short index) with the same `module`.
     * New localization property will be placed at the end of the file.
     * - If file is still not found, we try to find a file by its name. We search for files that contain the form
     * `module` in its name, and prioritize files with `form(s)` in name.
     * New localization property will be placed at the end of the file.
     * - At last, if still nothing is found, returns null
     */
    fun findBestPlacement(
        element: FormLocalizableElement<*>,
        entry: L10nEntry,
        preferredL10nFile: JsonFile? = null,
    ): L10nPlacement? {
        val placementByRelatedElements = tryFindPlacementForParent(element, element, entry, preferredL10nFile)

        return placementByRelatedElements
    }

    private fun tryFindPlacementForParent(
        originalElement: FormLocalizableElement<*>,
        element: FormElement<*>,
        originalEntry: L10nEntry,
        preferredL10nFile: JsonFile?,
    ): L10nPlacement? {
        if (originalElement != element) {
            val l10nProperty = tryFindL10nProperty(element, originalEntry, preferredL10nFile)
            if (l10nProperty != null) {
                return L10nPlacement.after(l10nProperty.containingFile as JsonFile, l10nProperty)
            }
        }

        element.parents.forEach { parent ->
            val children = parent.children
            val indexOfCurrentElement = children.indexOf(element)
            if (indexOfCurrentElement == -1) {
                return@forEach
            }

            val childrenBeforeElement = children.subList(0, indexOfCurrentElement).reversed()
            childrenBeforeElement.forEach { child ->
                val placement = tryFindPlacementForChild(child, originalEntry, preferredL10nFile, searchingAfterParent = false)
                if (placement != null) {
                    return placement
                }
            }

            val childrenAfterElement = children.subList(indexOfCurrentElement + 1, children.size)
            childrenAfterElement.forEach { child ->
                val placement = tryFindPlacementForChild(child, originalEntry, preferredL10nFile, searchingAfterParent = true)
                if (placement != null) {
                    return placement
                }
            }

            val placement = tryFindPlacementForParent(originalElement, parent, originalEntry, preferredL10nFile)
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
    ): L10nPlacement? {
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
            val placement = tryFindPlacementForChild(child, originalEntry, preferredL10nFile, searchingAfterParent)
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
        l10nFile: JsonFile?,
    ): JsonProperty? {
        if (element !is FormLocalizableElement<*>) {
            return null
        }

        val entry = element.l10nEntries.withSameFormAndLocaleAs(originalEntry) ?: return null
        val searchQuery = FormL10nSearch.search(element.project, entry)
        if (l10nFile != null) {
            searchQuery.inScope(l10nFile.fileScope())
        }

        return searchQuery.findFirstProperty()
    }

}