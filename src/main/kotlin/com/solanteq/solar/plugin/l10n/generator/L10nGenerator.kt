package com.solanteq.solar.plugin.l10n.generator

import com.intellij.json.JsonElementTypes
import com.intellij.json.psi.*
import com.intellij.openapi.application.runUndoTransparentWriteAction

object L10nGenerator {

    fun generateL10n(
        key: String,
        value: String,
        file: JsonFile,
        afterProperty: JsonProperty? = null,
    ) {
        val project = file.project
        val generator = JsonElementGenerator(project)
        val generatedL10nProperty = generator.createProperty(key, value)

        val topLevelObject = file.topLevelValue as? JsonObject

        runUndoTransparentWriteAction {
            val effectiveTopLevelObject = if (topLevelObject == null) {
                val generatedObject = generator.createObject("{\n}")
                file.add(generatedObject) as JsonObject
            } else {
                topLevelObject
            }
            val insertAfter = if (afterProperty != null) {
                val comma = afterProperty.nextSibling
                if (!JsonPsiUtil.hasElementType(comma, JsonElementTypes.COMMA)) {
                    effectiveTopLevelObject.addAfter(afterProperty, generator.createComma())
                } else {
                    comma
                }
            } else {
                val lastProperty = effectiveTopLevelObject.propertyList.lastOrNull()
                if (lastProperty != null) {
                    effectiveTopLevelObject.addAfter(generator.createComma(), lastProperty)
                } else {
                    effectiveTopLevelObject.lastChild.prevSibling
                }
            }
            effectiveTopLevelObject.addAfter(generatedL10nProperty, insertAfter)
        }
    }

}