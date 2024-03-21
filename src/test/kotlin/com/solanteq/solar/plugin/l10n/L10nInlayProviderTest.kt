package com.solanteq.solar.plugin.l10n

import com.intellij.codeInsight.hints.InlineInlayRenderer
import com.intellij.openapi.editor.Inlay
import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.base.configureByRootForms
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class L10nInlayProviderTest : LightPluginTestBase() {

    override fun getTestDataSuffix() = "l10n/inlay"

    @Test
    fun testL10nInlayHints() {
        L10nTestUtils.addL10nFile(fixture, "l10n.json", L10nLocale.EN)
        L10nTestUtils.addL10nFile(fixture, "l10n2.json", L10nLocale.EN)
        val formFile = fixture.configureByRootForms("test", "form.json")

        fixture.doHighlighting()

        val inlays = fixture.editor.inlayModel.getAfterLineEndElementsInRange(0, formFile.textLength)
        assertEquals(5, inlays.size)
        assertInlayTextEquals(inlays[0], "form")
        assertInlayTextEquals(inlays[1], "group")
        assertInlayTextEquals(inlays[2], "field")
        assertInlayTextEquals(inlays[3], "nested field")
        assertInlayTextEquals(inlays[4], "multiple l10ns +1")
    }

    private fun assertInlayTextEquals(inlay: Inlay<*>, expectedText: String) {
        val renderer = inlay.renderer as InlineInlayRenderer
        val actualText = renderer.toString()
        assertEquals(expectedText, actualText)
    }

}