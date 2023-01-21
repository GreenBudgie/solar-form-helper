package com.solanteq.solar.plugin.jsonInclude

import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.base.createFormAndConfigure
import com.solanteq.solar.plugin.base.createIncludedForm
import org.junit.jupiter.api.Test

class JsonIncludeCompletionTest : LightPluginTestBase() {

    @Test
    fun `test json include form basic path completion`() {
        fixture.createIncludedForm("includedForm1", "dir1", "{}")
        fixture.createIncludedForm("includedForm2", "dir1", "{}")
        fixture.createIncludedForm("includedForm3", "differentPath", "{}")
        fixture.createIncludedForm("includedForm4", "dir1/differentPath", "{}")
        fixture.createIncludedForm("includedForm5", "", "{}")
        fixture.createFormAndConfigure("form", """
            {
                "json://includes/forms/dir1/<caret>"
            }
        """.trimIndent())

        assertCompletionsContainsExact(
            "includedForm1.json",
            "includedForm2.json",
            "differentPath"
        )
    }

    @Test
    fun `test json include form non convenient path completion`() {
        fixture.createIncludedForm("includedForm1", "dir1/dir2", "{}")
        fixture.createIncludedForm("includedForm2", "dir1/dir2", "{}")
        fixture.createIncludedForm("includedForm3", "differentPath", "{}")
        fixture.createIncludedForm("includedForm4", "dir1/differentPath", "{}")
        fixture.createIncludedForm("includedForm4", "dir1/dir2/dir3", "{}")
        fixture.createFormAndConfigure("form", """
            {
                "json://includes/forms/dir1/dir2/<caret>"
            }
        """.trimIndent())

        assertCompletionsContainsExact(
            "includedForm1.json",
            "includedForm2.json",
            "dir3"
        )
    }

    @Test
    fun `test json include form empty path completion`() {
        fixture.createIncludedForm("includedForm1", "", "{}")
        fixture.createIncludedForm("includedForm2", "", "{}")
        fixture.createIncludedForm("includedForm3", "dir1", "{}")
        fixture.createIncludedForm("includedForm4", "dir2/dir3", "{}")
        fixture.createFormAndConfigure("form", """
            {
                "json://includes/forms/<caret>"
            }
        """.trimIndent())

        assertCompletionsContainsExact(
            "includedForm1.json",
            "includedForm2.json",
            "dir1",
            "dir2"
        )
    }

}