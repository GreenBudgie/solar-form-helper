package com.solanteq.solar.plugin.l10n.generator

import com.intellij.json.JsonElementTypes
import com.intellij.json.psi.*
import com.intellij.openapi.application.runUndoTransparentWriteAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager

object L10nGenerator {

    fun generateL10n(
        key: String,
        value: String,
        placement: L10nPlacement,
    ) {
        val project = placement.file.project
        val generator = JsonElementGenerator(project)
        val generatedL10nProperty = generator.createProperty(key, "\"$value\"")

        runUndoTransparentWriteAction {
            writeL10n(placement.file, generatedL10nProperty, placement, project, generator)
        }
    }

    private fun writeL10n(
        file: JsonFile,
        newProperty: JsonProperty,
        placement: L10nPlacement,
        project: Project,
        generator: JsonElementGenerator,
    ) {
        val topLevelObject = getOrCreateTopLevelObject(file, generator)

        val addedProperty = if (placement.anchorProperty == null) {
            JsonPsiUtil.addProperty(
                topLevelObject,
                newProperty,
                placement.position == PlacementPosition.BEFORE
            )
        } else {
            addPropertyWithAnchor(
                placement,
                topLevelObject,
                newProperty,
                placement.anchorProperty,
                generator
            )
        }

        val range = addedProperty.textRange
        CodeStyleManager.getInstance(project).reformatText(file, range.startOffset, range.endOffset)
    }

    private fun getOrCreateTopLevelObject(
        file: JsonFile,
        generator: JsonElementGenerator,
    ): JsonObject {
        val topLevelObject = file.topLevelValue as? JsonObject
        if (topLevelObject != null) {
            return topLevelObject
        }

        val generatedObject = generator.createObject("")
        return file.add(generatedObject) as JsonObject
    }

    private fun addPropertyWithAnchor(
        placement: L10nPlacement,
        topLevelObject: JsonObject,
        newProperty: JsonProperty,
        anchorProperty: JsonProperty,
        generator: JsonElementGenerator,
    ): PsiElement {
        val addedProperty = writeProperty(newProperty, anchorProperty, topLevelObject, placement, generator)

        val rCurly = addedProperty.nextSibling
        if (!JsonPsiUtil.hasElementType(rCurly, JsonElementTypes.R_CURLY)) {
            topLevelObject.addAfter(generator.createComma(), addedProperty)
        }

        return addedProperty
    }

    private fun writeProperty(
        newProperty: JsonProperty,
        anchorProperty: JsonProperty,
        topLevelObject: JsonObject,
        placement: L10nPlacement,
        generator: JsonElementGenerator,
    ): PsiElement {
        if (placement.position == PlacementPosition.BEFORE) {
            return topLevelObject.addBefore(newProperty, anchorProperty)
        }

        val comma = anchorProperty.nextSibling
        val effectiveComma = if (!JsonPsiUtil.hasElementType(comma, JsonElementTypes.COMMA)) {
            topLevelObject.addAfter(generator.createComma(), anchorProperty)
        } else comma

        return topLevelObject.addAfter(newProperty, effectiveComma)
    }

}