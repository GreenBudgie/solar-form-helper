package com.solanteq.solar.plugin.settings

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.hints.InlayHintsFactory
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.panel
import com.solanteq.solar.plugin.bundle.SolarBundle
import com.solanteq.solar.plugin.l10n.L10nLocale
import javax.swing.JComponent

class SolarProjectConfigurable(private val project: Project) : Configurable {

    private var localeComponent: ComboBox<String>? = null

    override fun createComponent(): JComponent {
        val configuration = service<SolarProjectConfiguration>()
        return panel {
            row(SolarBundle.message("configurable.form.locale")) {
                localeComponent = comboBox(L10nLocale.entries.map { it.name }).component.apply {
                    selectedItem = configuration.state.locale.name
                }
            }
        }
    }

    override fun isModified(): Boolean {
        val localeComponent = localeComponent ?: return false

        val configuration = service<SolarProjectConfiguration>()

        return configuration.state.locale.name != localeComponent.selectedItem as String
    }

    override fun apply() {
        val localeComponent = localeComponent ?: return

        val configuration = service<SolarProjectConfiguration>()
        configuration.state.locale = L10nLocale.valueOf(localeComponent.selectedItem as String)

        InlayHintsFactory.forceHintsUpdateOnNextPass()
        DaemonCodeAnalyzer.getInstance(project).restart()
    }

    override fun getDisplayName() = SolarBundle.message("configurable.name.solar.project.settings")

    override fun disposeUIResources() {
        localeComponent = null
    }

}