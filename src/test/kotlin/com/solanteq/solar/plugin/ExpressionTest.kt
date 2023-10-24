package com.solanteq.solar.plugin

import com.solanteq.solar.plugin.base.*
import com.solanteq.solar.plugin.reference.expression.ExpressionDeclarationProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ExpressionTest : LightPluginTestBase() {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "requiredWhen",
            "visibleWhen",
            "editableWhen",
            "removableWhen",
            "editModeWhen",
            "success",
            "warning",
            "error",
            "info",
            "muted"
        ]
    )
    fun `test expression name reference - basic case for all possible literals`(propertyName: String) {
        fixture.createFormAndConfigure(
            "form", "abc", """
            {
              "$propertyName": "never<caret>"
              "expressions": [
                {
                  "name": "never",
                  "value": "false"
                }
              ]
            }
        """.trimIndent()
        )

        assertReferencedSymbolNameEquals("never")
    }

    @Test
    fun `test expression name reference - expression in included form`() {
        fixture.createIncludedForm(
            "includedForm", "abc", """
            [
              {
                "name": "never",
                "value": "false"
              }
            ]
        """.trimIndent()
        )

        fixture.createFormAndConfigure(
            "form", "abc", """
            {
              "requiredWhen": "never<caret>"
              "expressions": "json://includes/forms/abc/includedForm.json"
            }
        """.trimIndent()
        )

        assertReferencedSymbolNameEquals("never")
    }

    @Test
    fun `test expression name reference - expression in root form, but referenced from included form`() {
        fixture.createIncludedFormAndConfigure(
            "includedForm", "abc", """
            [
              {
                "visibleWhen": "<caret>never"
              }
            ]
        """.trimIndent()
        )

        fixture.createForm(
            "form", "abc", """
            {
              "groups": "json://includes/forms/abc/includedForm.json"
              "expressions": [
                {
                  "name": "never",
                  "value": "false"
                }
              ]
            }
        """.trimIndent()
        )

        assertReferencedSymbolNameEquals("never")
    }

    @Test
    fun `test expression name completion`() {
        fixture.createFormAndConfigure(
            "form", "abc", """
            {
              "visibleWhen": "<caret>"
              "expressions": [
                {
                  "name": "expr1",
                  "value": "false"
                },
                {
                  "name": "expr2",
                  "value": "true"
                }
              ]
            }
        """.trimIndent()
        )

        assertCompletionsContainsExact("expr1", "expr2")
    }

    @Test
    fun `test expression name rename from reference - in the same root form`() {
        fixture.createFormAndConfigure(
            "form", "abc", """
            {
              "visibleWhen": "expr1<caret>",
              "expressions": [
                {
                  "name": "expr1",
                  "value": "false"
                },
                {
                  "name": "expr2",
                  "value": "true"
                }
              ]
            }
        """.trimIndent()
        )

        renameFormSymbolReference("newName")

        val expectedRenameResult = """
            {
              "visibleWhen": "newName",
              "expressions": [
                {
                  "name": "newName",
                  "value": "false"
                },
                {
                  "name": "expr2",
                  "value": "true"
                }
              ]
            }
        """.trimIndent()

        fixture.checkResult(expectedRenameResult)
    }

    @Test
    fun `test expression name rename from declaration - in the same root form`() {
        fixture.createFormAndConfigure(
            "form", "abc", """
            {
              "visibleWhen": "expr1",
              "expressions": [
                {
                  "name": "expr1<caret>",
                  "value": "false"
                },
                {
                  "name": "expr2",
                  "value": "true"
                }
              ]
            }
        """.trimIndent()
        )

        renameFormSymbolDeclaration(ExpressionDeclarationProvider(), "newName")

        val expectedRenameResult = """
            {
              "visibleWhen": "newName",
              "expressions": [
                {
                  "name": "newName",
                  "value": "false"
                },
                {
                  "name": "expr2",
                  "value": "true"
                }
              ]
            }
        """.trimIndent()

        fixture.checkResult(expectedRenameResult)
    }

    @Test
    fun `test expression name rename from reference - in different included forms`() {
        val formWithReference = fixture.createIncludedFormAndConfigure(
            "formWithReference", "abc", """
            [
              {
                "visibleWhen": "<caret>expr1"
              }
            ]
        """.trimIndent()
        )

        val formWithDeclaration = fixture.createIncludedForm(
            "formWithDeclaration", "abc", """
            [
              {
                "name": "expr1",
                "value": "false"
              },
              {
                "name": "expr2",
                "value": "true"
              }
            ]
        """.trimIndent()
        )

        val rootForm = fixture.createForm(
            "form", "abc", """
            {
              "editableWhen": "expr1",
              "groups": "json://includes/forms/abc/formWithReference.json",
              "expressions": "json?://includes/forms/abc/formWithDeclaration.json"
            }
        """.trimIndent()
        )

        renameFormSymbolReference("newName")

        val formWithReferenceResult = """
            [
              {
                "visibleWhen": "newName"
              }
            ]
        """.trimIndent()
        val formWithDeclarationResult = """
            [
              {
                "name": "newName",
                "value": "false"
              },
              {
                "name": "expr2",
                "value": "true"
              }
            ]
        """.trimIndent()
        val rootFormResult = """
            {
              "editableWhen": "newName",
              "groups": "json://includes/forms/abc/formWithReference.json",
              "expressions": "json?://includes/forms/abc/formWithDeclaration.json"
            }
        """.trimIndent()

        fixture.openFileInEditor(formWithReference.virtualFile)
        fixture.checkResult(formWithReferenceResult)
        fixture.openFileInEditor(formWithDeclaration.virtualFile)
        fixture.checkResult(formWithDeclarationResult)
        fixture.openFileInEditor(rootForm.virtualFile)
        fixture.checkResult(rootFormResult)
    }

    @Test
    fun `test expression name rename from reference - multiple declarations`() {
        val rootForm1 = fixture.createForm(
            "form1", "abc", """
            {
              "visibleWhen": "never"
              "groups": "json://includes/forms/abc/includedForm.json",
              "expressions": [
                {
                  "name": "never",
                  "value": "false"
                }
              ]
            }
        """.trimIndent()
        )

        val rootForm2 = fixture.createForm(
            "form2", "abc", """
            {
              "groups": "json://includes/forms/abc/includedForm.json",
              "expressions": [
                {
                  "name": "never",
                  "value": "false"
                }
              ]
            }
        """.trimIndent()
        )

        val includedForm = fixture.createIncludedFormAndConfigure(
            "includedForm", "abc", """
            [
              {
                "visibleWhen": "never<caret>"
              }
            ]
        """.trimIndent()
        )

        renameFormSymbolReference("newName")

        val rootForm1Result = """
            {
              "visibleWhen": "newName"
              "groups": "json://includes/forms/abc/includedForm.json",
              "expressions": [
                {
                  "name": "newName",
                  "value": "false"
                }
              ]
            }
        """.trimIndent()

        val rootForm2Result = """
            {
              "groups": "json://includes/forms/abc/includedForm.json",
              "expressions": [
                {
                  "name": "newName",
                  "value": "false"
                }
              ]
            }
        """.trimIndent()

        val includedFormResult = """
            [
              {
                "visibleWhen": "newName"
              }
            ]
        """.trimIndent()

        fixture.openFileInEditor(rootForm1.virtualFile)
        fixture.checkResult(rootForm1Result)
        fixture.openFileInEditor(rootForm2.virtualFile)
        fixture.checkResult(rootForm2Result)
        fixture.openFileInEditor(includedForm.virtualFile)
        fixture.checkResult(includedFormResult)
    }

    @Test
    fun `test expression name rename from declaration - declaration in root form, reference in included`() {
        val includedForm = fixture.createIncludedForm(
            "includedForm", "abc", """
            [
              {
                "visibleWhen": "expr"
              }
            ]
        """.trimIndent()
        )

        val rootForm = fixture.createFormAndConfigure(
            "form", "abc", """
            {
              "editableWhen": "expr",
              "groups": "json://includes/forms/abc/includedForm.json",
              "expressions": [
                {
                  "name": "expr<caret>",
                  "value": "false"
                }
              ]
            }
        """.trimIndent()
        )

        renameFormSymbolDeclaration(ExpressionDeclarationProvider(), "newName")

        val includedFormResult = """
            [
              {
                "visibleWhen": "newName"
              }
            ]
        """.trimIndent()
        val rootFormResult = """
            {
              "editableWhen": "newName",
              "groups": "json://includes/forms/abc/includedForm.json",
              "expressions": [
                {
                  "name": "newName",
                  "value": "false"
                }
              ]
            }
        """.trimIndent()

        fixture.openFileInEditor(includedForm.virtualFile)
        fixture.checkResult(includedFormResult)
        fixture.openFileInEditor(rootForm.virtualFile)
        fixture.checkResult(rootFormResult)
    }

    @Test
    fun `only one declaration found if expression is present in single included form but referenced from multiple root forms`() {
        fixture.createForm("rootForm1", "abc", """
            {
              "groups": "json://includes/forms/abc/groups.json",
              "expressions": "json://includes/forms/abc/expressions.json"
            }
        """.trimIndent())

        fixture.createForm("rootForm2", "abc", """
            {
              "groups": "json://includes/forms/abc/groups.json",
              "expressions": "json://includes/forms/abc/expressions.json"
            }
        """.trimIndent())

        fixture.createIncludedForm("expressions", "abc", """
            [
              {
                "name": "expr",
                "value": "false"
              }
            ]
        """.trimIndent())

        fixture.createIncludedFormAndConfigure("groups", "abc", """
            [
              {
                "visibleWhen": "expr<caret>"
              }
            ]
        """.trimIndent())

        val exprReference = getFormSymbolReferenceAtCaret()
        val result = exprReference.resolveReference()

        assertEquals(1, result.size)
    }

    @Test
    fun `exactly 2 declarations found if expression with the same name is present in multiple forms`() {
        fixture.createForm("rootForm1", "abc", """
            {
              "groups": "json://includes/forms/abc/groups.json",
              "expressions": "json://includes/forms/abc/expressions1.json"
            }
        """.trimIndent())

        fixture.createForm("rootForm2", "abc", """
            {
              "groups": "json://includes/forms/abc/groups.json",
              "expressions": "json://includes/forms/abc/expressions2.json"
            }
        """.trimIndent())

        fixture.createIncludedForm("expressions1", "abc", """
            [
              {
                "name": "expr1",
                "value": "false"
              }
            ]
        """.trimIndent())

        fixture.createIncludedForm("expressions2", "abc", """
            [
              {
                "name": "expr1",
                "value": "false"
              }
            ]
        """.trimIndent())

        fixture.createIncludedFormAndConfigure("groups", "abc", """
            [
              {
                "visibleWhen": "expr1<caret>"
              }
            ]
        """.trimIndent())

        val exprReference = getFormSymbolReferenceAtCaret()
        val result = exprReference.resolveReference()

        assertEquals(2, result.size)
    }

    @Test
    fun `exactly 2 declarations found - mixed scenario with 2 cases above`() {
        fixture.createForm("rootForm1", "abc", """
            {
              "groups": "json://includes/forms/abc/groups.json",
              "expressions": "json://includes/forms/abc/expressions1.json"
            }
        """.trimIndent())

        fixture.createForm("rootForm2", "abc", """
            {
              "groups": "json://includes/forms/abc/groups.json",
              "expressions": "json://includes/forms/abc/expressions2.json"
            }
        """.trimIndent())

        fixture.createForm("rootForm3", "abc", """
            {
              "groups": "json://includes/forms/abc/groups.json",
              "expressions": "json://includes/forms/abc/expressions2.json"
            }
        """.trimIndent())

        fixture.createIncludedForm("expressions1", "abc", """
            [
              {
                "name": "expr1",
                "value": "false"
              }
            ]
        """.trimIndent())

        fixture.createIncludedForm("expressions2", "abc", """
            [
              {
                "name": "expr1",
                "value": "false"
              }
            ]
        """.trimIndent())

        fixture.createIncludedFormAndConfigure("groups", "abc", """
            [
              {
                "visibleWhen": "expr1<caret>"
              }
            ]
        """.trimIndent())

        val exprReference = getFormSymbolReferenceAtCaret()
        val result = exprReference.resolveReference()

        assertEquals(2, result.size)
    }

}