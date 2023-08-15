package com.solanteq.solar.plugin.l10n.search

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.ID
import com.solanteq.solar.plugin.file.L10nFileType
import com.solanteq.solar.plugin.l10n.L10n
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.kotlin.idea.core.util.toPsiFile

abstract class L10nSearchBase<T : L10n>(
    private val indexName: ID<String, String>
) {

    protected abstract fun createL10n(property: JsonProperty): T?

    /**
     * Returns all localization values associated with the given key,
     * or empty list if this key is not present in any file or invalid.
     *
     * If you only need l10n values consider using [findL10nValuesByKey]
     *
     * TODO make distinct indexes for EN and RU l10ns
     */
    fun findL10nsByKey(key: String, project: Project, scope: GlobalSearchScope = project.allScope()): List<T> {
        val containingFiles = FileBasedIndex.getInstance().getContainingFiles(
            indexName, key, scope
        )
        return runReadAction {
            val psiFiles = containingFiles.mapNotNull { it.toPsiFile(project) as? JsonFile }
            val properties = psiFiles.flatMap { findL10nPropertiesInFile(it) }
            val applicableProperties = properties.filter { it.name == key }
            applicableProperties.mapNotNull { createL10n(it) }
        }
    }

    /**
     * Returns all localization values associated with the given key,
     * or empty list if this key is not present in any file or invalid.
     *
     * This method has very good performance because it uses index
     */
    fun findL10nValuesByKey(key: String, scope: GlobalSearchScope): List<String> =
        FileBasedIndex.getInstance().getValues(indexName, key, scope)

    /**
     * Returns all [VirtualFile]s that contain the specified keys.
     */
    fun getFilesContainingKeys(keys: List<String>, scope: GlobalSearchScope): Collection<VirtualFile> =
        keys.flatMap {
            FileBasedIndex.getInstance().getContainingFiles(indexName, it, scope)
        }.distinct()

    fun findL10nPropertiesInFile(file: JsonFile): List<JsonProperty> {
        if(file.fileType != L10nFileType) return emptyList()
        val topLevelObject = file.topLevelValue as? JsonObject ?: return emptyList()
        return topLevelObject.propertyList
    }

}