package com.solanteq.solar.plugin.l10n

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.solanteq.solar.plugin.index.FormNameL10nShortIndex
import com.solanteq.solar.plugin.l10n.search.FormL10nSearch
import org.jetbrains.kotlin.idea.core.util.toPsiFile

object L10nSearchQueryUtil {

    fun getPropertyKeysForL10nKeys(
        formL10nKeys: List<String>,
        matchL10nKeys: List<String>,
        globalSearchScope: GlobalSearchScope,
        project: Project
    ): List<JsonStringLiteral> {
        val filesToSearch = formL10nKeys.flatMap {
            FormNameL10nShortIndex.getFilesContainingFormL10n(it, globalSearchScope)
        }.distinct()
        val l10nProperties = filesToSearch.flatMap { file ->
            val psiFile = file.toPsiFile(project) as? JsonFile ?: return@flatMap emptyList()
            val properties = FormL10nSearch.findL10nPropertiesInFile(psiFile)
            getPropertiesMatchingKeys(properties, matchL10nKeys)
        }
        return l10nProperties.mapNotNull { it.nameElement as? JsonStringLiteral }
    }

    private fun getPropertiesMatchingKeys(properties: List<JsonProperty>,
                                          keys: List<String>): List<JsonProperty> {
        return properties.filter { property ->
            val propertyName = property.name
            keys.any { key ->
                propertyName.startsWith(key)
            }
        }
    }

}