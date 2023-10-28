package com.solanteq.solar.plugin.element

import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.solanteq.solar.plugin.element.base.AbstractFormElement
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
) : AbstractFormElement<JsonProperty>(sourceElement) {

    /**
     * Returns string literal element that represents the request string itself,
     * or null if request string element does exist.
     *
     * Request string looks like `test.testService.method`
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
        val module = rangeSplit.getOrNull(0)
        val clazz = rangeSplit.getOrNull(1)
        val method = rangeSplit.getOrNull(2)
        val service = if (module != null && clazz != null) {
            val serviceNameRange = TextRange(module.range.startOffset, clazz.range.endOffset)
            val serviceName = "${module.text}.${clazz.text}"
            RangeSplitEntry(serviceNameRange, serviceName, 0)
        } else null
        return@lazy RequestData(module, clazz, method, service)
    }

    /**
     * Returns method to which the request points to,
     * or null if request is invalid, no method/service is found or this is a dropdown request.
     *
     * Can return non-callable method.
     */
    val referencedMethod: PsiMethod? by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val methodName = requestData?.method?.text ?: return@lazy null
        val service = referencedService ?: return@lazy null
        return@lazy service.allMethods.find { it.name == methodName }
    }

    /**
     * Returns method from interface annotated `@Callable` to which the request points to.
     * Returns null if:
     * - Request is invalid
     * - No service is found
     * - Method with provided name is not found
     * - Method is found, but it has no `@Callable` annotation
     * - This is a dropdown request
     */
    val referencedCallableMethod: PsiMethod? by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val methodName = requestData?.method?.text ?: return@lazy null
        val service = referencedService ?: return@lazy null
        return@lazy service.allMethods
            .filter { it.name == methodName }
            .find { it.hasAnnotation(CALLABLE_ANNOTATION_FQ_NAME) }
    }

    /**
     * Returns service to which the request points to,
     * or null if request is invalid, no service is found or this is a dropdown request
     */
    val referencedService by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val fullServiceName = requestData?.service?.text ?: return@lazy null

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

    /**
     * Whether the request string contains module, name and method
     */
    val isValid by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val requestStringElement = requestStringElement ?: return@lazy false
        val rangeSplit = RangeSplit.from(requestStringElement)
        if (rangeSplit.size != 3) return@lazy false
        val requestData = requestData ?: return@lazy false
        if (requestData.module?.text.isNullOrBlank()) return@lazy false
        if (requestData.clazz?.text.isNullOrBlank()) return@lazy false
        if (requestData.method?.text.isNullOrBlank()) return@lazy false
        true
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
        val method: RangeSplitEntry?,
        val service: RangeSplitEntry?
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