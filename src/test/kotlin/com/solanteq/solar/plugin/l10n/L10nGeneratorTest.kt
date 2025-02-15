package com.solanteq.solar.plugin.l10n

import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.l10n.generator.L10nGenerator
import org.junit.jupiter.api.Test

class L10nGeneratorTest : LightPluginTestBase() {

    @Test
    fun `generate l10n - empty file`() = with(fixture) {
        val file = createL10nFileAndConfigure("l10n", "test1" to "test1")

        L10nGenerator.generateL10n(
            key = "test_key",
            value = "\"test value\"",
            file
        )
    }

}