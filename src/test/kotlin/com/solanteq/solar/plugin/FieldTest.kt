package com.solanteq.solar.plugin

import org.junit.jupiter.api.Test

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

        assertReferencedElementName("stringField")
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

        assertCompletionsContainsExact(
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

        assertReferencedElementName("nested")
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

        assertReferencedElementName("stringField")
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

        assertCompletionsContainsExact(
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

        assertReferencedElementName("superclassField")
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

        assertReferencedElementName("stringField")
    }

    @Test
    fun `test field completion from multiple inline configurations`() {
        fixture.configureByFiles(
            "basic/Cls.kt",
            "basic/SuperCls.java",
            "basic/DataClass.kt",
            "basic/TestService.kt",
            "basic/TestServiceImpl.kt"
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
            "basic/CustomService.kt",
            "basic/CustomServiceImpl.kt",
            "basic/GenericService.kt",
            "basic/GenericServiceImpl.kt",
            "basic/DataClass.kt"
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
    fun `test temp`() {
        fixture.configureByFiles(
            "basic/CustomService.kt",
            "basic/CustomServiceImpl.kt",
            "basic/GenericService.kt",
            "basic/GenericServiceImpl.kt",
            "basic/DataClass.kt"
        )

        fixture.configureByFormText("""
            {
              "request": "test.customService.<caret>"
            }
        """.trimIndent())

        assertCompletionsContainsExact(
            "stringField",
            "longField",
            "integerField"
        )
    }

    @Test
    fun `test field reference from generified list`() {
        fixture.configureByFiles(
            "basic/CustomService.kt",
            "basic/CustomServiceImpl.kt",
            "basic/GenericService.kt",
            "basic/GenericServiceImpl.kt",
            "basic/DataClass.kt"
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

        assertReferencedElementName("stringField")
    }

    @Test
    fun `test field reference from inline configuration`() {
        fixture.configureByFiles(
            "basic/DataClass.kt",
            "basic/TestService.kt",
            "basic/TestServiceImpl.kt"
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

        assertReferencedElementName("stringField")
    }

}