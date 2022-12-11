package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.PsiType
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.TypeConversionUtil
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.file.TopLevelFormFileType
import com.solanteq.solar.plugin.reference.form.FormReference
import com.solanteq.solar.plugin.util.valueAsString
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.uast.UClass
import org.jetbrains.uast.toUElementOfType

/**
 * Represents a top level form file.
 *
 * It is possible to separate top level forms by type, for example:
 * - Single (program.json)
 * - List (programList.json)
 * - Many (programs.json)
 * - Lookup (programLookup.json)
 * - Inline list (programInlineList.json)
 * - etc.
 *
 * However, there is a catch: you can always create a form with a completely different purpose.
 * Also, it is wrong to just suppose that a form named `programList.json` to actually be `List` type.
 * There are no hard-coded form naming rules.
 * So, **for now**, we don't separate top level forms by type.
 */
class FormTopLevelFile(
    sourceElement: JsonFile,
    private val topLevelObject: JsonObject
) : FormLocalizableElement<JsonFile>(sourceElement, topLevelObject) {

    /**
     * An actual json property element that represents module of this form
     */
    val moduleProperty by lazy { topLevelObject.findProperty("module") }

    /**
     * Module of this form object.
     *
     * It might return null if [sourceElement] does not have a `module` property or
     * this property has non-string value.
     */
    val module by lazy { moduleProperty.valueAsString() }

    /**
     * Fully-qualified SOLAR form name
     */
    val fullName by lazy {
        val name = name ?: return@lazy null
        val module = module ?: return@lazy name
        return@lazy "$module.$name"
    }

    /**
     * An array of all group rows in this form.
     * - An empty array if `groupRows` property is declared in this form, but the array is empty
     * - `null` if `groupRows` property is not declared
     */
    val groupRows by lazy {
        topLevelObject.findProperty(FormGroupRow.ARRAY_NAME).toFormArrayElement<FormGroupRow>()
    }

    /**
     * An array of all groups in this form.
     * - An empty array if `groups` property is declared in this form, but the array is empty
     * - `null` if `groups` property is not declared
     */
    val groups by lazy {
        topLevelObject.findProperty(FormGroup.ARRAY_NAME).toFormArrayElement<FormGroup>()
    }

    /**
     * All groups that are contained in this form.
     * - If groups are represented as `groupRows` property, it will retrieve all inner groups and combine them into this list.
     * - If groups are represented as `groups` property, it will just use them.
     *
     * Never returns null, only empty list.
     */
    val allGroups by lazy {
        val groupRows = groupRows ?: return@lazy groups?.contents ?: return@lazy emptyList()
        val notNullGroups = groupRows.mapNotNull { it.groups }
        return@lazy notNullGroups.flatMap { it.contents }
    }

    /**
     * List of all requests in this form. Possible requests are:
     * - source
     * - save
     * - remove
     * - createSource
     */
    val requests by lazy {
        listOfNotNull(
            sourceRequest,
            saveRequest,
            removeRequest,
            createSourceRequest
        )
    }

    val sourceRequest by lazy { getRequestByType(FormRequest.RequestType.SOURCE) }
    val saveRequest by lazy { getRequestByType(FormRequest.RequestType.SAVE) }
    val removeRequest by lazy { getRequestByType(FormRequest.RequestType.REMOVE) }
    val createSourceRequest by lazy { getRequestByType(FormRequest.RequestType.CREATE_SOURCE) }

    /**
     * Data class from source request that this field uses
     */
    val dataClassFromSourceRequest by lazy {
        val sourceRequest = sourceRequest ?: return@lazy null
        val method = sourceRequest.methodFromRequest ?: return@lazy null
        val derivedClass = sourceRequest.serviceFromRequest?.javaPsi ?: return@lazy null
        val superClass = method.containingClass ?: return@lazy null
        val rawReturnType = method.returnType ?: return@lazy null
        return@lazy substitutePsiType(
            superClass,
            derivedClass,
            rawReturnType
        )
    }

    /**
     * All requests from inline elements that reference this form
     */
    val inlineRequests: List<FormRequest> by lazy {
        val containingFile = containingFile ?: return@lazy emptyList()
        val references = ReferencesSearch.search(containingFile, project.allScope()).findAll()
        val formPropertyValueElements = references.filterIsInstance<FormReference>().map { it.element }
        val formInlineElements = formPropertyValueElements.mapNotNull {
            val formProperty = it.parent as? JsonProperty ?: return@mapNotNull null
            val inlineValueObject = formProperty.parent as? JsonObject ?: return@mapNotNull null
            val inlineProperty = inlineValueObject.parent as? JsonProperty ?: return@mapNotNull null
            return@mapNotNull inlineProperty.toFormElement<FormInline>()
        }
        return@lazy formInlineElements.mapNotNull { it.request }
    }

    /**
     * All data classes from inline elements that reference this form
     */
    val dataClassesFromInlineRequests by lazy {
        inlineRequests.mapNotNull {
            val method = it.methodFromRequest ?: return@mapNotNull null
            val derivedClass = it.serviceFromRequest?.javaPsi ?: return@mapNotNull null
            val superClass = method.containingClass ?: return@mapNotNull null
            val rawReturnListType = method.returnType as? PsiClassType ?: return@mapNotNull null
            val rawReturnType = rawReturnListType.parameters.firstOrNull() ?: return@mapNotNull null
            return@mapNotNull substitutePsiType(
                superClass,
                derivedClass,
                rawReturnType
            )
        }
    }

    private fun substitutePsiType(superClass: PsiClass, derivedClass: PsiClass, psiType: PsiType): UClass? {
        val substitutedReturnType = TypeConversionUtil.getClassSubstitutor(
            superClass,
            derivedClass,
            PsiSubstitutor.EMPTY
        )?.substitute(psiType)
        val classReturnType = substitutedReturnType as? PsiClassType ?: return null
        return classReturnType.resolve().toUElementOfType()
    }

    /**
     * Gets the request by its type, or null if such request isn't present
     */
    private fun getRequestByType(type: FormRequest.RequestType): FormRequest? =
        topLevelObject.findProperty(type.requestLiteral).toFormElement()

    companion object : FormElementCreator<FormTopLevelFile> {

        override fun create(sourceElement: JsonElement): FormTopLevelFile? {
            val jsonFile = sourceElement as? JsonFile ?: return null
            val topLevelObject = jsonFile.topLevelValue as? JsonObject ?: return null
            if(jsonFile.fileType == TopLevelFormFileType) {
                return FormTopLevelFile(jsonFile, topLevelObject)
            }
            return null
        }

    }

}