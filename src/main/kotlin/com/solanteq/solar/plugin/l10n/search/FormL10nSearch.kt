package com.solanteq.solar.plugin.l10n.search

import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.FileBasedIndex
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.index.l10n.FORM_L10N_INDEX_NAME
import com.solanteq.solar.plugin.index.l10n.FORM_L10N_SHORT_INDEX_NAME
import com.solanteq.solar.plugin.l10n.FormL10n
import com.solanteq.solar.plugin.l10n.L10nLocale
import com.solanteq.solar.plugin.l10n.withLocale
import org.jetbrains.kotlin.idea.base.util.projectScope

/**
 * Utility object that helps to find the necessary [FormL10n]s
 */
object FormL10nSearch : L10nSearchBase<FormL10n>(FORM_L10N_INDEX_NAME) {

    override fun createL10n(property: JsonProperty) = FormL10n.fromElement(property)

    fun findFileContainingFormL10n(
        form: FormRootFile,
        locale: L10nLocale? = null,
    ): VirtualFile? {
        val l10nEntries = if (locale != null) {
            form.l10nEntries.withLocale(locale)
        } else {
            form.l10nEntries
        }
        val effectiveEntry = l10nEntries.first().key
        val l10nFiles = FileBasedIndex.getInstance().getContainingFiles(
            FORM_L10N_SHORT_INDEX_NAME,
            effectiveEntry,
            form.project.projectScope()
        )

        return l10nFiles.firstOrNull()
    }

}