package com.solanteq.solar.plugin.element.impl

import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.PsiType
import com.intellij.psi.util.TypeConversionUtil
import com.solanteq.solar.plugin.element.FormField
import com.solanteq.solar.plugin.element.FormFile
import com.solanteq.solar.plugin.element.base.impl.FormLocalizableElementImpl
import com.solanteq.solar.plugin.element.toFormElement
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UField
import org.jetbrains.uast.toUElementOfType

class FormFieldImpl(
    sourceElement: JsonObject
) : FormLocalizableElementImpl<JsonObject>(sourceElement, sourceElement), FormField {

    override val stringPropertyChain by lazy { name?.split(".") }

    override val propertyChain by lazy {
        val stringPropertyChain = stringPropertyChain ?: return@lazy emptyList()
        if(stringPropertyChain.isEmpty()) {
            return@lazy emptyList()
        }

        val propertyChain = mutableListOf<FormField.FieldProperty>()
        var currentDataClass = dataClass

        stringPropertyChain.forEach { fieldName ->
            if(currentDataClass == null) {
                propertyChain += FormField.FieldProperty(fieldName, null, null)
                return@forEach
            }

            val field = findFieldByNameInClass(currentDataClass!!, fieldName)
            if(field == null) {
                propertyChain += FormField.FieldProperty(fieldName, currentDataClass, null)
                return@forEach
            }

            propertyChain += FormField.FieldProperty(fieldName, currentDataClass, field)

            currentDataClass = psiTypeAsUClassOrNull(field.type)
        }

        return@lazy propertyChain.toList()
    }

    override val dataClass by lazy {
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

    override val sourceRequest by lazy {
        val jsonFile = sourceElement.containingFile as? JsonFile ?: return@lazy null
        val formFile = jsonFile.toFormElement<FormFile>() ?: return@lazy null
        return@lazy formFile.sourceRequest
    }

    private fun findFieldByNameInClass(uClass: UClass, fieldName: String): UField? =
        uClass.javaPsi.allFields.find { it.name == fieldName }.toUElementOfType()

    private fun substitutePsiType(superClass: PsiClass, derivedClass: PsiClass, psiType: PsiType): UClass? {
        val substitutedReturnType = TypeConversionUtil.getClassSubstitutor(
            superClass,
            derivedClass,
            PsiSubstitutor.EMPTY
        )?.substitute(psiType)
        val classReturnType = substitutedReturnType as? PsiClassType ?: return null
        return classReturnType.resolve().toUElementOfType()
    }

    private fun psiTypeAsUClassOrNull(psiType: PsiType): UClass? {
        val classReturnType = psiType as? PsiClassType ?: return null
        return classReturnType.resolve().toUElementOfType()
    }

    companion object {

        fun create(sourceElement: JsonElement): FormFieldImpl? {
            if(canBeCreatedAsArrayElement(sourceElement, FormField.ARRAY_NAME)) {
                return FormFieldImpl(sourceElement as JsonObject)
            }
            return null
        }

    }

}