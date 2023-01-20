package com.solanteq.solar.plugin

import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.base.createFormAndConfigure
import com.solanteq.solar.plugin.base.createIncludedForm
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class JsonIncludeTest : LightPluginTestBase() {

    override fun getTestDataSuffix() = "jsonInclude"

    @ParameterizedTest
    @ValueSource(strings = [
        "json",
        "json?",
        "json-flat",
        "json-flat?",
    ])
    fun `test reference to included form`(prefix: String) {
        fixture.createIncludedForm("includedForm", "test", "{}")
        fixture.createFormAndConfigure("form", """
            {
                "$prefix://includes/forms/test/<caret>includedForm.json"
            }
        """.trimIndent())

        assertReferencedElementNameEquals("includedForm.json")
    }

    @Test
    fun `test reference to included form with non convenient path`() {
        fixture.createIncludedForm("includedForm", "dir1/dir2", "{}")
        fixture.createFormAndConfigure("form", """
            {
                "json://includes/forms/dir1/dir2/<caret>includedForm.json"
            }
        """.trimIndent())

        assertReferencedElementNameEquals("includedForm.json")
    }

    @Test
    fun `test json include form completion`() {
        fixture.createIncludedForm("includedForm1", "dir1/dir2", "{}")
        fixture.createIncludedForm("includedForm2", "dir1/dir2", "{}")
        fixture.createIncludedForm("includedForm3", "differentPath", "{}")
        fixture.createIncludedForm("includedForm4", "dir1/differentPath", "{}")
        fixture.createFormAndConfigure("form", """
            {
                "json://includes/forms/dir1/dir2/<caret>"
            }
        """.trimIndent())

        assertCompletionsContainsExact(
            "includedForm1.json",
            "includedForm2.json"
        )
    }

}