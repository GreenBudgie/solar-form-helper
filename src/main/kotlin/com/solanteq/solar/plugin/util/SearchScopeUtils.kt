package com.solanteq.solar.plugin.util

import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.solanteq.solar.plugin.file.IncludedFormFileType
import com.solanteq.solar.plugin.file.L10nFileType
import com.solanteq.solar.plugin.file.RootFormFileType

/**
 * Returns the search scope restricted to only search in the specified file types.
 * Similar to [GlobalSearchScope.getScopeRestrictedByFileTypes]
 * and [LocalSearchScope.getScopeRestrictedByFileTypes], but in a more convenient way.
 */
inline fun <reified T : SearchScope> T.restrictedByFileTypes(vararg types: FileType) = when(this) {
    is GlobalSearchScope -> GlobalSearchScope.getScopeRestrictedByFileTypes(this, *types) as T
    is LocalSearchScope -> LocalSearchScope.getScopeRestrictedByFileTypes(this, *types) as T
    else -> this
}

/**
 * Returns the search scope restricted to only search in form files, root and included
 */
inline fun <reified T : SearchScope> T.restrictedByFormFiles() = restrictedByFileTypes(
    RootFormFileType,
    IncludedFormFileType
)

/**
 * Returns the search scope restricted to only search in forms and localization files
 */
inline fun <reified T : SearchScope> T.restrictedByFormAndL10nFiles() = restrictedByFileTypes(
    RootFormFileType,
    IncludedFormFileType,
    L10nFileType
)

/**
 * Returns the search scope restricted to only search in localization files
 */
inline fun <reified T : SearchScope> T.restrictedByL10nFiles() = restrictedByFileTypes(L10nFileType)