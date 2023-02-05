package com.solanteq.solar.plugin.l10n.search

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.solanteq.solar.plugin.file.L10nFileType
import com.solanteq.solar.plugin.l10n.FormL10n
import com.solanteq.solar.plugin.l10n.L10n
import org.jetbrains.kotlin.idea.core.util.toPsiFile

object L10nSearch {

    fun findL10nVirtualFiles(scope: GlobalSearchScope) = runReadAction {
        FileTypeIndex.getFiles(L10nFileType, scope)
    }

    fun findL10nPsiFiles(project: Project, scope: GlobalSearchScope): List<JsonFile> {
        val files = findL10nVirtualFiles(scope)
        return runReadAction {
            files.mapNotNull {
                it.toPsiFile(project) as? JsonFile
            }
        }
    }

    fun findL10nProperties(project: Project, scope: GlobalSearchScope) = runReadAction {
        val files = findL10nPsiFiles(project, scope)
        return@runReadAction files.flatMap { findL10nPropertiesInFile(it) }
    }

    fun findL10nPropertiesInFile(file: JsonFile) = runReadAction {
        if(file.fileType != L10nFileType) return@runReadAction emptyList()
        val topLevelObject = file.topLevelValue as? JsonObject ?: return@runReadAction emptyList()
        return@runReadAction topLevelObject.propertyList
    }

    /**
     * Finds all [L10n]s in the specified scope.
     *
     * **Only returns plain l10ns**, for form l10ns use [findFormL10nsInFile]
     */
    fun findL10ns(project: Project, scope: GlobalSearchScope): List<L10n> {
        val files = findL10nPsiFiles(project, scope)
        return runReadAction {
            files.flatMap {
                findL10nsInFile(it)
            }
        }
    }

    /**
     * Finds all [FormL10n]s in the specified scope.
     */
    fun findFormL10ns(project: Project, scope: GlobalSearchScope): List<FormL10n> {
        val files = findL10nPsiFiles(project, scope)
        return runReadAction {
            files.flatMap {
                findFormL10nsInFile(it)
            }
        }
    }

    /**
     * Finds all [L10n]s in the specified file.
     *
     * **Only returns plain l10ns**, for form l10ns use [findFormL10nsInFile]
     */
    fun findL10nsInFile(file: JsonFile): List<L10n> {
        val properties = findL10nPropertiesInFile(file)
        return runReadAction {
            properties.mapNotNull {
                if (FormL10n.isFormL10n(it)) return@mapNotNull null
                return@mapNotNull L10n.fromElement(it)
            }
        }
    }

    /**
     * Finds all [FormL10n]s in the specified file.
     */
    fun findFormL10nsInFile(file: JsonFile): List<FormL10n> {
        val properties = findL10nPropertiesInFile(file)
        return runReadAction {
            properties.mapNotNull {
                return@mapNotNull FormL10n.fromElement(it)
            }
        }
    }

}