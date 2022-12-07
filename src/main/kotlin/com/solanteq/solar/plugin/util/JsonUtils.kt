package com.solanteq.solar.plugin.util

import com.intellij.json.psi.*
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.findParentOfType

fun JsonElement.isPropertyValueWithKey(vararg applicableKeys: String): Boolean {
    if(!JsonPsiUtil.isPropertyValue(this)) return false
    val parentJsonProperty = this.parent as? JsonProperty ?: return false
    return parentJsonProperty.name in applicableKeys
}

fun JsonElement.isInObjectWithKey(vararg applicableKeys: String): Boolean {
    val firstJsonObjectParent = findParentOfType<JsonObject>() ?: return false
    val jsonObjectProperty = firstJsonObjectParent.parent as? JsonProperty ?: return false
    return jsonObjectProperty.name in applicableKeys
}

fun JsonElement.isInArrayWithKey(vararg applicableKeys: String): Boolean {
    val firstJsonArrayParent = findParentOfType<JsonArray>() ?: return false
    val jsonArrayProperty = firstJsonArrayParent.parent as? JsonProperty ?: return false
    return jsonArrayProperty.name in applicableKeys
}

fun JsonElement.isAtTopLevelObject(): Boolean {
    val topLevelObject = findParentOfType<JsonObject>() ?: return false
    val jsonFile = topLevelObject.containingFile as? JsonFile ?: return false
    return jsonFile.topLevelValue == topLevelObject
}

/**
 * If the value of json property is string literal, returns its text, null otherwise
 */
fun JsonProperty?.valueAsString(): String? {
    val stringLiteral = this?.value as? JsonStringLiteral ?: return null
    return stringLiteral.value
}

/**
 * Relative text range of this string literal with trimmed quotes
 */
val JsonStringLiteral.textRangeWithoutQuotes: TextRange
    get() {
        val value = value
        if(value.length < 2) return TextRange.EMPTY_RANGE
        return TextRange.from(1, value.length)
    }