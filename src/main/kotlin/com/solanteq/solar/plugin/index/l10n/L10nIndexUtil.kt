package com.solanteq.solar.plugin.index.l10n

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.FileContent
import com.solanteq.solar.plugin.l10n.L10nLocale
import com.solanteq.solar.plugin.l10n.search.FormL10nSearch
import com.solanteq.solar.plugin.util.valueAsStringOrEmpty

object L10nIndexUtil {

    fun buildL10nIndexMap(fileContent: FileContent, propertyPredicate: (JsonProperty) -> Boolean): Map<L10nIndexKey, String> {
        val file = fileContent.psiFile as? JsonFile ?: return emptyMap()
        val fileLocale = getL10nLocaleByFile(fileContent.file) ?: return emptyMap()
        val properties = FormL10nSearch.findL10nPropertiesInFile(file)
        val formL10nProperties = properties.filter { propertyPredicate(it) }
        return formL10nProperties.associate {
            L10nIndexKey(it.name, fileLocale) to it.valueAsStringOrEmpty()
        }
    }

    private fun getL10nLocaleByFile(file: VirtualFile): L10nLocale? {
        return L10nLocale.getByDirectoryName(file.parent.name)
    }

}