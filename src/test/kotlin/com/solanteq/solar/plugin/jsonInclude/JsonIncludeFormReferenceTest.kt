package com.solanteq.solar.plugin.jsonInclude

import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.base.assertReferencedElementNameEquals
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
    fun `test reference to included form`(prefix: String) = with(fixture) {
        createIncludedForm("includedForm", "test", "{}")
        createFormAndConfigure("form", "module", """
            {
                "$prefix://includes/forms/test/<caret>includedForm.json"
            }
        """.trimIndent()
        )

        assertReferencedElementNameEquals("includedForm.json")
    }

    @Test
    fun `test reference to included form with non convenient path`() = with(fixture) {
        createIncludedForm("includedForm", "dir1/dir2", "{}")
        createFormAndConfigure("form", "module", """
            {
                "json://includes/forms/dir1/dir2/<caret>includedForm.json"
            }
        """.trimIndent()
        )

        assertReferencedElementNameEquals("includedForm.json")
    }

    @Test
    fun `test reference to included form with empty path`() = with(fixture) {
        createIncludedForm("includedForm", "", "{}")
        createFormAndConfigure("form", "module", """
            {
                "json://includes/forms/<caret>includedForm.json"
            }
        """.trimIndent()
        )

        assertReferencedElementNameEquals("includedForm.json")
    }

}