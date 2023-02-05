package com.solanteq.solar.plugin.jsonInclude

import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.base.createFormAndConfigure
import com.solanteq.solar.plugin.base.createIncludedForm
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class JsonIncludeFormReferenceTest : LightPluginTestBase() {

    @ParameterizedTest
    @ValueSource(strings = [
        "json",
        "json?",
        "json-flat",
        "json-flat?",
    ])
    fun `test reference to included form`(prefix: String) {
        fixture.createIncludedForm("includedForm", "test", "{}")
        fixture.createFormAndConfigure("form", "module", """
            {
                "$prefix://includes/forms/test/<caret>includedForm.json"
            }
        """.trimIndent()
        )

        assertReferencedElementNameEquals("includedForm.json")
    }

    @Test
    fun `test reference to included form with non convenient path`() {
        fixture.createIncludedForm("includedForm", "dir1/dir2", "{}")
        fixture.createFormAndConfigure("form", "module", """
            {
                "json://includes/forms/dir1/dir2/<caret>includedForm.json"
            }
        """.trimIndent()
        )

        assertReferencedElementNameEquals("includedForm.json")
    }

    @Test
    fun `test reference to included form with empty path`() {
        fixture.createIncludedForm("includedForm", "", "{}")
        fixture.createFormAndConfigure("form", "module", """
            {
                "json://includes/forms/<caret>includedForm.json"
            }
        """.trimIndent()
        )

        assertReferencedElementNameEquals("includedForm.json")
    }

}