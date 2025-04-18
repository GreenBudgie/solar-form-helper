package com.solanteq.solar.plugin

import com.intellij.json.psi.JsonObject
import com.solanteq.solar.plugin.base.*
import com.solanteq.solar.plugin.element.FormGroup
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.element.expression.ExpressionType
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
    fun `test expression name reference - basic case for all possible literals`(propertyName: String) = with(fixture) {
        createFormAndConfigure(
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
    fun `test expression name reference - expression in included form`() = with(fixture) {
        createIncludedForm(
            "includedForm", "abc", """
            [
              {
                "name": "never",
                "value": "false"
              }
            ]
        """.trimIndent()
        )

        createFormAndConfigure(
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
    fun `test expression name reference - expression in root form, but referenced from included form`() = with(fixture) {
        createIncludedFormAndConfigure(
            "includedForm", "abc", """
            [
              {
                "visibleWhen": "<caret>never"
              }
            ]
        """.trimIndent()
        )

        createForm(
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
    fun `test expression name completion`() = with(fixture) {
        createFormAndConfigure(
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
    fun `test expression name rename from reference - in the same root form`() = with(fixture) {
        createFormAndConfigure(
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

        checkResult(expectedRenameResult)
    }

    @Test
    fun `test expression name rename from declaration - in the same root form`() = with(fixture) {
        createFormAndConfigure(
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

        checkResult(expectedRenameResult)
    }

    @Test
    fun `test expression name rename from reference - in different included forms`() = with(fixture) {
        val formWithReference = createIncludedFormAndConfigure(
            "formWithReference", "abc", """
            [
              {
                "visibleWhen": "<caret>expr1"
              }
            ]
        """.trimIndent()
        )

        val formWithDeclaration = createIncludedForm(
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

        val rootForm = createForm(
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

        openFileInEditor(formWithReference.virtualFile)
        checkResult(formWithReferenceResult)
        openFileInEditor(formWithDeclaration.virtualFile)
        checkResult(formWithDeclarationResult)
        openFileInEditor(rootForm.virtualFile)
        checkResult(rootFormResult)
    }

    @Test
    fun `test expression name rename from reference - multiple declarations`() = with(fixture) {
        val rootForm1 = createForm(
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

        val rootForm2 = createForm(
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

        val includedForm = createIncludedFormAndConfigure(
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

        openFileInEditor(rootForm1.virtualFile)
        checkResult(rootForm1Result)
        openFileInEditor(rootForm2.virtualFile)
        checkResult(rootForm2Result)
        openFileInEditor(includedForm.virtualFile)
        checkResult(includedFormResult)
    }

    @Test
    fun `test expression name rename from declaration - declaration in root form, reference in included`() = with(fixture) {
        val includedForm = createIncludedForm(
            "includedForm", "abc", """
            [
              {
                "visibleWhen": "expr"
              }
            ]
        """.trimIndent()
        )

        val rootForm = createFormAndConfigure(
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

        openFileInEditor(includedForm.virtualFile)
        checkResult(includedFormResult)
        openFileInEditor(rootForm.virtualFile)
        checkResult(rootFormResult)
    }

    @Test
    fun `only one declaration found if expression is present in single included form but referenced from multiple root forms`() = with(fixture) {
        createForm(
            "rootForm1", "abc", """
            {
              "groups": "json://includes/forms/abc/groups.json",
              "expressions": "json://includes/forms/abc/expressions.json"
            }
        """.trimIndent()
        )

        createForm(
            "rootForm2", "abc", """
            {
              "groups": "json://includes/forms/abc/groups.json",
              "expressions": "json://includes/forms/abc/expressions.json"
            }
        """.trimIndent()
        )

        createIncludedForm(
            "expressions", "abc", """
            [
              {
                "name": "expr",
                "value": "false"
              }
            ]
        """.trimIndent()
        )

        createIncludedFormAndConfigure(
            "groups", "abc", """
            [
              {
                "visibleWhen": "expr<caret>"
              }
            ]
        """.trimIndent()
        )

        val exprReference = getFormSymbolReferenceAtCaret()
        val result = exprReference.resolveReference()

        assertEquals(1, result.size)
    }

    @Test
    fun `exactly 2 declarations found if expression with the same name is present in multiple forms`() = with(fixture) {
        createForm(
            "rootForm1", "abc", """
            {
              "groups": "json://includes/forms/abc/groups.json",
              "expressions": "json://includes/forms/abc/expressions1.json"
            }
        """.trimIndent()
        )

        createForm(
            "rootForm2", "abc", """
            {
              "groups": "json://includes/forms/abc/groups.json",
              "expressions": "json://includes/forms/abc/expressions2.json"
            }
        """.trimIndent()
        )

        createIncludedForm(
            "expressions1", "abc", """
            [
              {
                "name": "expr1",
                "value": "false"
              }
            ]
        """.trimIndent()
        )

        createIncludedForm(
            "expressions2", "abc", """
            [
              {
                "name": "expr1",
                "value": "false"
              }
            ]
        """.trimIndent()
        )

        createIncludedFormAndConfigure(
            "groups", "abc", """
            [
              {
                "visibleWhen": "expr1<caret>"
              }
            ]
        """.trimIndent()
        )

        val exprReference = getFormSymbolReferenceAtCaret()
        val result = exprReference.resolveReference()

        assertEquals(2, result.size)
    }

    @Test
    fun `exactly 2 declarations found - mixed scenario with 2 cases above`() = with(fixture) {
        createForm(
            "rootForm1", "abc", """
            {
              "groups": "json://includes/forms/abc/groups.json",
              "expressions": "json://includes/forms/abc/expressions1.json"
            }
        """.trimIndent()
        )

        createForm(
            "rootForm2", "abc", """
            {
              "groups": "json://includes/forms/abc/groups.json",
              "expressions": "json://includes/forms/abc/expressions2.json"
            }
        """.trimIndent()
        )

        createForm(
            "rootForm3", "abc", """
            {
              "groups": "json://includes/forms/abc/groups.json",
              "expressions": "json://includes/forms/abc/expressions2.json"
            }
        """.trimIndent()
        )

        createIncludedForm(
            "expressions1", "abc", """
            [
              {
                "name": "expr1",
                "value": "false"
              }
            ]
        """.trimIndent()
        )

        createIncludedForm(
            "expressions2", "abc", """
            [
              {
                "name": "expr1",
                "value": "false"
              }
            ]
        """.trimIndent()
        )

        createIncludedFormAndConfigure(
            "groups", "abc", """
            [
              {
                "visibleWhen": "expr1<caret>"
              }
            ]
        """.trimIndent()
        )

        val exprReference = getFormSymbolReferenceAtCaret()
        val result = exprReference.resolveReference()

        assertEquals(2, result.size)
    }

    @Test
    fun `ExpressionAware getExpressions - returns no expressions if expression is not present in object`() = with(fixture) {
        val formFile = createForm(
            "form", "abc", """
            {
              "expressions": [
                {
                  "name": "expression",
                  "value": "false"
                }
              ]
            }
        """.trimIndent()
        )
        val formElement = FormRootFile.createFrom(formFile)!!

        val result = formElement.getExpressions(ExpressionType.EDITABLE_WHEN)

        assertEquals(0, result.size)
    }

    @Test
    fun `ExpressionAware getExpressions - 1 expression returned`() = with(fixture) {
        val formFile = createForm(
            "form", "abc", """
            {
              "editableWhen": "expression"
              "expressions": [
                {
                  "name": "expression",
                  "value": "false"
                }
              ]
            }
        """.trimIndent()
        )
        val formElement = FormRootFile.createFrom(formFile)!!

        val result = formElement.getExpressions(ExpressionType.EDITABLE_WHEN)

        assertEquals(1, result.size)
        assertEquals("expression", result[0].name)
    }

    @Test
    fun `ExpressionAware getExpressions - multiple expressions returned`() = with(fixture) {
        createForm(
            "rootForm1", "abc", """
            {
              "groups": "json://includes/forms/abc/groups.json",
              "expressions": "json://includes/forms/abc/expressions1.json"
            }
        """.trimIndent()
        )

        createForm(
            "rootForm2", "abc", """
            {
              "groups": "json://includes/forms/abc/groups.json",
              "expressions": "json://includes/forms/abc/expressions2.json"
            }
        """.trimIndent()
        )

        createIncludedForm(
            "expressions1", "abc", """
            [
              {
                "name": "expr1",
                "value": "false"
              }
            ]
        """.trimIndent()
        )

        createIncludedForm(
            "expressions2", "abc", """
            [
              {
                "name": "expr1",
                "value": "false"
              }
            ]
        """.trimIndent()
        )

        createIncludedFormAndConfigure(
            "groups", "abc", """
            [
              {<caret>
                "visibleWhen": "expr1"
              }
            ]
        """.trimIndent()
        )

        val objectAtCaret = file.findElementAt(caretOffset)!!.parent as JsonObject
        val formElement = FormGroup.createFrom(objectAtCaret)!!

        val result = formElement.getExpressions(ExpressionType.VISIBLE_WHEN)

        assertEquals(2, result.size)
        assertEquals("expr1", result[0].name)
        assertEquals("expr1", result[1].name)
    }

    @Test
    fun `ExpressionAware isNeverExpression - false if no expression`() = with(fixture) {
        val formFile = createForm(
            "form", "abc", """
            {
              "expressions": [
                {
                  "name": "never",
                  "value": "false"
                }
              ]
            }
        """.trimIndent()
        )
        val formElement = FormRootFile.createFrom(formFile)!!

        val result = formElement.isNeverExpression(ExpressionType.EDITABLE_WHEN)

        assertEquals(false, result)
    }

    @Test
    fun `ExpressionAware isNeverExpression - true even if editableWhen exists but expression does not`() = with(fixture) {
        val formFile = createForm(
            "form", "abc", """
            {
              "editableWhen": "never"
            }
        """.trimIndent()
        )
        val formElement = FormRootFile.createFrom(formFile)!!

        val result = formElement.isNeverExpression(ExpressionType.EDITABLE_WHEN)

        assertEquals(true, result)
    }

}