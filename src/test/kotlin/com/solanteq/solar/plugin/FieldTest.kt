package com.solanteq.solar.plugin

import org.junit.jupiter.api.Test

class FieldTest : FormTestBase() {

    override fun getTestDataSuffix() = "field"

    @Test
    fun `test field name reference from source request`() {
        fixture.configureByFiles(
            "DataClass.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
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

        assertReferencedElementNameEquals("stringField")
    }

    @Test
    fun `test field name completion from source request`() {
        fixture.configureByFiles(
            "DataClass.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
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

        assertCompletionsContainsExact(
            "stringField",
            "longField",
            "integerField"
        )
    }

    @Test
    fun `test field name reference from source request to nested property`() {
        fixture.configureByFiles(
            "DataClassWithNestedProperty.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
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

        assertReferencedElementNameEquals("nested")
    }

    @Test
    fun `test field name reference from source request to nested property field`() {
        fixture.configureByFiles(
            "DataClass.kt",
            "DataClassWithNestedProperty.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
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

        assertReferencedElementNameEquals("stringField")
    }

    @Test
    fun `test field name completion from source request with nested property`() {
        fixture.configureByFiles(
            "DataClass.kt",
            "DataClassWithNestedProperty.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
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

        assertCompletionsContainsExact(
            "stringField",
            "longField",
            "integerField"
        )
    }

    @Test
    fun `test field name reference from source request to superclass`() {
        fixture.configureByFiles(
            "Cls.kt",
            "SuperCls.java",
            "TestService.kt",
            "TestServiceImpl.kt",
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

        assertReferencedElementNameEquals("superclassField")
    }

    @Test
    fun `test field name reference from source request with generic return type`() {
        fixture.configureByFiles(
            "CustomService.kt",
            "CustomServiceImpl.kt",
            "GenericService.kt",
            "GenericServiceImpl.kt",
            "DataClass.kt"
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

        assertReferencedElementNameEquals("stringField")
    }

    @Test
    fun `test field completion from multiple inline configurations`() {
        fixture.configureByFiles(
            "Cls.kt",
            "SuperCls.java",
            "DataClass.kt",
            "TestService.kt",
            "TestServiceImpl.kt"
        )

        fixture.createForm("dataClass", """
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
        """.trimIndent(), "abc1")

        fixture.createForm("cls", """
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
        """.trimIndent(), "abc2")

        fixture.createFormAndConfigure("testForm", """
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
        """.trimIndent(), "test")

        assertCompletionsContainsExact(
            "a", //From Cls
            "superclassField", //From SuperCls
            "stringField", //From DataClass
            "longField", //From DataClass
            "integerField" //From DataClass
        )
    }

    @Test
    fun `test field completion from generified list`() {
        fixture.configureByFiles(
            "CustomService.kt",
            "CustomServiceImpl.kt",
            "GenericService.kt",
            "GenericServiceImpl.kt",
            "DataClass.kt"
        )

        fixture.createForm("dataClass", """
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
        """.trimIndent(), "abc")

        fixture.createFormAndConfigure("testForm", """
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
        """.trimIndent(), "test")

        assertCompletionsContainsExact(
            "stringField",
            "longField",
            "integerField"
        )
    }

    @Test
    fun `test field reference from generified list`() {
        fixture.configureByFiles(
            "CustomService.kt",
            "CustomServiceImpl.kt",
            "GenericService.kt",
            "GenericServiceImpl.kt",
            "DataClass.kt"
        )

        fixture.createForm("dataClass", """
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
        """.trimIndent(), "abc")

        fixture.createFormAndConfigure("testForm", """
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
        """.trimIndent(), "test")

        assertReferencedElementNameEquals("stringField")
    }

    @Test
    fun `test field reference from inline configuration`() {
        fixture.configureByFiles(
            "DataClass.kt",
            "TestService.kt",
            "TestServiceImpl.kt"
        )

        fixture.createForm("dataClass", """
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
        """.trimIndent(), "abc")

        fixture.createFormAndConfigure("testForm", """
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
        """.trimIndent(), "test")

        assertReferencedElementNameEquals("stringField")
    }

    @Test
    fun `test field name reference in included form`() {
        fixture.configureByFiles(
            "DataClass.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
        )

        fixture.createForm("rootForm", """
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
        """.trimIndent(), "test")

        fixture.createIncludedFormAndConfigure("includedForm", "test2", """
            [
              {
                "name": "<caret>stringField"
              }
            ]
        """.trimIndent())

        assertReferencedElementNameEquals("stringField")
    }

    @Test
    fun `test field name completion in included form for multiple declarations`() {
        fixture.configureByFiles(
            "DataClass.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
            "Cls.kt",
            "SuperCls.java"
        )

        fixture.createForm("dataClassForm", """
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
        """.trimIndent(), "test")

        fixture.createForm("clsForm", """
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
        """.trimIndent(), "test")

        fixture.createIncludedFormAndConfigure("includedForm", "test2", """
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
    fun `test field name reference in included form with json-flat declaration`() {
        fixture.configureByFiles(
            "DataClass.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
        )

        fixture.createForm("rootForm", """
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
        """.trimIndent(), "test")

        fixture.createIncludedFormAndConfigure("includedForm", "test2", """
            [
              {
                "name": "<caret>stringField"
              }
            ]
        """.trimIndent())

        assertReferencedElementNameEquals("stringField")
    }

    @Test
    fun `test field name completion in included form with json-flat declaration`() {
        fixture.configureByFiles(
            "DataClass.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
        )

        fixture.createForm("rootForm", """
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
        """.trimIndent(), "test")

        fixture.createIncludedFormAndConfigure("includedForm", "test2", """
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
    fun `test reference from form with related list field`() {
        fixture.configureByFiles(
            "DataClass.kt",
            "DataClassWithList.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
        )

        fixture.createForm("formWithListField", """
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
        """.trimIndent(), "test")

        fixture.createFormAndConfigure("listForm", """
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
        """.trimIndent(), "test")

        assertReferencedElementNameEquals("stringField")
    }

    @Test
    fun `test completion from form with related list field`() {
        fixture.configureByFiles(
            "DataClass.kt",
            "DataClassWithList.kt",
            "TestService.kt",
            "TestServiceImpl.kt",
        )

        fixture.createForm("formWithListField", """
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
        """.trimIndent(), "test")

        fixture.createFormAndConfigure("listForm", """
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
        """.trimIndent(), "test")

        assertCompletionsContainsExact(
            "stringField",
            "longField",
            "integerField"
        )
    }

}