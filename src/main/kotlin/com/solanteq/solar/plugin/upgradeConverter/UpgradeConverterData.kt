package com.solanteq.solar.plugin.upgradeConverter

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiJavaCodeReferenceElement
import org.jetbrains.uast.*

/**
 * @author nbundin
 * @since %CURRENT_VERSION%
 */
data class UpgradeConverterData(
    val project: Project,
    val converterUClass: UClass,
    val converterClass: PsiClass,
    val extendedAbstractConverter: PsiJavaCodeReferenceElement
) {

    val versionTo by lazy {
        findFieldOrMethodValue(converterClass, "versionTo", "getVersionTo")
    }

    val module by lazy {
        findFieldOrMethodValue(converterClass, "module", "getModule")
    }

    val tableName by lazy {
        findTableName(project, converterClass, extendedAbstractConverter)
    }

    val classDeclarationPsi by lazy {
        converterUClass.uastAnchor?.sourcePsi
    }

    val l10nKey by lazy {
        val module = module ?: return@lazy null
        val versionTo = versionTo ?: return@lazy null
        val tableName = tableName ?: return@lazy null
        val l10nVersionTo = versionTo.replace('.', '_')
        return@lazy "${module}.upg.${tableName}.${l10nVersionTo}.job_name"
    }

    private fun findTableName(
        project: Project,
        javaClass: PsiClass,
        abstractEntityUpgradeConverter: PsiJavaCodeReferenceElement,
    ): String? {
        val declaredTableName = findFieldOrMethodValue(javaClass, "tableName", "getTableName")

        if (declaredTableName != null) {
            return declaredTableName
        }

        val entityClassTypeParameter = abstractEntityUpgradeConverter
            .typeParameters
            .firstOrNull() as? PsiClassType ?: return null
        val entityClass = entityClassTypeParameter.resolve() ?: return null
        return findTableNameRecursively(entityClass, project)
    }

    private fun findFieldOrMethodValue(javaClass: PsiClass, fieldName: String, methodName: String): String? {
        val field = javaClass.findFieldByName(fieldName, true)?.toUElementOfType<UField>()
        val valueByField = field?.uastInitializer?.evaluateString()
        if (valueByField != null) {
            return valueByField
        }

        val method = javaClass.findMethodsByName(methodName, true)
            .firstOrNull()
            ?.toUElementOfType<UMethod>() ?: return null
        val methodBody = method.uastBody as? UBlockExpression ?: return null
        val returnExpression = methodBody.expressions
            .filterIsInstance<UReturnExpression>()
            .firstOrNull() ?: return null
        return returnExpression.returnExpression?.evaluateString()
    }

    private tailrec fun findTableNameRecursively(psiClass: PsiClass, project: Project): String? {
        val tableName = getTableNameFromAnnotations(psiClass, project)
        if (tableName != null) {
            return tableName
        }

        val superclass = psiClass.superClass ?: return null
        return findTableNameRecursively(superclass, project)
    }

    private fun getTableNameFromAnnotations(psiClass: PsiClass, project: Project): String? {
        val tableAnnotation = psiClass.getAnnotation(TABLE_ANNOTATION_FQ_NAME) ?: return null
        val nameValue = tableAnnotation.findAttributeValue("name") ?: return null
        return JavaPsiFacade.getInstance(project).constantEvaluationHelper
            .computeConstantExpression(nameValue) as? String
    }

    private companion object {

        const val ABSTRACT_ENTITY_UPGRADE_CONVERTER_FQ_NAME =
            "com.solanteq.solar.commons.upgrade.converter.AbstractEntityUpgradeConverter"

        const val TABLE_ANNOTATION_FQ_NAME = "javax.persistence.Table"

    }

}