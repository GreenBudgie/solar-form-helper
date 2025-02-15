package com.solanteq.solar.plugin.jsonInclude

import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.base.assertCompletionsContainsExact
import com.solanteq.solar.plugin.base.createFormAndConfigure
import com.solanteq.solar.plugin.base.createIncludedForm
import org.junit.jupiter.api.Test

class JsonIncludeCompletionTest : LightPluginTestBase() {

    @Test
    fun `test json include basic path completion`() = with(fixture) {
        createIncludedForm("includedForm1", "dir1", "{}")
        createIncludedForm("includedForm2", "dir1", "{}")
        createIncludedForm("includedForm3", "differentPath", "{}")
        createIncludedForm("includedForm4", "dir1/differentPath", "{}")
        createIncludedForm("includedForm5", "", "{}")
        createFormAndConfigure("form", "module", """
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
    fun `test json include non convenient path completion`() = with(fixture) {
        createIncludedForm("includedForm1", "dir1/dir2", "{}")
        createIncludedForm("includedForm2", "dir1/dir2", "{}")
        createIncludedForm("includedForm3", "differentPath", "{}")
        createIncludedForm("includedForm4", "dir1/differentPath", "{}")
        createIncludedForm("includedForm4", "dir1/dir2/dir3", "{}")
        createFormAndConfigure("form", "module", """
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
    fun `test json include empty path completion`() = with(fixture) {
        createIncludedForm("includedForm1", "", "{}")
        createIncludedForm("includedForm2", "", "{}")
        createIncludedForm("includedForm3", "dir1", "{}")
        createIncludedForm("includedForm4", "dir2/dir3", "{}")
        createFormAndConfigure("form", "module", """
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
    fun `test json include completion between slashes empty path`() = with(fixture) {
        createIncludedForm("includedForm1", "", "{}")
        createIncludedForm("includedForm2", "test", "{}")
        createFormAndConfigure("form", "module", """
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
    fun `test json include completion between slashes non empty path`() = with(fixture) {
        createIncludedForm("includedForm1", "", "{}")
        createIncludedForm("includedForm2", "test1", "{}")
        createIncludedForm("includedForm3", "test1", "{}")
        createIncludedForm("includedForm4", "test1/test2", "{}")
        createFormAndConfigure("form", "module", """
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