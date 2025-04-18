package com.solanteq.solar.plugin.l10n

import com.intellij.codeInsight.hints.InlineInlayRenderer
import com.intellij.openapi.editor.Inlay
import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.base.configureByRootForms
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class L10nInlayProviderTest : LightPluginTestBase() {

    override fun getTestDataSuffix() = "l10n/inlay"

    @Test
    fun `test l10n inlay hints`() = with(fixture) {
        addL10nFile("l10n.json", L10nLocale.EN)
        addL10nFile("l10n2.json", L10nLocale.EN)
        val formFile = configureByRootForms("test", "form.json")

        doHighlighting()

        val inlays = editor.inlayModel.getAfterLineEndElementsInRange(0, formFile.textLength)
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