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
import com.solanteq.solar.plugin.index.l10n.L10nIndexKey
import com.solanteq.solar.plugin.l10n.L10n
import com.solanteq.solar.plugin.l10n.L10nLocale
import com.solanteq.solar.plugin.util.asList
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.kotlin.idea.core.util.toPsiFile

abstract class L10nSearchBase<T : L10n>(
    private val indexName: ID<L10nIndexKey, String>
) {

    protected abstract fun createL10n(property: JsonProperty): T?

    fun findL10nPropertiesInFile(file: JsonFile): List<JsonProperty> {
        if(file.fileType != L10nFileType) return emptyList()
        val topLevelObject = file.topLevelValue as? JsonObject ?: return emptyList()
        return topLevelObject.propertyList
    }

    /**
     * Constructs the query to search localizations.
     * Important notes:
     * - It is required to specify one or more keys with [L10nSearchQuery.byKey] or [L10nSearchQuery.byKeys]
     * - If locale is not set by [L10nSearchQuery.withLocale] then localizations with any locale will be found
     * - By default, [Project.allScope] is used as the search scope. Use [L10nSearchQuery.inScope] to override it
     */
    fun search(project: Project) = L10nSearchQuery(project)

    inner class L10nSearchQuery(
        private val project: Project
    ) {

        private var useFormNameShortIndex = false
        private var keys: List<String> = emptyList()
        private var scope: GlobalSearchScope = project.allScope()
        private var locale: L10nLocale? = null

        /**
         * Search by specific key.
         * Only localizations with this key will be found.
         */
        fun byKey(key: String): L10nSearchQuery {
            this.keys = key.asList()
            return this
        }

        /**
         * Search by every provided key.
         * All localizations with these keys will be found.
         */
        fun byKeys(keys: List<String>): L10nSearchQuery {
            this.keys = keys
            return this
        }

        /**
         * Search by every provided key.
         * All localizations with these keys will be found.
         */
        fun byKeys(vararg keys: String): L10nSearchQuery {
            return byKeys(keys.toList())
        }

        /**
         * Search for localizations with specific locale.
         * Only localizations with this locale will be found.
         */
        fun withLocale(locale: L10nLocale): L10nSearchQuery {
            this.locale = locale
            return this
        }

        /**
         * Search in specific scope.
         * Only localizations in this scope will be found.
         */
        fun inScope(scope: GlobalSearchScope): L10nSearchQuery {
            this.scope = scope
            return this
        }

        fun findFiles(): List<VirtualFile> {
            return mapIndexKeys { FileBasedIndex.getInstance().getContainingFiles(indexName, it, scope) }.distinct()
        }

        fun findValues(): List<String> {
            return mapIndexKeys { FileBasedIndex.getInstance().getValues(indexName, it, scope) }.distinct()
        }

        fun findFirstValue(): String? {
            return findValues().firstOrNull()
        }

        fun findProperties(): List<JsonProperty> {
            val containingFiles = findFiles()
            return runReadAction {
                val psiFiles = containingFiles.mapNotNull { it.toPsiFile(project) as? JsonFile }
                val properties = psiFiles.flatMap { findL10nPropertiesInFile(it) }
                val applicableProperties = properties.filter { it.name in keys }
                applicableProperties
            }
        }

        fun findFirstProperty(): JsonProperty? {
            val containingFiles = findFiles()
            return runReadAction {
                val psiFiles = containingFiles.mapNotNull { it.toPsiFile(project) as? JsonFile }
                val properties = psiFiles.flatMap { findL10nPropertiesInFile(it) }
                val applicableProperty = properties.find { it.name in keys }
                applicableProperty
            }
        }

        fun findObjects(): List<T> {
            return findProperties().mapNotNull { createL10n(it) }
        }

        fun findFirstObject(): T? {
            return findFirstProperty()?.let { createL10n(it) }
        }

        private fun <T> mapIndexKeys(mapper: (L10nIndexKey) -> Collection<T>): List<T> {
            validateHasKey()
            val localeList = locale?.asList() ?: L10nLocale.entries
            val indexKeys = keys.flatMap { key ->
                localeList.map { locale ->
                    L10nIndexKey(key, locale)
                }
            }
            return indexKeys.flatMap { mapper(it) }.distinct()
        }

        private fun validateHasKey() {
            keys.ifEmpty { throw IllegalStateException("No keys are defined for l10n search") }
        }

    }

}