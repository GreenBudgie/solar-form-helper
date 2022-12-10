package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.element.base.FormPropertyArrayElement

/**
 * Represents a form file (not included)
 */
interface FormFile : FormLocalizableElement<JsonFile> {

    /**
     * An actual json property element that represents module of this form
     */
    val moduleProperty: JsonProperty?

    /**
     * Module of this form object.
     *
     * It might return null if [sourceElement] does not have a `module` property or
     * this property has non-string value.
     */
    val module: String?

    /**
     * Fully-qualified SOLAR form name
     */
    val fullName: String?

    /**
     * An array of all group rows in this form.
     * - An empty array if `groupRows` property is declared in this form, but the array is empty
     * - `null` if `groupRows` property is not declared
     */
    val groupRows: FormPropertyArrayElement<FormGroupRow>?

    /**
     * An array of all groups in this form.
     * - An empty array if `groups` property is declared in this form, but the array is empty
     * - `null` if `groups` property is not declared
     */
    val groups: FormPropertyArrayElement<FormGroup>?

    /**
     * All groups that are contained in this form.
     * - If groups are represented as `groupRows` property, it will retrieve all inner groups and combine them into this list.
     * - If groups are represented as `groups` property, it will just use them.
     *
     * Never returns null, only empty list.
     */
    val allGroups: List<FormGroup>

    /**
     * List of all requests in this form. Possible requests are:
     * - source
     * - save
     * - remove
     * - createSource
     */
    val requests: List<FormRequest>

    val sourceRequest: FormRequest?
    val saveRequest: FormRequest?
    val removeRequest: FormRequest?
    val createSourceRequest: FormRequest?

}