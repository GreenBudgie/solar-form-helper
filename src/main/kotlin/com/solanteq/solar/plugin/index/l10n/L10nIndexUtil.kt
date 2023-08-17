package com.solanteq.solar.plugin.index.l10n

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.FileContent
import com.solanteq.solar.plugin.l10n.L10nLocale
import com.solanteq.solar.plugin.l10n.search.FormL10nSearch
import com.solanteq.solar.plugin.util.valueAsStringOrEmpty
import org.jetbrains.kotlin.konan.file.File

object L10nIndexUtil {

    /**
     * Tries to get l10n locale based on provided virtual file or returns null if it has invalid path
     */
    fun getL10nLocaleByVirtualFile(file: VirtualFile): L10nLocale? {
        val path = file.path
        val lastSeparatorIndex = path.lastIndexOf(File.separatorChar)
        if (lastSeparatorIndex == -1) {
            return null
        }
        val parentPath = path.substring(0, lastSeparatorIndex)
        val parentLastSeparatorIndex = parentPath.lastIndexOf(File.separatorChar)
        val parentDirectoryName = parentPath.substring(parentLastSeparatorIndex + 1)
        return L10nLocale.getByDirectoryName(parentDirectoryName)
    }

    fun buildL10nIndexMap(fileContent: FileContent, propertyPredicate: (JsonProperty) -> Boolean): Map<L10nIndexKey, String> {
        val file = fileContent.psiFile as? JsonFile ?: return emptyMap()
        val fileLocale = getL10nLocaleByVirtualFile(fileContent.file) ?: return emptyMap()
        val properties = FormL10nSearch.findL10nPropertiesInFile(file)
        val formL10nProperties = properties.filter { propertyPredicate(it) }
        return formL10nProperties.associate {
            L10nIndexKey(it.name, fileLocale) to it.valueAsStringOrEmpty()
        }
    }

}