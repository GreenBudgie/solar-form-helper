package com.solanteq.solar.plugin

import com.solanteq.solar.plugin.base.*
import org.junit.jupiter.api.Test

class FieldTest : JavaPluginTestBase() {

    override fun getTestDataSuffix() = "field"

    @Test
    fun `test field name reference from source request`() = with(fixture) {
        configureByFiles(
            "DataClass.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
        )

        configureByFormText("""
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

        assertReferencedElementNameEquals("stringField")
    }

    @Test
    fun `test field name completion from source request`() = with(fixture) {
        configureByFiles(
            "DataClass.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
        )

        configureByFormText("""
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

        assertCompletionsContainsExact(
            "stringField",
            "longField",
            "integerField"
        )
    }

    @Test
    fun `test field name reference from source request to nested property`() = with(fixture) {
        configureByFiles(
            "DataClassWithNestedProperty.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
        )

        configureByFormText("""
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

        assertReferencedElementNameEquals("nested")
    }

    @Test
    fun `test field name reference from source request to nested property field`() = with(fixture) {
        configureByFiles(
            "DataClass.kt",
            "DataClassWithNestedProperty.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
        )

        configureByFormText("""
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

        assertReferencedElementNameEquals("stringField")
    }

    @Test
    fun `test field name completion from source request with nested property`() = with(fixture) {
        configureByFiles(
            "DataClass.kt",
            "DataClassWithNestedProperty.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
        )

        configureByFormText("""
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

        assertCompletionsContainsExact(
            "stringField",
            "longField",
            "integerField"
        )
    }

    @Test
    fun `test field name reference from source request to superclass`() = with(fixture) {
        configureByFiles(
            "Cls.kt",
            "SuperCls.java",
            "TestService.kt",
            "TestServiceImpl.kt",
        )

        configureByFormText("""
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

        assertReferencedElementNameEquals("superclassField")
    }

    @Test
    fun `test field name reference from source request with generic return type`() = with(fixture) {
        configureByFiles(
            "CustomService.kt",
            "CustomServiceImpl.kt",
            "GenericService.kt",
            "GenericServiceImpl.kt",
            "DataClass.kt"
        )

        configureByFormText("""
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

        assertReferencedElementNameEquals("stringField")
    }

    @Test
    fun `test field completion from multiple inline configurations`() = with(fixture) {
        configureByFiles(
            "Cls.kt",
            "SuperCls.java",
            "DataClass.kt",
            "TestService.kt",
            "TestServiceImpl.kt"
        )

        createForm(
            "dataClass", "abc1", """
                {
                  "groups": [
                    {
                      "inline": {
                        "form": "test.testForm",
                        "request": {
                          "name": "test.testService.findDataList"
                        }
                      }
                    }
                  ]
                }
            """.trimIndent()
        )

        createForm(
            "cls", "abc2", """
                {
                  "groups": [
                    {
                      "inline": {
                        "form": "test.testForm",
                        "request": "test.testService.findDataClsList"
                      }
                    }
                  ]
                }
            """.trimIndent()
        )

        createFormAndConfigure(
            "testForm", "test", """
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
            """.trimIndent()
        )

        assertCompletionsContainsExact(
            "a", //From Cls
            "superclassField", //From SuperCls
            "stringField", //From DataClass
            "longField", //From DataClass
            "integerField" //From DataClass
        )
    }

    @Test
    fun `test field completion from generified list`() = with(fixture) {
        configureByFiles(
            "CustomService.kt",
            "CustomServiceImpl.kt",
            "GenericService.kt",
            "GenericServiceImpl.kt",
            "DataClass.kt"
        )

        createForm(
            "dataClass", "abc", """
                {
                  "groups": [
                    {
                      "inline": {
                        "form": "test.testForm",
                        "request": "test.customService.findDataList"
                      }
                    }
                  ]
                }
            """.trimIndent()
        )

        createFormAndConfigure(
            "testForm", "test", """
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
            """.trimIndent()
        )

        assertCompletionsContainsExact(
            "stringField",
            "longField",
            "integerField"
        )
    }

    @Test
    fun `test field reference from generified list`() = with(fixture) {
        configureByFiles(
            "CustomService.kt",
            "CustomServiceImpl.kt",
            "GenericService.kt",
            "GenericServiceImpl.kt",
            "DataClass.kt"
        )

        createForm(
            "dataClass", "abc", """
                {
                  "groups": [
                    {
                      "inline": {
                        "form": "test.testForm",
                        "request": "test.customService.findDataList"
                      }
                    }
                  ]
                }
            """.trimIndent()
        )

        createFormAndConfigure(
            "testForm", "test", """
                {
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
            """.trimIndent()
        )

        assertReferencedElementNameEquals("stringField")
    }

    @Test
    fun `test field reference from inline configuration`() = with(fixture) {
        configureByFiles(
            "DataClass.kt",
            "TestService.kt",
            "TestServiceImpl.kt"
        )

        createForm(
            "dataClass", "abc", """
                {
                  "groups": [
                    {
                      "inline": {
                        "form": "test.testForm",
                        "request": {
                          "name": "test.testService.findDataList"
                        }
                      }
                    }
                  ]
                }
            """.trimIndent()
        )

        createFormAndConfigure(
            "testForm", "test", """
                {
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
            """.trimIndent()
        )

        assertReferencedElementNameEquals("stringField")
    }

    @Test
    fun `test field name reference in included form`() = with(fixture) {
        configureByFiles(
            "DataClass.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
        )

        createForm(
            "rootForm", "test", """
                {
                  "source": "test.testService.findData",
                  "groups": [
                    {
                      "rows": [
                        {
                          "fields": "json://includes/forms/test2/includedForm.json"
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()
        )

        createIncludedFormAndConfigure("includedForm", "test2", """
            [
              {
                "name": "<caret>stringField"
              }
            ]
        """.trimIndent())

        assertReferencedElementNameEquals("stringField")
    }

    @Test
    fun `test field name completion in included form for multiple declarations`() = with(fixture) {
        configureByFiles(
            "DataClass.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
            "Cls.kt",
            "SuperCls.java"
        )

        createForm(
            "dataClassForm", "test", """
                {
                  "source": "test.testService.findData",
                  "groups": [
                    {
                      "rows": [
                        {
                          "fields": "json://includes/forms/test2/includedForm.json"
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()
        )

        createForm(
            "clsForm", "test", """
                {
                  "source": "test.testService.findDataClassWithSuperClass",
                  "groups": [
                    {
                      "rows": [
                        {
                          "fields": "json://includes/forms/test2/includedForm.json"
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()
        )

        createIncludedFormAndConfigure("includedForm", "test2", """
            [
              {
                "name": "<caret>"
              }
            ]
        """.trimIndent())

        assertCompletionsContainsExact(
            "a", //From Cls
            "superclassField", //From SuperCls
            "stringField", //From DataClass
            "longField", //From DataClass
            "integerField" //From DataClass
        )
    }

    @Test
    fun `test field name reference in included form with json-flat declaration`() = with(fixture) {
        configureByFiles(
            "DataClass.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
        )

        createForm(
            "rootForm", "test", """
                {
                  "source": "test.testService.findData",
                  "groups": [
                    {
                      "rows": [
                        {
                          "fields": [
                            "json-flat://includes/forms/test2/includedForm.json"
                          ]
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()
        )

        createIncludedFormAndConfigure("includedForm", "test2", """
            [
              {
                "name": "<caret>stringField"
              }
            ]
        """.trimIndent())

        assertReferencedElementNameEquals("stringField")
    }

    @Test
    fun `test field name completion in included form with json-flat declaration`() = with(fixture) {
        configureByFiles(
            "DataClass.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
        )

        createForm(
            "rootForm", "test", """
                {
                  "source": "test.testService.findData",
                  "groups": [
                    {
                      "rows": [
                        {
                          "fields": [
                            "json-flat://includes/forms/test2/includedForm.json"
                          ]
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()
        )

        createIncludedFormAndConfigure("includedForm", "test2", """
            [
              {
                "name": "<caret>"
              }
            ]
        """.trimIndent())

        assertCompletionsContainsExact(
            "stringField",
            "longField",
            "integerField"
        )
    }

    @Test
    fun `test reference from form with related list field`() = with(fixture) {
        configureByFiles(
            "DataClass.kt",
            "DataClassWithList.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
        )

        createForm(
            "formWithListField", "test", """
                {
                  "source": "test.testService.findDataWithList",
                  "groups": [
                    {
                      "rows": [
                        {
                          "fields": [
                            {
                              "name": "dataClassList",
                              "type": "LIST",
                              "form": "test.listForm"
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()
        )

        createFormAndConfigure(
            "listForm", "test", """
                {
                  "groups": [
                    {
                      "rows": [
                        {
                          "fields": [
                            {
                              "name": "stringField<caret>"
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()
        )

        assertReferencedElementNameEquals("stringField")
    }

    @Test
    fun `test completion from form with related list field`() = with(fixture) {
        configureByFiles(
            "DataClass.kt",
            "DataClassWithList.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
        )

        createForm(
            "formWithListField", "test", """
                {
                  "source": "test.testService.findDataWithList",
                  "groups": [
                    {
                      "rows": [
                        {
                          "fields": [
                            {
                              "name": "dataClassList",
                              "type": "LIST",
                              "form": "test.listForm"
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()
        )

        createFormAndConfigure(
            "listForm", "test", """
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
            """.trimIndent()
        )

        assertCompletionsContainsExact(
            "stringField",
            "longField",
            "integerField"
        )
    }

}