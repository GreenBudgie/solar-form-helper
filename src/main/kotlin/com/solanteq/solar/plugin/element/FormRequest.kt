package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.solanteq.solar.plugin.element.base.FormElement
import com.solanteq.solar.plugin.element.creator.FormElementCreator
import com.solanteq.solar.plugin.index.CallableServiceImplIndex
import com.solanteq.solar.plugin.index.DropdownIndex
import com.solanteq.solar.plugin.util.*
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.uast.UFile
import org.jetbrains.uast.toUElementOfType

/**
 * Represents request definitions in forms
 *
 * Examples:
 * ```
 * "request": "test.testService.find"
 *
 * "save": {
 *   "name": "test.testService.save",
 *   "group": "test"
 * }
 *
 * "source": {
 *   "name": "test.testService.save",
 *   "group": "test"
 *   "params": [
 *     {
 *       "name": "id",
 *       "value": "id"
 *     }
 *   ]
 * }
 * ```
 */
class FormRequest(
    sourceElement: JsonProperty
) : FormElement<JsonProperty>(sourceElement) {

    /**
     * Returns string literal element that represents the request string itself,
     * or null if request string element does exist.
     *
     * Request string looks like `test.testService.method`.
     */
    val requestStringElement by lazy(LazyThreadSafetyMode.PUBLICATION) {
        (sourceElement.value as? JsonStringLiteral)?.let { return@lazy it }
        val jsonObject = sourceElement.value as? JsonObject ?: return@lazy null
        val requestNameElement = jsonObject.propertyList.find { it.name == "name" } ?: return@lazy null
        return@lazy requestNameElement.value as? JsonStringLiteral
    }

    /**
     * Returns the group if this request, or null if it is not specified
     */
    val group by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val jsonObject = sourceElement.value as? JsonObject ?: return@lazy null
        val groupElement = jsonObject.propertyList.find { it.name == "group" } ?: return@lazy null
        return@lazy groupElement.valueAsStringOrNull()
    }

    /**
     * Whether this request has a `group` specified
     */
    val hasGroup by lazy(LazyThreadSafetyMode.PUBLICATION) {
        group != null
    }

    /**
     * Whether this request has a $dropdown group and can only lead
     */

    val isDropdownRequest by lazy(LazyThreadSafetyMode.PUBLICATION) {
        group == "\$dropdown"
    }

    /**
     * Parses the given request string and returns its data,
     * or null if request string has invalid format
     */
    val requestData by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val requestStringElement = requestStringElement ?: return@lazy null
        val rangeSplit = RangeSplit.from(requestStringElement)
        return@lazy RequestData(
            rangeSplit.getOrNull(0),
            rangeSplit.getOrNull(1),
            rangeSplit.getOrNull(2),
        )
    }

    /**
     * Returns method to which the request points to,
     * or null if request is invalid, no method/service is found or this is a dropdown request
     */
    val referencedMethod: PsiMethod? by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val methodName = requestData?.method?.text ?: return@lazy null
        val service = referencedService ?: return@lazy null
        return@lazy service.allMethods.find { it.name == methodName }
    }

    /**
     * Returns service to which the request points to,
     * or null if request is invalid, no service is found or this is a dropdown request
     */
    val referencedService by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val requestData = requestData ?: return@lazy null
        val module = requestData.module?.text ?: return@lazy null
        val clazz = requestData.clazz?.text ?: return@lazy null
        val fullServiceName = "${module}.${clazz}"

        val applicableFiles = CallableServiceImplIndex.getFilesContainingCallableServiceImpl(
            fullServiceName, project.allScope()
        ).mapNotNull { it.toPsiFile(project)?.toUElementOfType<UFile>() }
        val possibleServices = applicableFiles.flatMap { it.classes }

        findApplicableService(possibleServices, fullServiceName)
    }

    /**
     * If this request is a dropdown request ([isDropdownRequest]), returns the dropdown
     * enum that this request points to, or null if it cannot be resolved
     */
    val referencedDropdown by lazy(LazyThreadSafetyMode.PUBLICATION) {
        if(!isDropdownRequest) {
            return@lazy null
        }
        val dropdownModule = requestData?.module?.text ?: return@lazy null
        val dropdownName = requestData?.clazz?.text ?: return@lazy null
        val fullName = "$dropdownModule.$dropdownName"
        val fileWithDropdown = DropdownIndex.getFilesContainingDropdown(
            fullName,
            project.allScope()
        ).firstOrNull() ?: return@lazy null
        val uFile = fileWithDropdown.toPsiFile(project).toUElementOfType<UFile>() ?: return@lazy null
        val uClass = uFile.classes.find {
            it.hasAnnotation(DROPDOWN_ANNOTATION_FQ_NAME)
        } ?: return@lazy null
        return@lazy uClass.javaPsi
    }

    private fun findApplicableService(possibleServices: Collection<PsiClass>,
                                      requiredServiceName: String): PsiClass? {
        return possibleServices.find {
            it.serviceSolarName == requiredServiceName
        }
    }

    data class RequestData(
        val module: RangeSplitEntry?,
        val clazz: RangeSplitEntry?,
        val method: RangeSplitEntry?
    )

    enum class RequestType(
        val requestLiteral: String,
        val isFormRequest: Boolean
    ) {

        SOURCE("source", true),
        SAVE("save", true),
        REMOVE("remove", true),
        CREATE_SOURCE("createSource", true),
        INLINE_REQUEST("request", false),
        INLINE_COUNT_REQUEST("countRequest", false);

        companion object {

            val requestLiterals = entries.map { it.requestLiteral }.toTypedArray()
            val formRequests = entries.filter { it.isFormRequest }.toTypedArray()

        }

    }

    companion object : FormElementCreator<FormRequest, JsonProperty>() {

        override fun doCreate(sourceElement: JsonProperty): FormRequest? {
            if(sourceElement.name in RequestType.requestLiterals) {
                return FormRequest(sourceElement)
            }
            return null
        }

    }

}