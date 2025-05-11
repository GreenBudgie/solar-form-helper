package com.solanteq.solar.plugin.upgradeConverter

import com.intellij.lang.jvm.JvmModifier
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.util.InheritanceUtil
import org.jetbrains.uast.UClass

/**
 * @author nbundin
 * @since %CURRENT_VERSION%
 */
object UpgradeConverterUtils {

    private const val ABSTRACT_ENTITY_UPGRADE_CONVERTER_FQ_NAME =
        "com.solanteq.solar.commons.upgrade.converter.AbstractEntityUpgradeConverter"

    fun getUpgradeConverterData(project: Project, uClass: UClass): UpgradeConverterData? {
        if (uClass.isInterface) {
            return null
        }

        val javaClass = uClass.javaPsi
        val isAbstract = javaClass.hasModifier(JvmModifier.ABSTRACT)
        if (isAbstract) {
            return null
        }

        var foundExtendedConverter: PsiJavaCodeReferenceElement? = null
        InheritanceUtil.processSupers(javaClass, true) { superclass ->
            foundExtendedConverter = findExtendedAbstractConverter(superclass)
            return@processSupers foundExtendedConverter == null
        }

        val extendedConverter = foundExtendedConverter ?: return null
        return UpgradeConverterData(project, uClass, javaClass, extendedConverter)
    }

    private fun findExtendedAbstractConverter(javaClass: PsiClass): PsiJavaCodeReferenceElement? {
        return javaClass.extendsList?.referenceElements?.find {
            it.qualifiedName == ABSTRACT_ENTITY_UPGRADE_CONVERTER_FQ_NAME
        }
    }



}