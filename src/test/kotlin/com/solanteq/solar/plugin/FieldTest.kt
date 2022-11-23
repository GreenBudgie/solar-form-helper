package com.solanteq.solar.plugin

import com.intellij.psi.PsiNamedElement
import com.intellij.testFramework.RunsInEdt
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture
import org.jetbrains.uast.UField
import org.jetbrains.uast.toUElement
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@RunsInEdt
class FieldTest : FormTestBase() {

    override fun getTestDataSuffix() = "field"

    @Test
    fun `test field name reference from source request`() {
        fixture.configureByFiles(
            "basic/DataClass.kt",
            "basic/TestService.kt",
            "basic/TestServiceImpl.kt",
        )

        fixture.configureByFormText("""
            {
              "source": "test.testService.findData",
              "groups": [
                {
                  "rows": [
                    {
                      "fields": [
                        {
                          "name": "<caret>stringField"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent())

        assertReferenceFieldName(fixture, "stringField")
    }

    @Test
    fun `test field name completion from source request`() {
        fixture.configureByFiles(
            "basic/DataClass.kt",
            "basic/TestService.kt",
            "basic/TestServiceImpl.kt",
        )

        fixture.configureByFormText("""
            {
              "source": "test.testService.findData",
              "groupRows": [
                {
                  "groups": [
                    {
                      "rows": [
                        {
                          "fields": [
                            {
                              "name": "<caret>"
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent())

        assertCompletionsContainsExact(fixture,
            "stringField",
            "longField",
            "integerField"
        )
    }

    @Test
    fun `test field name reference from source request to nested property`() {
        fixture.configureByFiles(
            "basic/DataClassWithNestedProperty.kt",
            "basic/TestService.kt",
            "basic/TestServiceImpl.kt",
        )

        fixture.configureByFormText("""
            {
              "source": "test.testService.findDataWithNestedProperty",
              "groups": [
                {
                  "rows": [
                    {
                      "fields": [
                        {
                          "name": "<caret>nested.stringField"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent())

        assertReferenceFieldName(fixture, "nested")
    }

    @Test
    fun `test field name reference from source request to nested property field`() {
        fixture.configureByFiles(
            "basic/DataClass.kt",
            "basic/DataClassWithNestedProperty.kt",
            "basic/TestService.kt",
            "basic/TestServiceImpl.kt",
        )

        fixture.configureByFormText("""
            {
              "source": "test.testService.findDataWithNestedProperty",
              "groups": [
                {
                  "rows": [
                    {
                      "fields": [
                        {
                          "name": "nested.<caret>stringField"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent())

        assertReferenceFieldName(fixture, "stringField")
    }

    @Test
    fun `test field name completion from source request with nested property`() {
        fixture.configureByFiles(
            "basic/DataClass.kt",
            "basic/DataClassWithNestedProperty.kt",
            "basic/TestService.kt",
            "basic/TestServiceImpl.kt",
        )

        fixture.configureByFormText("""
            {
              "source": "test.testService.findData",
              "groups": [
                {
                  "rows": [
                    {
                      "fields": [
                        {
                          "name": "nested.<caret>"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent())

        assertCompletionsContainsExact(fixture,
            "stringField",
            "longField",
            "integerField"
        )
    }

    @Test
    fun `test field name reference from source request to superclass`() {
        fixture.configureByFiles(
            "basic/Cls.kt",
            "basic/SuperCls.java",
            "basic/TestService.kt",
            "basic/TestServiceImpl.kt",
        )

        fixture.configureByFormText("""
            {
              "source": "test.testService.findDataClassWithSuperClass",
              "groups": [
                {
                  "rows": [
                    {
                      "fields": [
                        {
                          "name": "<caret>superclassField"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent())

        assertReferenceFieldName(fixture, "superclassField")
    }

    @Test
    fun `test field name reference from source request with generic return type`() {
        fixture.configureByFiles(
            "basic/CustomService.kt",
            "basic/CustomServiceImpl.kt",
            "basic/GenericService.kt",
            "basic/GenericServiceImpl.kt",
            "basic/DataClass.kt"
        )

        fixture.configureByFormText("""
            {
              "source": "test.customService.findById",
              "groups": [
                {
                  "rows": [
                    {
                      "fields": [
                        {
                          "name": "<caret>stringField"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent())

        assertReferenceFieldName(fixture, "stringField")
    }

    private fun assertReferenceFieldName(fixture: JavaCodeInsightTestFixture, fieldName: String) {
        val propertyReference = fixture.file.findReferenceAt(fixture.caretOffset)

        Assertions.assertNotNull(propertyReference)

        val referencedField = propertyReference!!.resolve().toUElement() as? UField

        Assertions.assertNotNull(referencedField)

        val namedElement = referencedField!!.javaPsi as? PsiNamedElement

        Assertions.assertNotNull(namedElement)
        Assertions.assertEquals(fieldName, namedElement!!.name)
    }

    private fun assertCompletionsContainsExact(
        fixture: JavaCodeInsightTestFixture,
        vararg expectedCompletions: String
    ) {
        val actualCompletions = fixture.completeBasic().map { it.lookupString }

        Assertions.assertEquals(expectedCompletions.size, actualCompletions.size)
        Assertions.assertTrue(
            actualCompletions.containsAll(listOf(*expectedCompletions))
        )
    }

}