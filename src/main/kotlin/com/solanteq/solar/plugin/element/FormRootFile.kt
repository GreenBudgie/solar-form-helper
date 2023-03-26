package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.*
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.TypeConversionUtil
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.file.RootFormFileType
import com.solanteq.solar.plugin.reference.form.FormNameReference
import com.solanteq.solar.plugin.util.FormPsiUtils
import com.solanteq.solar.plugin.util.asList
import com.solanteq.solar.plugin.util.restrictedByFormFiles
import com.solanteq.solar.plugin.util.valueAsStringOrNull
import org.jetbrains.kotlin.idea.base.util.allScope
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
     * The short name of this form.
     *
     * If the "name" property is not present in the form, it will return the name
     * of the form file without extension
     */
    override val name by lazy(LazyThreadSafetyMode.PUBLICATION) {
        super.name?.let { return@lazy it }
        containingFile?.virtualFile?.nameWithoutExtension
    }

    /**
     * An actual json property element that represents module of this form
     */
    val moduleProperty by lazy(LazyThreadSafetyMode.PUBLICATION) { topLevelObject.findProperty("module") }

    /**
     * Module name of this form file.
     *
     * If the "module" property is not present in the form, it will return the name
     * of parent directory of the form file, which should correspond to its module
     */
    val moduleName by lazy(LazyThreadSafetyMode.PUBLICATION) {
        moduleProperty.valueAsStringOrNull()?.let { return@lazy it }
        containingFile?.parent?.name
    }

    override val l10nKeys by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val module = moduleName ?: return@lazy emptyList()
        val name = name ?: return@lazy emptyList()
        return@lazy "$module.form.$name".asList()
    }

    /**
     * Fully-qualified SOLAR form name
     */
    val fullName by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val name = name ?: return@lazy null
        val module = moduleName ?: return@lazy name
        return@lazy "$module.$name"
    }

    /**
     * An array of all group rows in this form.
     * - An empty array if `groupRows` property is declared in this form, but the array is empty
     * - `null` if `groupRows` property is not declared
     */
    val groupRows by lazy(LazyThreadSafetyMode.PUBLICATION) {
        topLevelObject.findProperty(FormGroupRow.ARRAY_NAME).toFormArrayElement<FormGroupRow>()
    }

    /**
     * An array of all groups in this form.
     * - An empty array if `groups` property is declared in this form, but the array is empty
     * - `null` if `groups` property is not declared
     */
    val groups by lazy(LazyThreadSafetyMode.PUBLICATION) {
        topLevelObject.findProperty(FormGroup.ARRAY_NAME).toFormArrayElement<FormGroup>()
    }

    /**
     * All groups that are contained in this form.
     * - If groups are represented as `groupRows` property, it will retrieve all inner groups and combine them into this list.
     * - If groups are represented as `groups` property, it will just use them.
     *
     * Never returns null, only empty list.
     */
    val allGroups by lazy(LazyThreadSafetyMode.PUBLICATION) {
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
    val allFields by lazy(LazyThreadSafetyMode.PUBLICATION) {
        allGroups.flatMap { it.fields }
    }

    /**
     * List of all requests in this form. Possible requests are:
     * - source
     * - save
     * - remove
     * - createSource
     */
    val requests by lazy(LazyThreadSafetyMode.PUBLICATION) {
        listOfNotNull(
            sourceRequest,
            saveRequest,
            removeRequest,
            createSourceRequest
        )
    }

    val sourceRequest by lazy(LazyThreadSafetyMode.PUBLICATION) { getRequestByType(FormRequest.RequestType.SOURCE) }
    val saveRequest by lazy(LazyThreadSafetyMode.PUBLICATION) { getRequestByType(FormRequest.RequestType.SAVE) }
    val removeRequest by lazy(LazyThreadSafetyMode.PUBLICATION) { getRequestByType(FormRequest.RequestType.REMOVE) }
    val createSourceRequest by lazy(LazyThreadSafetyMode.PUBLICATION) { getRequestByType(FormRequest.RequestType.CREATE_SOURCE) }

    /**
     * Data class from source request that this field uses
     */
    val dataClassFromSourceRequest by lazy(LazyThreadSafetyMode.PUBLICATION) {
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
    val inlineRequests: List<FormRequest> by lazy(LazyThreadSafetyMode.PUBLICATION) {
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
    val dataClassesFromInlineRequests by lazy(LazyThreadSafetyMode.PUBLICATION) {
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
    val allDataClassesFromRequests: List<UClass> by lazy(LazyThreadSafetyMode.PUBLICATION) {
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
    val relatedListFields: List<FormField> by lazy(LazyThreadSafetyMode.PUBLICATION) {
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
    val dataClassesFromListFields: List<UClass> by lazy(LazyThreadSafetyMode.PUBLICATION) {
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

    private val formReferences by lazy(LazyThreadSafetyMode.PUBLICATION) {
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