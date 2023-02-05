package com.solanteq.solar.plugin.jsonInclude

import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.base.createFormAndConfigure
import com.solanteq.solar.plugin.base.createIncludedForm
import com.solanteq.solar.plugin.reference.include.JsonIncludeReference
import org.junit.jupiter.api.Test

class JsonIncludeDirectoryReferenceTest : LightPluginTestBase() {

    @Test
    fun `test json include empty path no directory reference`() {
        fixture.createIncludedForm("includedForm", "", "{}")

        fixture.createFormAndConfigure(
            "rootForm", "module", """
                {
                  "json://includes/<caret>forms/includedForm.json"
                }
            """.trimIndent()
        )

        assertNoReferenceAtCaret<JsonIncludeReference>()
    }

    @Test
    fun `test json include basic path directory reference`() {
        fixture.createIncludedForm("includedForm", "test", "{}")

        fixture.createFormAndConfigure(
            "rootForm", "module", """
                {
                  "json://includes/forms/<caret>test/includedForm.json"
                }
            """.trimIndent()
        )

        assertReferencedElementNameEquals("test")
    }

    @Test
    fun `test json include non convenient path directory reference`() {
        fixture.createIncludedForm("includedForm", "test1/test2", "{}")

        fixture.createFormAndConfigure(
            "rootForm", "module", """
                {
                  "json://includes/forms/test1/<caret>test2/includedForm.json"
                }
            """.trimIndent()
        )

        assertReferencedElementNameEquals("test2")
    }

}