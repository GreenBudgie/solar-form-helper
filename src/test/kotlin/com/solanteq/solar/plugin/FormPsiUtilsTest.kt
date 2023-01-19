package com.solanteq.solar.plugin

import com.intellij.json.psi.*
import com.solanteq.solar.plugin.util.FormPsiUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FormPsiUtilsTest : FormTestBase() {

    @Test
    fun `test first parents of type on top level form`() {
        fixture.createFormAndConfigure("topLevelForm", """
            {
              "objectProperty": {
                "arrayProperty": [
                  "<caret>"
                ]
              }
            }
        """.trimIndent(), "test")

        val element = fixture.file.findElementAt(fixture.caretOffset)?.parent as JsonElement

        val firstParent = FormPsiUtils.firstParentsOfType(element, JsonObject::class).firstOrNull()

        assertNotNull(firstParent)

        val parentProperty = firstParent!!.parent as? JsonProperty

        assertNotNull(parentProperty)
        assertTrue(parentProperty!!.name == "objectProperty")
    }

    @Test
    fun `test first parents of type in included form to top level form`() {
        fixture.createForm("topLevelForm", """
            {
              "objectProperty": {
                "arrayProperty": "json://includes/forms/test/includedForm.json"
              }
            }
        """.trimIndent(), "test")

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
        fixture.createForm("topLevelForm", """
            {
              "objectProperty": {
                "arrayProperty": "json://includes/forms/test/includedForm.json"
              }
            }
        """.trimIndent(), "test")

        fixture.createForm("topLevelForm2", """
            {
              "objectProperty2": {
                "arrayProperty": "json://includes/forms/test/includedForm.json"
              }
            }
        """.trimIndent(), "test")

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
        fixture.createForm("topLevelForm", """
            {
              "objectProperty": "json://includes/forms/test/includedForm.json"
            }
        """.trimIndent(), "test")

        fixture.createForm("topLevelForm2", """
            {
              "objectProperty2": "json://includes/forms/test/includedForm.json"
            }
        """.trimIndent(), "test")

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
        fixture.createForm("topLevelForm", """
            {
              "objectProperty": "json://includes/forms/test/includedForm.json"
            }
        """.trimIndent(), "test")

        fixture.createForm("topLevelForm2", """
            {
              "arrayProperty": [
                "json://includes/forms/test/includedForm.json"
              ]
            }
        """.trimIndent(), "test")

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
        fixture.createForm("topLevelForm", """
            {
              "objectProperty": {
                "value": "json://includes/forms/test/includedForm.json"
              }
            }
        """.trimIndent(), "test")

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
        fixture.createForm("topLevelForm", """
            {
              "objectProperty": "json://includes/forms/test/includedForm.json"
            }
        """.trimIndent(), "test")

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
        fixture.createForm("topLevelForm", """
            {
              "arrayProperty": [
                "json://includes/forms/test/includedForm.json"
              ]
            }
        """.trimIndent(), "test")

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
        fixture.createForm("topLevelForm", """
            {
              "arrayProperty": "json://includes/forms/test/includedForm.json"
            }
        """.trimIndent(), "test")

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
        fixture.createForm("topLevelForm", """
            {
              "arrayProperty": [
                "json-flat://includes/forms/test/includedForm.json"
              ]
            }
        """.trimIndent(), "test")

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
        fixture.createForm("topLevelForm", """
            {
              "arrayProperty": [
                "json-flat://includes/forms/test/includedForm1.json"
              ]
            }
        """.trimIndent(), "test")

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
    fun `test isInObjectInArrayWithKey for top-level form positive scenario property`() {
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
        fixture.createForm("topLevelForm", """
            {
              "fields": "json://includes/forms/test/includedForm.json"
            }
        """.trimIndent(), "test")

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
        fixture.createForm("topLevelForm", """
            {
              "fields": [
                "json-flat://includes/forms/test/includedForm.json"
              ]
            }
        """.trimIndent(), "test")

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
    fun `test isInObjectInArrayWithKey for top-level form negative scenario no object in array`() {
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
    fun `test isInObjectInArrayWithKey for top-level form negative scenario inner object property`() {
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
        fixture.createForm("topLevelForm", """
            {
              "fields": [
                "json://includes/forms/test/includedForm.json"
              ]
            }
        """.trimIndent(), "test")

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
        fixture.createForm("topLevelForm1", """
            {
              "arrayProperty": [
                "json-flat://includes/forms/test/includedForm.json"
              ]
            }
        """.trimIndent(), "test")

        fixture.createForm("topLevelForm2", """
            {
              "arrayProperty": "json://includes/forms/test/includedForm.json"
            }
        """.trimIndent(), "test")

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
        val arrayInTopLevelForm1 = firstParents.filter {
            it.containingFile.name == "topLevelForm1.json"
        }

        assertEquals(2, firstParents.size)
        assertEquals(1, arrayInIncludedFormParent.size)
        assertEquals(1, arrayInTopLevelForm1.size)
    }

    @Test
    fun `test different parents for flat and non-flat json include declarations`() {
        fixture.createForm("topLevelForm1", """
            {
              "arrayProperty": [
                "json-flat://includes/forms/test/includedForm.json"
              ]
            }
        """.trimIndent(), "test")

        fixture.createForm("topLevelForm2", """
            {
              "arrayProperty": "json://includes/forms/test/includedForm.json"
            }
        """.trimIndent(), "test")

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
        val arrayInTopLevelForm1 = firstParents.filter {
            it.containingFile.name == "topLevelForm1.json"
        }

        assertTrue(firstParents.all { it is JsonArray })
        assertEquals(2, firstParents.size)
        assertEquals(1, arrayInIncludedFormParent.size)
        assertEquals(1, arrayInTopLevelForm1.size)
    }

    @Test
    fun `test different parents for flat and non-flat json include declarations 2x deep`() {
        fixture.createForm("topLevelForm1", """
            {
              "arrayProperty": [
                "json-flat://includes/forms/test/includedForm1.json"
              ]
            }
        """.trimIndent(), "test")

        fixture.createForm("topLevelForm2", """
            {
              "arrayProperty": "json://includes/forms/test/includedForm1.json"
            }
        """.trimIndent(), "test")

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
        val arrayInTopLevelForm1 = firstParents.filter {
            it.containingFile.name == "topLevelForm1.json"
        }

        assertTrue(firstParents.all { it is JsonArray })
        assertEquals(2, firstParents.size)
        assertEquals(1, arrayInIncludedFormParent.size)
        assertEquals(1, arrayInTopLevelForm1.size)
    }

    @Test
    fun `test get property value as json include declaration`() {
        fixture.createFormAndConfigure("topLevelForm", """
            {
              "<caret>property": "json://includes/forms/test/includedForm.json"
            }
        """.trimIndent(), "test")

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

}