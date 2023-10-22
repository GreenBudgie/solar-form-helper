package com.solanteq.solar.plugin.element.base

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.solanteq.solar.plugin.element.FormIncludedFile
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.util.asList

abstract class AbstractFormElement<T : JsonElement> protected constructor(
    override val sourceElement: T
) : FormElement<T> {

    override val project by lazy(LazyThreadSafetyMode.PUBLICATION) {
        sourceElement.project
    }

    override val containingFile by lazy(LazyThreadSafetyMode.PUBLICATION) {
        sourceElement.containingFile?.originalFile as? JsonFile
    }

    override val virtualFile by lazy(LazyThreadSafetyMode.PUBLICATION) {
        containingFile?.virtualFile
    }

    override val containingForm by lazy(LazyThreadSafetyMode.PUBLICATION) {
        FormFile.createFrom(containingFile)
    }

    /**
     * All root forms that contain this element.
     * - If the element is in the root form, only one form will be returned.
     * - If the element is in the included form, finds all root forms that contain this included form.
     */
    val containingRootForms by lazy(LazyThreadSafetyMode.PUBLICATION) {
        FormRootFile.createFrom(containingFile)?.let {
            return@lazy it.asList()
        }
        FormIncludedFile.createFrom(containingFile)?.let {
            return@lazy it.allRootForms
        }
        return@lazy emptyList()
    }

    override fun equals(other: Any?): Boolean {
        if(other is AbstractFormElement<*>) {
            return this === other || other.sourceElement == sourceElement
        }
        return false
    }

    override fun hashCode() = sourceElement.hashCode()

}