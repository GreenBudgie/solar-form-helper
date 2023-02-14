package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Key
import com.intellij.psi.*
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.TypeConversionUtil
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.file.RootFormFileType
import com.solanteq.solar.plugin.l10n.FormL10n
import com.solanteq.solar.plugin.l10n.search.L10nSearch
import com.solanteq.solar.plugin.reference.form.FormNameReference
import com.solanteq.solar.plugin.util.FormPsiUtils
import com.solanteq.solar.plugin.util.restrictedByFormFiles
import com.solanteq.solar.plugin.util.valueAsString
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.kotlin.idea.base.util.projectScope
import org.jetbrains.uast.UClass
import org.jetbrains.uast.toUElementOfType

/**
 * Represents a root (top-level) form file.
 *
 * It is possible to separate root forms by type, for example:
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
 * So, **for now**, we don't separate root forms by type.
 */
class FormRootFile(
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

    override val localizations: List<String> by lazy {
        val formL10ns = L10nSearch.findFormL10ns(project, project.projectScope())
        return@lazy formL10ns
            .filter { it.type == FormL10n.L10nType.FORM }
            .filter { this == it.referencedFormFileElement }
            .map { it.value }
    }

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
        val groupRows = groupRows
        if(groupRows != null) {
            return@lazy groupRows.flatMap { it.groups ?: emptyList() }
        }
        val groups = groups
        if(groups != null) {
            return@lazy groups
        }
        return@lazy emptyList()
    }

    /**
     * All fields from [allGroups] in this form
     */
    val allFields by lazy {
        allGroups.flatMap { it.allFields }
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
        val derivedClass = sourceRequest.serviceFromRequest ?: return@lazy null
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
        val formPropertyValueElements = formReferences.filterIsInstance<FormNameReference>().map { it.element }
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
            getDataClassFromInlineRequest(it)
        }
    }

    /**
     * A list of data classes that can be used as data sources for this form.
     *
     * Data classes are collected from various requests in the specified order:
     * 1. Source request in this form
     * 2. Inline requests from other forms
     * 3. List field in other forms
     */
    val allDataClassesFromRequests: List<UClass> by lazy {
        dataClassFromSourceRequest?.let { return@lazy listOf(it) }

        val inlineDataClasses = dataClassesFromInlineRequests
        if(inlineDataClasses.isNotEmpty()) {
            return@lazy inlineDataClasses
        }

        val dataClassesFromListFields = dataClassesFromListFields
        if(dataClassesFromListFields.isNotEmpty()) {
            return@lazy dataClassesFromListFields
        }

        return@lazy emptyList()
    }


    /**
     * All fields from other forms with `LIST` type that relate to this form
     */
    val relatedListFields: List<FormField> by lazy {
        val formPropertyValueElements = formReferences.filterIsInstance<FormNameReference>().map { it.element }
        val fieldElements = formPropertyValueElements
            .flatMap {
                FormPsiUtils.firstParentsOfType(it, JsonObject::class)
            }.mapNotNull {
                it.toFormElement<FormField>()
            }
        val fieldListElements = fieldElements.filter {
            it.type == "LIST"
        }
        return@lazy fieldListElements
    }

    /**
     * All applicable data classes from list fields
     */
    val dataClassesFromListFields: List<UClass> by lazy {
        relatedListFields.mapNotNull {
            val referencedField = it.propertyChain.lastOrNull()?.referencedField ?: return@mapNotNull null
            val fieldContainingClass = referencedField.containingClass ?: return@mapNotNull null
            val rawListType = referencedField.type as? PsiClassType ?: return@mapNotNull null
            val rawInnerType = rawListType.parameters.firstOrNull() ?: return@mapNotNull null
            return@mapNotNull substitutePsiType(
                fieldContainingClass,
                fieldContainingClass,
                rawInnerType
            )
        }
    }

    private val formReferences by lazy {
        val containingFile = containingFile ?: return@lazy emptyList()
        val searchScope = project.allScope().restrictedByFormFiles()
        return@lazy ProgressManager.getInstance().runProcess<Collection<PsiReference>>({
            ReferencesSearch.search(containingFile, searchScope).findAll()
        }, EmptyProgressIndicator())
    }

    private fun getDataClassFromInlineRequest(request: FormRequest): UClass? {
        val method = request.methodFromRequest ?: return null
        val derivedClass = request.serviceFromRequest ?: return null
        val superClass = method.containingClass ?: return null
        val rawReturnListType = method.returnType as? PsiClassType ?: return null
        val rawReturnType = rawReturnListType.parameters.firstOrNull() ?: return null
        return substitutePsiType(
            superClass,
            derivedClass,
            rawReturnType
        )
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

    companion object : FormElementCreator<FormRootFile> {

        override val key = Key<CachedValue<FormRootFile>>("solar.element.rootFile")

        override fun create(sourceElement: JsonElement): FormRootFile? {
            val jsonFile = sourceElement as? JsonFile ?: return null
            val topLevelObject = jsonFile.topLevelValue as? JsonObject ?: return null
            if(jsonFile.fileType == RootFormFileType) {
                return FormRootFile(jsonFile, topLevelObject)
            }
            return null
        }

    }

}