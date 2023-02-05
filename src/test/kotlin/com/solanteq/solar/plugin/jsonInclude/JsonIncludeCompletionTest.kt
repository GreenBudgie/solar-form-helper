package com.solanteq.solar.plugin.jsonInclude

import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.base.createFormAndConfigure
import com.solanteq.solar.plugin.base.createIncludedForm
import org.junit.jupiter.api.Test

class JsonIncludeCompletionTest : LightPluginTestBase() {

    @Test
    fun `test json include basic path completion`() {
        fixture.createIncludedForm("includedForm1", "dir1", "{}")
        fixture.createIncludedForm("includedForm2", "dir1", "{}")
        fixture.createIncludedForm("includedForm3", "differentPath", "{}")
        fixture.createIncludedForm("includedForm4", "dir1/differentPath", "{}")
        fixture.createIncludedForm("includedForm5", "", "{}")
        fixture.createFormAndConfigure("form", "module", """
            {
                "json://includes/forms/dir1/<caret>"
            }
        """.trimIndent()
        )

        assertCompletionsContainsExact(
            "includedForm1.json",
            "includedForm2.json",
            "differentPath"
        )
    }

    @Test
    fun `test json include non convenient path completion`() {
        fixture.createIncludedForm("includedForm1", "dir1/dir2", "{}")
        fixture.createIncludedForm("includedForm2", "dir1/dir2", "{}")
        fixture.createIncludedForm("includedForm3", "differentPath", "{}")
        fixture.createIncludedForm("includedForm4", "dir1/differentPath", "{}")
        fixture.createIncludedForm("includedForm4", "dir1/dir2/dir3", "{}")
        fixture.createFormAndConfigure("form", "module", """
            {
                "json://includes/forms/dir1/dir2/<caret>"
            }
        """.trimIndent()
        )

        assertCompletionsContainsExact(
            "includedForm1.json",
            "includedForm2.json",
            "dir3"
        )
    }

    @Test
    fun `test json include empty path completion`() {
        fixture.createIncludedForm("includedForm1", "", "{}")
        fixture.createIncludedForm("includedForm2", "", "{}")
        fixture.createIncludedForm("includedForm3", "dir1", "{}")
        fixture.createIncludedForm("includedForm4", "dir2/dir3", "{}")
        fixture.createFormAndConfigure("form", "module", """
            {
                "json://includes/forms/<caret>"
            }
        """.trimIndent()
        )

        assertCompletionsContainsExact(
            "includedForm1.json",
            "includedForm2.json",
            "dir1",
            "dir2"
        )
    }

    @Test
    fun `test json include completion between slashes empty path`() {
        fixture.createIncludedForm("includedForm1", "", "{}")
        fixture.createIncludedForm("includedForm2", "test", "{}")
        fixture.createFormAndConfigure("form", "module", """
            {
                "json://includes/forms/<caret>//someText///"
            }
        """.trimIndent()
        )

        assertCompletionsContainsExact(
            "includedForm1.json",
            "test"
        )
    }

    @Test
    fun `test json include completion between slashes non empty path`() {
        fixture.createIncludedForm("includedForm1", "", "{}")
        fixture.createIncludedForm("includedForm2", "test1", "{}")
        fixture.createIncludedForm("includedForm3", "test1", "{}")
        fixture.createIncludedForm("includedForm4", "test1/test2", "{}")
        fixture.createFormAndConfigure("form", "module", """
            {
                "json://includes/forms/test1/<caret>/"
            }
        """.trimIndent()
        )

        assertCompletionsContainsExact(
            "includedForm2.json",
            "includedForm3.json",
            "test2"
        )
    }

}