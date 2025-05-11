package com.solanteq.solar.plugin.upgradeConverter.inspection

import com.intellij.codeInspection.AbstractBaseUastLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.solanteq.solar.plugin.bundle.SolarBundle
import com.solanteq.solar.plugin.l10n.L10nLocale
import com.solanteq.solar.plugin.l10n.search.L10nSearch
import com.solanteq.solar.plugin.upgradeConverter.UpgradeConverterUtils
import org.jetbrains.uast.UClass

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
        val upgradeConverterData = UpgradeConverterUtils.getUpgradeConverterData(manager.project, aClass) ?: return null
        val l10nKey = upgradeConverterData.l10nKey ?: return null
        val classDeclarationPsi = upgradeConverterData.classDeclarationPsi ?: return null
        val foundL10ns = L10nSearch.search(manager.project).byKey(l10nKey).findObjects()
        val foundL10nLocales = foundL10ns.map { it.locale }.toSet()
        val missingL10nLocales = L10nLocale.entries - foundL10nLocales
        if (missingL10nLocales.isEmpty()) {
            return null
        }

        val missingLocalesInfo = missingL10nLocales.joinToString(", ") { it.displayName }
        return arrayOf(
            manager.createProblemDescriptor(
                classDeclarationPsi,
                SolarBundle.message("inspection.message.upgrade.converter.missing.l10n", missingLocalesInfo),
                emptyArray(),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                isOnTheFly,
                false
            )
        )
    }

}