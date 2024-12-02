package com.solanteq.solar.plugin.l10n

import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.l10n.generator.L10nGenerator
import org.junit.jupiter.api.Test

class L10nGeneratorTest : LightPluginTestBase() {

    @Test
    fun `basic test`() {
        val file = L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n", "test1" to "test1")

        L10nGenerator.generateL10n(
            "test_key",
            "\"test value\"",
            file
        )
    }

}