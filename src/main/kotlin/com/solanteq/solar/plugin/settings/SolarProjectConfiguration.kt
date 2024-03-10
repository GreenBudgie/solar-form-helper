package com.solanteq.solar.plugin.settings

import com.intellij.openapi.components.*
import com.solanteq.solar.plugin.l10n.L10nLocale

@Service
@State(name = "solar-project-configuration", storages = [Storage("solarProject.xml")])
class SolarProjectConfiguration :
    SimplePersistentStateComponent<SolarProjectConfigurationState>(SolarProjectConfigurationState())

class SolarProjectConfigurationState : BaseState() {

    var locale by enum(L10nLocale.EN)

}