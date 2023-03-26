package com.solanteq.solar.plugin.element.base

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.FormGroup
import com.solanteq.solar.plugin.l10n.FormL10n
import com.solanteq.solar.plugin.l10n.search.FormL10nSearch
import org.jetbrains.kotlin.idea.base.util.allScope

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

    /**
     * All localization values that correspond to every key in [l10nKeys]
     */
    val l10nValues: List<String> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val project = project
        l10nKeys.flatMap {
            FormL10nSearch.findL10nValuesByKey(it, project.allScope())
        }
    }

    /**
     * All [FormL10n]s that correspond to every key in [l10nKeys]
     */
    val l10ns: List<FormL10n> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val project = project
        l10nKeys.flatMap {
            FormL10nSearch.findL10nsByKey(it, project)
        }
    }

}