package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.element.base.FormNamedElement
import com.solanteq.solar.plugin.element.creator.FormElementCreator
import com.solanteq.solar.plugin.search.FormSearch
import com.solanteq.solar.plugin.util.valueAsStringOrNull
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.kotlin.idea.core.util.toPsiFile

/**
 * Represents an inline configuration property in [FormGroup]
 */
class FormInline(
    sourceElement: JsonProperty,
    private val valueObject: JsonObject
) : FormNamedElement<JsonProperty>(sourceElement, valueObject) {

    val request by lazy(LazyThreadSafetyMode.PUBLICATION) {
       FormRequest.createFrom(valueObject.findProperty("request"))
    }

    val formFile by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val formProperty = valueObject.findProperty("form") ?: return@lazy null
        val stringValue = formProperty.valueAsStringOrNull() ?: return@lazy null
        return@lazy FormSearch.findRootFormBySolarName(stringValue, project.allScope())?.toPsiFile(project) as? JsonFile
    }

    val formElement by lazy(LazyThreadSafetyMode.PUBLICATION) {
       FormRootFile.createFrom(formFile)
    }

    companion object : FormElementCreator<FormInline, JsonProperty>() {

        override fun doCreate(sourceElement: JsonProperty): FormInline? {
            if(sourceElement.name == "inline") {
                val valueObject = sourceElement.value as? JsonObject ?: return null
                return FormInline(sourceElement, valueObject)
            }
            return null
        }

    }

}