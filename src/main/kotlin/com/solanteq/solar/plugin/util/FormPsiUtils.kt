package com.solanteq.solar.plugin.util

import com.intellij.json.psi.*
import com.intellij.psi.PsiElement
import com.intellij.psi.util.findParentOfType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parentOfTypes
import com.solanteq.solar.plugin.element.FormIncludedFile
import com.solanteq.solar.plugin.element.FormJsonInclude
import com.solanteq.solar.plugin.element.toFormElement
import com.solanteq.solar.plugin.file.IncludedFormFileType
import com.solanteq.solar.plugin.file.TopLevelFormFileType
import kotlin.reflect.KClass

/**
 * Helps traverse the psi tree in form files of both types (top level and included).
 * Use this util (when possible and necessary) to traverse the form psi tree.
 *
 * The main goal of this util is to take JSON include into account.
 *
 * For example, if we invoke [PsiElement.parentOfType] in included form instead of the method in this util,
 * it will not consider the probable existence of multiple places in top-level forms where this
 * JSON include declared.
 *
 * Note that methods in this util can return multiple parent psi elements because multiple
 * JSON includes can be declared to reference the same included form.
 *
 * **Most methods use project-scope or even all-scope references search,
 * so they are very performance impactful.**
 */
object FormPsiUtils {

    /**
     * In top-level form:
     * - Finds first parent with [parentClass] type of the given [element] by traversing up the psi tree,
     * similar to [PsiElement.parentOfTypes]. Returns a list with single element,
     * or empty list if no element is found.
     *
     * In included form:
     * - Finds all parents with [parentClass] type of the given [element] by either
     * traversing up the psi tree or using JSON include references to find first parents in other forms.
     * Returns the list of all applicable parent elements.
     *
     * For included forms with json-flat declarations:
     * - Ignores the top-level array element
     */
    fun <T : JsonElement> firstParentsOfType(element: JsonElement, parentClass: KClass<T>): List<T> {
        val containingJsonFile = element.containingFile as? JsonFile ?: return emptyList()
        val isTopLevelForm = containingJsonFile.fileType == TopLevelFormFileType

        val firstParentInThisFile = element.parentOfTypes(parentClass)

        if(isTopLevelForm) {
            return if(firstParentInThisFile != null) {
                listOf(firstParentInThisFile)
            } else {
                emptyList()
            }
        }

        val isSearchingForArrayParent = parentClass == JsonArray::class
        val topLevelValue = containingJsonFile.topLevelValue
        val needToConsiderJsonFlat = topLevelValue is JsonArray
                && isSearchingForArrayParent
                && firstParentInThisFile == topLevelValue

        if(!needToConsiderJsonFlat && firstParentInThisFile != null) {
            return listOf(firstParentInThisFile)
        }

        val jsonIncludeDeclarations = jsonIncludeDeclarations(containingJsonFile)
        val isFirstParentIsTopLevelJsonArray = firstParentInThisFile is JsonArray
                && firstParentInThisFile == topLevelValue

        return jsonIncludeDeclarations.flatMap {
            val isFlat = it.type.isFlat
            if(needToConsiderJsonFlat && isFirstParentIsTopLevelJsonArray && !isFlat) {
                return@flatMap listOf(firstParentInThisFile!!)
            }
            return@flatMap firstParentsOfType(it.sourceElement, parentClass)
        }
    }

    /**
     * In top-level form:
     * - Finds the direct parent of the given [element], similar to [PsiElement.getParent]
     * Returns a list with single parent element, or empty list if [element] is [JsonFile]
     *
     * In included form:
     * - Finds the direct parent of the given [element]. If this [element] is a top-level element
     * in included form file, finds all its declarations and returns a list of corresponding
     * parent elements relative to [FormJsonInclude.sourceElement].
     * Otherwise, similar to [PsiElement.getParent]
     *
     * For included forms with json-flat declarations:
     * - Ignores the top-level array element
     */
    fun parents(element: JsonElement): List<JsonElement> {
        val containingJsonFile = element.containingFile as? JsonFile ?: return emptyList()
        val topLevelValue = containingJsonFile.topLevelValue
        val isTopLevelForm = containingJsonFile.fileType == TopLevelFormFileType

        val parentInThisForm = element.parent as? JsonElement ?: return emptyList()

        val needToConsiderJsonFlat = topLevelValue is JsonArray
                && parentInThisForm == topLevelValue
                && !isTopLevelForm
        if(!needToConsiderJsonFlat && parentInThisForm !is JsonFile) {
            return listOf(parentInThisForm)
        }

        val jsonIncludeDeclarations = jsonIncludeDeclarations(containingJsonFile)

        return jsonIncludeDeclarations.flatMap {
            val isFlat = it.type.isFlat
            if(needToConsiderJsonFlat && !isFlat) {
                return@flatMap listOf(parentInThisForm)
            }
            return@flatMap parents(it.sourceElement)
        }
    }

    /**
     * In top-level form:
     * - Similar to [JsonPsiUtil.isPropertyValue]
     *
     * In included form:
     * - If this is not a top-level json value, similar to [JsonPsiUtil.isPropertyValue].
     * Otherwise, finds all JSON include declarations and checks whether a declaration is
     * a property value. If any declaration is a property value - returns true.
     */
    fun isPropertyValue(element: JsonElement): Boolean {
        if(JsonPsiUtil.isPropertyValue(element)) return true

        val parentsInOtherForms = parents(element).filter { it != element.parent }
        return parentsInOtherForms.any { it is JsonProperty }
    }

    /**
     * In top-level form:
     * - Similar to [JsonPsiUtil.isPropertyValue], but also checks the property name
     * to be in [applicableKeys]
     *
     * In included form:
     * - If this is not a top-level json value, similar to top-level form.
     * Otherwise, finds all JSON include declarations and checks whether a declaration is
     * a property value and checks the property name to be in [applicableKeys].
     * If any declaration is a property value with applicable property key - returns true.
     */
    fun isPropertyValueWithKey(element: JsonElement, vararg applicableKeys: String): Boolean {
        val directParent = element.parent
        if(directParent is JsonProperty
            && element === directParent.value
            && directParent.name in applicableKeys) {
            return true
        }

        val parentsInOtherForms = parents(element).filter { it != element.parent }
        return parentsInOtherForms.any {
            it is JsonProperty && it.name in applicableKeys
        }
    }

    /**
     * In top-level form:
     * - Whether the element is inside a json object that is a value of property with one of specified keys.
     *
     * Example:
     *
     * Consider the following json structure:
     * ```
     * "request": {
     *      "name": "lty.service.findById", //true
     *      "group": "lty", //true
     *      "params": [ //true
     *          { //true
     *              "name": "id", //false
     *              "value": "id" //false
     *          }
     *      ]
     * }
     * ```
     * `isElementInsideObject("request")` will return `true` for every json element inside `"request"` object,
     * excluding `"name": "id"` and `"value": "id"` in params because they are inside their own unnamed object.
     *
     * In included form:
     * - Finds all json object parents in other forms by JSON include declarations
     * and checks whether one of them is a value of property with one of specified keys.
     */
    fun isInObjectWithKey(element: JsonElement, vararg applicableKeys: String): Boolean {
        val firstJsonObjectParents = firstParentsOfType(element, JsonObject::class)

        return firstJsonObjectParents.any {
            isPropertyValueWithKey(it, *applicableKeys)
        }
    }

    /**
     * In top-level form:
     * - Whether the element is inside a json array that is a value of property with one of specified keys.
     *
     * Example for `isObjectInArrayWithKey("fields")`:
     * ```
     * "fields": [ //false
     *   { //true
     *     "name": "fieldName" //true
     *   }, //true
     *   "jsonInclude" //true
     * ],
     * "boilerplateKey": "boilerplateValue" //false
     * ```
     *
     * In included form:
     * - Finds all json array parents in other forms by JSON include declarations
     * and checks whether one of them is a value of property with one of specified keys.
     */
    fun isInArrayWithKey(element: JsonElement, vararg applicableKeys: String): Boolean {
        val firstJsonArrayParents = firstParentsOfType(element, JsonArray::class)

        return firstJsonArrayParents.any {
            isPropertyValueWithKey(it, *applicableKeys)
        }
    }

    /**
     * In top-level form:
     * Whether the element is located directly at top-level json object.
     *
     * Example:
     * ```
     * { //false (top-level object itself is not considered to be located at top-level json object ._.)
     *   "name": "hi", //true
     *   "module": "test", //true
     *   "someArray": [ //true for property key
     *     { //false
     *       "someKey": "someValue" //false
     *     }
     *   ],
     *   "someObject": { //true for property key
     *
     *   }
     * }
     * ```
     *
     * In included form:
     * - Always false
     */
    fun isAtTopLevelObject(element: JsonElement): Boolean {
        val topLevelObject = element.findParentOfType<JsonObject>() ?: return false
        val jsonFile = topLevelObject.containingFile as? JsonFile ?: return false
        return jsonFile.fileType != IncludedFormFileType && jsonFile.topLevelValue === topLevelObject
    }

    /**
     * If the property value is a JSON include declaration, finds the referenced form and returns
     * its top-level value (can only be [JsonObject] or [JsonArray]).
     * Note that if the property value can be considered as JSON include declaration, but the
     * referenced form cannot be found, then **null will be returned**.
     *
     * Otherwise, works the same as [JsonProperty.getValue].
     */
    fun getPropertyValue(propertyElement: JsonProperty): JsonValue? {
        val value = propertyElement.value
        if(value !is JsonStringLiteral) {
            return value
        }
        val jsonIncludeDeclaration = value.toFormElement<FormJsonInclude>() ?: return value
        val referencedForm = jsonIncludeDeclaration.referencedFormPsiFile ?: return value
        return referencedForm.topLevelValue
    }

    private fun jsonIncludeDeclarations(jsonFile: JsonFile): List<FormJsonInclude> {
        val includedForm = jsonFile.toFormElement<FormIncludedFile>() ?: return emptyList()
        return includedForm.declarations
    }


}