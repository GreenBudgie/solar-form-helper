package com.solanteq.solar.plugin.inspection

import com.intellij.codeInspection.AbstractBaseUastLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.jvm.JvmModifier
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.solanteq.solar.plugin.l10n.search.L10nSearch
import org.jetbrains.uast.*

/**
 * @author nbundin
 * @since %CURRENT_VERSION%
 */
class UpgradeConverterMissingL10nInspection : AbstractBaseUastLocalInspectionTool(UClass::class.java) {

    override fun checkClass(
        aClass: UClass,
        manager: InspectionManager,
        isOnTheFly: Boolean,
    ): Array<ProblemDescriptor>? {
        if (aClass.isInterface) {
            return null
        }

        val javaClass = aClass.javaPsi
        val isAbstract = javaClass.hasModifier(JvmModifier.ABSTRACT)
        if (isAbstract) {
            return null
        }

        val abstractEntityUpgradeConverter = javaClass.extendsList?.referenceElements?.find {
            it.qualifiedName == ABSTRACT_ENTITY_UPGRADE_CONVERTER_FQ_NAME
        } ?: return null

        val versionTo = findFieldOrMethodValue(javaClass, "versionTo", "getVersionTo") ?: return null
        val module = findFieldOrMethodValue(javaClass, "module", "getModule") ?: return null

        val entityClassTypeParameter = abstractEntityUpgradeConverter
            .typeParameters
            .firstOrNull() as? PsiClassType ?: return null
        val entityClass = entityClassTypeParameter.resolve() ?: return null
        val tableName = findTableNameRecursively(entityClass, manager.project) ?: return null

        val l10nVersionTo = versionTo.replace('.', '_')
        val l10nKey = "${module}.upg.${tableName}.${l10nVersionTo}.job_name"
        val foundL10ns = L10nSearch.search(manager.project).byKey(l10nKey).findFiles()
        if (foundL10ns.isEmpty()) {
            return arrayOf(
                manager.createProblemDescriptor(
                    aClass.sourcePsi!!,
                    "L10n not found!",
                    emptyArray(),
                    ProblemHighlightType.WARNING,
                    isOnTheFly,
                    false
                )
            )
        }

        return null
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
        val literalExpression = returnExpression.returnExpression as? ULiteralExpression ?: return null
        return literalExpression.evaluateString()
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