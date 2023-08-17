package com.solanteq.solar.plugin.element.base

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.FormGroup
import com.solanteq.solar.plugin.l10n.FormL10n
import com.solanteq.solar.plugin.l10n.L10nLocale
import com.solanteq.solar.plugin.l10n.search.FormL10nSearch
import com.solanteq.solar.plugin.l10n.search.L10nSearchBase

/**
 * Represents a json element with `name` property that can be localized
 */
abstract class FormLocalizableElement<T : JsonElement>(
    sourceElement: T,
    objectWithNameProperty: JsonObject
) : FormNamedElement<T>(sourceElement, objectWithNameProperty) {

    /**
     * A list of localization keys for this [FormElement].
     *
     * For example, [FormGroup] in the root form can have the following localization key:
     * "test.form.testForm.details".
     *
     * If [FormElement] is placed in included form, it can have multiple localization keys
     * based on root forms that contain this included form.
     */
    abstract val l10nKeys: List<String>

    fun getL10nValues(locale: L10nLocale? = null): List<String> {
        l10nKeys.ifEmpty { return emptyList() }
        return buildL10nSearcher(locale).findValues()
    }

    fun getL10ns(locale: L10nLocale? = null): List<FormL10n> {
        l10nKeys.ifEmpty { return emptyList() }
        return buildL10nSearcher(locale).findObjects()
    }

    private fun buildL10nSearcher(locale: L10nLocale?): L10nSearchBase<FormL10n>.L10nSearchQuery {
        val l10nSearcher = FormL10nSearch.search(project).byKeys(l10nKeys)
        if(locale != null) {
            l10nSearcher.withLocale(locale)
        }
        return l10nSearcher
    }

}