package com.solanteq.solar.plugin.element.base

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * Form element is a representation of a SOLAR form json element.
 * Form elements should only be created via its companion object
 * [com.solanteq.solar.plugin.element.creator.FormElementCreator.createFrom] call.
 *
 * The main goal of form elements is to provide a pseudo PSI tree for SOLAR forms without
 * actually creating one.
 *
 * It does not inherit [JsonElement], but does contain [sourceElement]
 * as a link to original field it have been created from.
 *
 * **Important note**: *form elements are mostly utility classes and data storage, not PSI elements themselves!*
 *
 * Most of its properties are lazy-initialized from source element.
 * As it does not inherit [JsonElement], [sourceElement] can become invalid at any time after creation
 * which can break how form element works. Try to only use form elements in-place.
 * If you need to update any information about the element, just reuse
 * [com.solanteq.solar.plugin.element.creator.FormElementCreator.createFrom] method on json element.
 *
 * Every [FormElement] must have a private constructor and a companion object that inherits
 * [com.solanteq.solar.plugin.element.creator.FormElementCreator].
 */
interface FormElement<T : JsonElement> {

    /**
     * JSON source element that this form element has been created from
     */
    val sourceElement: T

    /**
     * Project lazy value to only call it once for performance
     */
    val project: Project

    /**
     * Containing file (original) lazy value to only call it once for performance
     */
    val containingFile: JsonFile?

    /**
     * Virtual file lazy value to only call it once for performance
     */
    val virtualFile: VirtualFile?

    /**
     * Form file (root or included) that contains this element, or null if the file or element is invalid
     */
    val containingForm: FormFile?

    /**
     * Direct parents of this [FormElement].
     * Every element can have multiple parents since it can be inside an included form.
     *
     * Might be very slow.
     *
     * **Important**:
     *
     * Keep in mind that elements might not know about some of their parents in some contexts. For example,
     * requests can be at the top-level of a root form, but also they can be inside inline configurations
     * and many other places. Look at concrete implementations of this method before relying on it to determine
     * whether the parent you are looking for is even supported.
     */
    val parents: List<FormElement<*>>

    /**
     * Direct children of this [FormElement].
     *
     * Might be very slow.
     */
    val children: List<FormElement<*>>
}