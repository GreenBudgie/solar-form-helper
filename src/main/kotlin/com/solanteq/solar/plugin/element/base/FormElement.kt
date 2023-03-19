package com.solanteq.solar.plugin.element.base

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.element.base.FormElement.FormElementCreator
import com.solanteq.solar.plugin.element.toFormElement
import com.solanteq.solar.plugin.util.FormPsiUtils

/**
 * Form element is a representation of a SOLAR form json element.
 * Form element must only be created via [toFormElement] call.
 *
 * The main goal of form elements is to provide a pseudo PSI tree for SOLAR forms without
 * actually creating one.
 *
 * It does not inherit [JsonElement], but does contain [sourceElement]
 * as a link to original field it have been created from.
 *
 * [sourceElement] has a particular json element type for which [toFormElement] must be called.
 *
 * **Important note**: *form elements are mostly utility classes and data storage, not PSI elements themselves!*
 *
 * Most of its properties are lazy-initialized from source element.
 * As it does not inherit [JsonElement], [sourceElement] can become invalid at any time after creation
 * which can break how form element works. Try to only use form elements in-place.
 * If you need to update any information about the element, just reuse [toFormElement] method on json element.
 *
 * Every form element must have [FormElementCreator] companion object.
 */
abstract class FormElement<T : JsonElement> protected constructor(
    val sourceElement: T
) {

    /**
     * Project lazy value to only call it once for performance
     */
    protected val project by lazy(LazyThreadSafetyMode.PUBLICATION) {
        sourceElement.project
    }

    /**
     * Containing file (original) lazy value to only call it once for performance
     */
    protected val containingFile by lazy(LazyThreadSafetyMode.PUBLICATION) {
        sourceElement.containingFile?.originalFile as? JsonFile
    }

    override fun equals(other: Any?): Boolean {
        if(other is FormElement<*>) return other.sourceElement == sourceElement
        return false
    }

    override fun hashCode(): Int {
        return sourceElement.hashCode()
    }

    companion object {

        @JvmStatic
        protected fun canBeCreatedAsArrayElement(
            sourceElement: JsonElement,
            requiredArrayName: String
        ): Boolean {
            val jsonObject = sourceElement as? JsonObject ?: return false

            val parentArrays = FormPsiUtils.parents(jsonObject)

            val containsParentPropertyWithArrayName = parentArrays.any {
                FormPsiUtils.isPropertyValueWithKey(it, requiredArrayName)
            }
            return containsParentPropertyWithArrayName
        }

    }

    /**
     * An interface that companion object of this element must implement.
     * It is used to create element from the factory using [toFormElement].
     */
    interface FormElementCreator<T : FormElement<*>> {

        fun create(sourceElement: JsonElement): T?

    }

}