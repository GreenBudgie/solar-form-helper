package com.solanteq.solar.plugin.element.base

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.FormGroup
import com.solanteq.solar.plugin.l10n.FormL10n
import com.solanteq.solar.plugin.l10n.FormL10nEntry
import com.solanteq.solar.plugin.l10n.L10nLocale
import com.solanteq.solar.plugin.l10n.search.FormL10nSearch
import com.solanteq.solar.plugin.l10n.search.L10nSearchBase
import org.jetbrains.kotlin.idea.base.util.projectScope

/**
 * Represents a json element with `name` property that can be localized
 */
abstract class FormLocalizableElement<T : JsonElement>(
    sourceElement: T,
    objectWithNameProperty: JsonObject,
) : FormNamedElement<T>(sourceElement, objectWithNameProperty) {

    /**
     * A list of fully unique [l10nEntries].
     */
    abstract val l10nEntries: List<FormL10nEntry>

    /**
     * A list of localization keys for this [AbstractFormElement].
     *
     * For example, [FormGroup] in the root form can have the following localization key:
     * `test.form.testForm.detail`.
     *
     * If [AbstractFormElement] is placed in included form, it can have multiple localization keys
     * based on root forms that contain this included form.
     */
    val l10nKeys by lazy(LazyThreadSafetyMode.PUBLICATION) {
        return@lazy l10nEntries.map { it.key }.distinct()
    }

    fun getL10nValues(locale: L10nLocale? = null, projectOnly: Boolean = false): List<String> {
        l10nKeys.ifEmpty { return emptyList() }
        return buildL10nQuery(locale, projectOnly).findValues()
    }

    fun getL10nValue(locale: L10nLocale? = null, projectOnly: Boolean = false): String? {
        return getL10nValues(locale, projectOnly).firstOrNull()
    }

    fun getL10ns(locale: L10nLocale? = null, projectOnly: Boolean = false): List<FormL10n> {
        l10nKeys.ifEmpty { return emptyList() }
        return buildL10nQuery(locale, projectOnly).findObjects()
    }

    private fun buildL10nQuery(locale: L10nLocale?, projectOnly: Boolean): L10nSearchBase<FormL10n>.L10nSearchQuery {
        val l10nSearcher = FormL10nSearch.search(project).byKeys(l10nKeys)
        if (locale != null) {
            l10nSearcher.withLocale(locale)
        }
        if (projectOnly) {
            l10nSearcher.inScope(project.projectScope())
        }
        return l10nSearcher
    }

}