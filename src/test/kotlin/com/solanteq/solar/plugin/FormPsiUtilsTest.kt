package com.solanteq.solar.plugin

import com.intellij.json.psi.*
import com.solanteq.solar.plugin.base.*
import com.solanteq.solar.plugin.util.FormPsiUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FormPsiUtilsTest : LightPluginTestBase() {

    @Test
    fun `test first parents of type on root form`() {
        fixture.createFormAndConfigure(
            "rootForm", "test", """
                {
                  "objectProperty": {
                    "arrayProperty": [
                      "<caret>"
                    ]
                  }
                }
            """.trimIndent()
        )

        val element = fixture.file.findElementAt(fixture.caretOffset)?.parent as JsonElement

        val firstParent = FormPsiUtils.firstParentsOfType(element, JsonObject::class).firstOrNull()

        assertNotNull(firstParent)

        val parentProperty = firstParent!!.parent as? JsonProperty

        assertNotNull(parentProperty)
        assertTrue(parentProperty!!.name == "objectProperty")
    }

    @Test
    fun `test first parents of type in included form to root form`() {
        fixture.createForm(
            "rootForm", "test", """
                {
                  "objectProperty": {
                    "arrayProperty": "json://includes/forms/test/includedForm.json"
                  }
                }
            """.trimIndent()
        )

        fixture.createIncludedFormAndConfigure("includedForm", "test", """
            [
              "anotherArray": "json://includes/forms/test/includedForm2.json"
            ]
        """.trimIndent())

        fixture.createIncludedFormAndConfigure("includedForm2", "test", """
            [
              "anotherArray2": [
                "<caret>"
              ]
            ]
        """.trimIndent())

        val element = fixture.file.findElementAt(fixture.caretOffset)?.parent as JsonElement

        val firstParent = FormPsiUtils.firstParentsOfType(element, JsonObject::class).firstOrNull()

        assertNotNull(firstParent)

        val parentProperty = firstParent!!.parent as? JsonProperty

        assertNotNull(parentProperty)
        assertTrue(parentProperty!!.name == "objectProperty")
    }

    @Test
    fun `test find multiple first parents`() {
        fixture.createForm(
            "rootForm", "test", """
                {
                  "objectProperty": {
                    "arrayProperty": "json://includes/forms/test/includedForm.json"
                  }
                }
            """.trimIndent()
        )

        fixture.createForm(
            "rootForm2", "test", """
                {
                  "objectProperty2": {
                    "arrayProperty": "json://includes/forms/test/includedForm.json"
                  }
                }
            """.trimIndent()
        )

        fixture.createIncludedFormAndConfigure("includedForm", "test", """
            [
              "anotherArray": "<caret>"
            ]
        """.trimIndent())

        val element = fixture.file.findElementAt(fixture.caretOffset)?.parent as JsonElement

        val parents = FormPsiUtils.firstParentsOfType(element, JsonObject::class)
        assertEquals(2, parents.size)

        val parentProperties = parents.mapNotNull { it.parent as? JsonProperty }
        assertEquals(2, parentProperties.size)

        val parentPropertyNames = parentProperties.map { it.name }
        assertTrue(parentPropertyNames.containsAll(listOf("objectProperty", "objectProperty2")))
    }

    @Test
    fun `test multiple parents`() {
        fixture.createForm(
            "rootForm", "test", """
                {
                  "objectProperty": "json://includes/forms/test/includedForm.json"
                }
            """.trimIndent()
        )

        fixture.createForm(
            "rootForm2", "test", """
                {
                  "objectProperty2": "json://includes/forms/test/includedForm.json"
                }
            """.trimIndent()
        )

        fixture.createIncludedFormAndConfigure("includedForm", "test", """
            <caret>{
              "key": "value"
            }
        """.trimIndent())

        val element = fixture.file.findElementAt(fixture.caretOffset)?.parent as JsonElement

        val parents = FormPsiUtils.parents(element)
        assertEquals(2, parents.size)

        val parentProperties = parents.mapNotNull { it as? JsonProperty }
        assertEquals(2, parentProperties.size)

        val parentPropertyNames = parentProperties.map { it.name }
        assertTrue(parentPropertyNames.containsAll(listOf("objectProperty", "objectProperty2")))
    }

    @Test
    fun `test isPropertyValueWithKey for included form`() {
        fixture.createForm(
            "rootForm", "test", """
                {
                  "objectProperty": "json://includes/forms/test/includedForm.json"
                }
            """.trimIndent()
        )

        fixture.createForm(
            "rootForm2", "test", """
                {
                  "arrayProperty": [
                    "json://includes/forms/test/includedForm.json"
                  ]
                }
            """.trimIndent()
        )

        fixture.createIncludedFormAndConfigure("includedForm", "test", """
            <caret>{
              "key": "value"
            }
        """.trimIndent())

        val element = fixture.file.findElementAt(fixture.caretOffset)?.parent as JsonElement

        assertTrue(FormPsiUtils.isPropertyValueWithKey(element, "objectProperty"))
    }

    @Test
    fun `test isInObjectWithKey for included form`() {
        fixture.createForm(
            "rootForm", "test", """
                {
                  "objectProperty": {
                    "value": "json://includes/forms/test/includedForm.json"
                  }
                }
            """.trimIndent()
        )

        fixture.createIncludedFormAndConfigure("includedForm", "test", """
            [
              "key": "<caret>"
            ]
        """.trimIndent())

        val element = fixture.file.findElementAt(fixture.caretOffset)?.parent as JsonElement

        assertTrue(FormPsiUtils.isInObjectWithKey(element, "objectProperty"))
    }

    @Test
    fun `test isInObjectWithKey for included form as direct object value`() {
        fixture.createForm(
            "rootForm", "test", """
                {
                  "objectProperty": "json://includes/forms/test/includedForm.json"
                }
            """.trimIndent()
        )

        fixture.createIncludedFormAndConfigure("includedForm", "test", """
            {
              "key": "<caret>"
            }
        """.trimIndent())

        val element = fixture.file.findElementAt(fixture.caretOffset)?.parent as JsonElement

        assertTrue(FormPsiUtils.isInObjectWithKey(element, "objectProperty"))
    }

    @Test
    fun `test isInArrayWithKey for included form`() {
        fixture.createForm(
            "rootForm", "test", """
                {
                  "arrayProperty": [
                    "json://includes/forms/test/includedForm.json"
                  ]
                }
            """.trimIndent()
        )

        fixture.createIncludedFormAndConfigure("includedForm", "test", """
            {
              "key": "<caret>"
            }
        """.trimIndent())

        val element = fixture.file.findElementAt(fixture.caretOffset)?.parent as JsonElement

        assertTrue(FormPsiUtils.isInArrayWithKey(element, "arrayProperty"))
    }

    @Test
    fun `test isInArrayWithKey for included form as direct array value`() {
        fixture.createForm(
            "rootForm", "test", """
                {
                  "arrayProperty": "json://includes/forms/test/includedForm.json"
                }
            """.trimIndent()
        )

        fixture.createIncludedFormAndConfigure("includedForm", "test", """
            [
              {
                "key": "<caret>"
              }
            ]
        """.trimIndent())

        val element = fixture.file.findElementAt(fixture.caretOffset)?.parent as JsonElement

        assertTrue(FormPsiUtils.isInArrayWithKey(element, "arrayProperty"))
    }

    @Test
    fun `test isInArrayWithKey for included form with json-flat declaration`() {
        fixture.createForm(
            "rootForm", "test", """
                {
                  "arrayProperty": [
                    "json-flat://includes/forms/test/includedForm.json"
                  ]
                }
            """.trimIndent()
        )

        fixture.createIncludedFormAndConfigure("includedForm", "test", """
            [
              {
                "key": "<caret>"
              }
            ]
        """.trimIndent())

        val element = fixture.file.findElementAt(fixture.caretOffset)?.parent as JsonElement

        assertTrue(FormPsiUtils.isInArrayWithKey(element, "arrayProperty"))
    }

    @Test
    fun `test isInArrayWithKey for included form with json-flat declaration 2x deep`() {
        fixture.createForm(
            "rootForm", "test", """
                {
                  "arrayProperty": [
                    "json-flat://includes/forms/test/includedForm1.json"
                  ]
                }
            """.trimIndent()
        )

        fixture.createIncludedFormAndConfigure("includedForm1", "test", """
            [
              "json-flat://includes/forms/test/includedForm2.json"
            ]
        """.trimIndent())

        fixture.createIncludedFormAndConfigure("includedForm2", "test", """
            [
              {
                "key": "<caret>"
              }
            ]
        """.trimIndent())

        val element = fixture.file.findElementAt(fixture.caretOffset)?.parent as JsonElement

        assertTrue(FormPsiUtils.isInArrayWithKey(element, "arrayProperty"))
    }

    @Test
    fun `test isInObjectInArrayWithKey for root form positive scenario property`() {
        fixture.configureByFormText("""
            {
              "fields": [
                {
                  "property": "value<caret>"
                }
              ]
            }
        """.trimIndent())

        val element = getJsonStringLiteralAtCaret()
        val result = FormPsiUtils.isInObjectInArrayWithKey(element, "fields")
        assertTrue(result)
    }

    @Test
    fun `test isInObjectInArrayWithKey for included form positive scenario property`() {
        fixture.createForm(
            "rootForm", "test", """
                {
                  "fields": "json://includes/forms/test/includedForm.json"
                }
            """.trimIndent()
        )

        fixture.createIncludedFormAndConfigure("includedForm", "test", """
            [
              {
                "property": "value<caret>"
              }
            ]
        """.trimIndent())

        val element = getJsonStringLiteralAtCaret()
        val result = FormPsiUtils.isInObjectInArrayWithKey(element, "fields")
        assertTrue(result)
    }

    @Test
    fun `test isInObjectInArrayWithKey for flat included form positive scenario property`() {
        fixture.createForm(
            "rootForm", "test", """
                {
                  "fields": [
                    "json-flat://includes/forms/test/includedForm.json"
                  ]
                }
            """.trimIndent()
        )

        fixture.createIncludedFormAndConfigure("includedForm", "test", """
            [
              {
                "property": "value<caret>"
              }
            ]
        """.trimIndent())

        val element = getJsonStringLiteralAtCaret()
        val result = FormPsiUtils.isInObjectInArrayWithKey(element, "fields")
        assertTrue(result)
    }

    @Test
    fun `test isInObjectInArrayWithKey for root form negative scenario no object in array`() {
        fixture.configureByFormText("""
            {
              "fields": [
                "someString<caret>"
              ]
            }
        """.trimIndent())

        val element = getJsonStringLiteralAtCaret()
        val result = FormPsiUtils.isInObjectInArrayWithKey(element, "fields")
        assertFalse(result)
    }

    @Test
    fun `test isInObjectInArrayWithKey for root form negative scenario inner object property`() {
        fixture.configureByFormText("""
            {
              "fields": [
                {
                  "property": "value",
                  "innerObject": {
                    "mustNot": "pass<caret>"
                  }
                }
              ]
            }
        """.trimIndent())

        val element = getJsonStringLiteralAtCaret()
        val result = FormPsiUtils.isInObjectInArrayWithKey(element, "fields")
        assertFalse(result)
    }

    @Test
    fun `test isInObjectInArrayWithKey for included form negative scenario inner object property`() {
        fixture.createForm(
            "rootForm", "test", """
                {
                  "fields": [
                    "json://includes/forms/test/includedForm.json"
                  ]
                }
            """.trimIndent()
        )

        fixture.createIncludedFormAndConfigure("includedForm", "test", """
            {
              "property": "value",
              "innerObject": {
                "mustNot": "pass<caret>"
              }
            }
        """.trimIndent())

        val element = getJsonStringLiteralAtCaret()
        val result = FormPsiUtils.isInObjectInArrayWithKey(element, "fields")
        assertFalse(result)
    }

    @Test
    fun `test different first parents for flat and non-flat json include declarations`() {
        fixture.createForm(
            "rootForm1", "test", """
                {
                  "arrayProperty": [
                    "json-flat://includes/forms/test/includedForm.json"
                  ]
                }
            """.trimIndent()
        )

        fixture.createForm(
            "rootForm2", "test", """
                {
                  "arrayProperty": "json://includes/forms/test/includedForm.json"
                }
            """.trimIndent()
        )

        fixture.createIncludedFormAndConfigure("includedForm", "test", """
            [
              {
                "key": "<caret>"
              }
            ]
        """.trimIndent())

        val element = fixture.file.findElementAt(fixture.caretOffset)?.parent as JsonElement
        val firstParents = FormPsiUtils.firstParentsOfType(element, JsonArray::class)
        val arrayInIncludedFormParent = firstParents.filter {
            it.containingFile.name == "includedForm.json"
        }
        val arrayInRootForm1 = firstParents.filter {
            it.containingFile.name == "rootForm1.json"
        }

        assertEquals(2, firstParents.size)
        assertEquals(1, arrayInIncludedFormParent.size)
        assertEquals(1, arrayInRootForm1.size)
    }

    @Test
    fun `test different parents for flat and non-flat json include declarations`() {
        fixture.createForm(
            "rootForm1", "test", """
                {
                  "arrayProperty": [
                    "json-flat://includes/forms/test/includedForm.json"
                  ]
                }
            """.trimIndent()
        )

        fixture.createForm(
            "rootForm2", "test", """
                {
                  "arrayProperty": "json://includes/forms/test/includedForm.json"
                }
            """.trimIndent()
        )

        fixture.createIncludedFormAndConfigure("includedForm", "test", """
            [
              "<caret>"
            ]
        """.trimIndent())

        val element = fixture.file.findElementAt(fixture.caretOffset)?.parent as JsonElement
        val firstParents = FormPsiUtils.parents(element)
        val arrayInIncludedFormParent = firstParents.filter {
            it.containingFile.name == "includedForm.json"
        }
        val arrayInRootForm1 = firstParents.filter {
            it.containingFile.name == "rootForm1.json"
        }

        assertTrue(firstParents.all { it is JsonArray })
        assertEquals(2, firstParents.size)
        assertEquals(1, arrayInIncludedFormParent.size)
        assertEquals(1, arrayInRootForm1.size)
    }

    @Test
    fun `test different parents for flat and non-flat json include declarations 2x deep`() {
        fixture.createForm(
            "rootForm1", "test", """
                {
                  "arrayProperty": [
                    "json-flat://includes/forms/test/includedForm1.json"
                  ]
                }
            """.trimIndent()
        )

        fixture.createForm(
            "rootForm2", "test", """
                {
                  "arrayProperty": "json://includes/forms/test/includedForm1.json"
                }
            """.trimIndent()
        )

        fixture.createIncludedFormAndConfigure("includedForm1", "test", """
            [
              "json-flat://includes/forms/test/includedForm2.json"
            ]
        """.trimIndent())

        fixture.createIncludedFormAndConfigure("includedForm2", "test", """
            [
              "<caret>"
            ]
        """.trimIndent())

        val element = fixture.file.findElementAt(fixture.caretOffset)?.parent as JsonElement
        val firstParents = FormPsiUtils.parents(element)
        val arrayInIncludedFormParent = firstParents.filter {
            it.containingFile.name == "includedForm1.json"
        }
        val arrayInRootForm1 = firstParents.filter {
            it.containingFile.name == "rootForm1.json"
        }

        assertTrue(firstParents.all { it is JsonArray })
        assertEquals(2, firstParents.size)
        assertEquals(1, arrayInIncludedFormParent.size)
        assertEquals(1, arrayInRootForm1.size)
    }

    @Test
    fun `test get property value as json include declaration`() {
        fixture.createFormAndConfigure(
            "rootForm", "test", """
                {
                  "<caret>property": "json://includes/forms/test/includedForm.json"
                }
            """.trimIndent()
        )

        fixture.createIncludedForm("includedForm", "test", """
            [
              "element"
            ]
        """.trimIndent())

        val element = fixture.file.findElementAt(fixture.caretOffset)?.parent as JsonStringLiteral
        val property = element.parent as JsonProperty

        val value = FormPsiUtils.getPropertyValue(property)

        assertNotNull(value)
        assertTrue(value is JsonArray)
    }

    @Test
    fun `test find all parents if both json and json-flat declarations present`() {
        fixture.createForm(
            "rootForm1", "test", """
                {
                  "property1": "json://includes/forms/test/includedForm.json"
                }
            """.trimIndent()
        )

        fixture.createForm(
            "rootForm2", "test", """
                {
                  "property2": [
                    "json-flat://includes/forms/test/includedForm.json"
                  ]
                }
            """.trimIndent()
        )

        fixture.createIncludedFormAndConfigure(
            "includedForm", "test", """
                [
                  "<caret>abc"
                ]
            """.trimIndent()
        )

        val elementAtCaret = getJsonStringLiteralAtCaret()
        val parents = FormPsiUtils.parents(elementAtCaret)

        assertEquals(2, parents.size)
        assertTrue(parents[0] is JsonArray)
        assertTrue(parents[1] is JsonArray)
    }

}