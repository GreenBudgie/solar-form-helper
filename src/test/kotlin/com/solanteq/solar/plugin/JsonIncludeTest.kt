package com.solanteq.solar.plugin

import com.intellij.testFramework.RunsInEdt
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@RunsInEdt
class JsonIncludeTest : FormTestBase() {

    override fun getTestDataSuffix() = "jsonInclude"

    @ParameterizedTest
    @ValueSource(strings = [
        "json",
        "json?",
        "json-flat",
        "json-flat?",
    ])
    fun `test included form reference from json include`(prefix: String) {
        fixture.createIncludedForm("includedForm", "test", "{}")
        fixture.createFormAndConfigure("form", """
            {
                "$prefix://includes/forms/test/<caret>includedForm.json"
            }
        """.trimIndent())

        assertReferencedElementName("includedForm")
    }

}