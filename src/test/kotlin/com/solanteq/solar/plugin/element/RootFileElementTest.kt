package com.solanteq.solar.plugin.element

import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.base.createForm
import com.solanteq.solar.plugin.base.createIncludedForm
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RootFileElementTest : LightPluginTestBase() {

    @Test
    fun `test find all groups with groups declaration (no json includes)`() {
        val form = fixture.createForm("rootForm", "test", """
            {
              "groups": [
                {
                  "name": "group1",
                  "rows": []
                },
                {
                  "name": "group2"
                }
              ]
            }
        """.trimIndent()).toFormElement<FormRootFile>()!!

        val expectedGroupNames = listOf("group1", "group2").sorted()
        val actualGroupNames = form.allGroups.mapNotNull { it.name }.sorted()

        Assertions.assertIterableEquals(expectedGroupNames, actualGroupNames)
    }

    @Test
    fun `test find all groups with groups declaration (with json includes)`() {
        val form = fixture.createForm("rootForm", "test", """
            {
              "groups": [
                {
                  "name": "group1"
                },
                "json-flat://includes/forms/test/flatGroups.json",
                "json://includes/forms/test/group.json"
              ]
            }
        """.trimIndent()).toFormElement<FormRootFile>()!!

        fixture.createIncludedForm("group", "test", """
            {
              "name": "group2"
            }
        """.trimIndent())

        fixture.createIncludedForm("flatGroups", "test", """
            [
              {
                "name": "group3",
                "rows": []
              },
              "json-flat://includes/forms/test5/group5.json"
            ]
        """.trimIndent())

        fixture.createIncludedForm("group5", "test5", """
            [
              {
                "name": "group4"
              }
            ]
        """.trimIndent())

        val expectedGroupNames = listOf("group1", "group2", "group3", "group4").sorted()
        val actualGroupNames = form.allGroups.mapNotNull { it.name }.sorted()

        Assertions.assertIterableEquals(expectedGroupNames, actualGroupNames)
    }

    @Test
    fun `test find all groups with groupRows declaration (no json includes)`() {
        val form = fixture.createForm("rootForm", "test", """
            {
              "groupRows": [
                {
                  "groups": [
                    {
                      "name": "group1"
                    },
                    {
                      "name": "group2"
                    }
                  ]
                },
                {
                  "groups": [
                    {
                      "name": "group3"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()).toFormElement<FormRootFile>()!!

        val expectedGroupNames = listOf("group1", "group2", "group3").sorted()
        val actualGroupNames = form.allGroups.mapNotNull { it.name }.sorted()

        Assertions.assertIterableEquals(expectedGroupNames, actualGroupNames)
    }

    @Test
    fun `test find all groups with groupRows declaration (with json includes)`() {
        val form = fixture.createForm("rootForm", "test", """
            {
              "groupRows": [
                {
                  "groups": [
                    {
                      "name": "group1"
                    },
                    "json://includes/forms/test2/group2.json"
                  ]
                },
                "json-flat://includes/forms/test3/groups.json"
              ]
            }
        """.trimIndent()).toFormElement<FormRootFile>()!!

        fixture.createIncludedForm("group2", "test2", """
            {
              "name": "group2"
            }
        """.trimIndent())

        fixture.createIncludedForm("groups", "test3", """
            [
              {
                "groups": [
                  {
                    "name": "group3"
                  },
                  {
                    "name": "group4"
                  }
                ]
              },
              {
                "groups": "json://includes/forms/test5/group5.json"
              }
            ]
        """.trimIndent())

        fixture.createIncludedForm("group5", "test5", """
            [
              {
                "name": "group5"
              }
            ]
        """.trimIndent())

        val expectedGroupNames = listOf("group1", "group2", "group3", "group4", "group5").sorted()
        val actualGroupNames = form.allGroups.mapNotNull { it.name }.sorted()

        Assertions.assertIterableEquals(expectedGroupNames, actualGroupNames)
    }

    @Test
    fun `test find all fields (no json includes)`() {
        val form = fixture.createForm("rootForm", "test", """
            {
              "groups": [
                {
                  "rows": [
                    {
                      "fields": [
                        {
                          "name": "field1"
                        },
                        {
                          "name": "field2"
                        }
                      ]
                    },
                    {
                      "fields": [
                        {
                          "name": "field3"
                        }
                      ]
                    }
                  ]
                },
                {
                  "rows": [
                    {
                      "fields": [
                        {
                          "name": "field4"
                        },
                        {
                          "name": "field5"
                        }
                      ]
                    },
                    {
                      "fields": [
                        {
                          "name": "field6"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()).toFormElement<FormRootFile>()!!

        val expectedFieldNames = listOf(
            "field1", "field2", "field3", "field4", "field5", "field6"
        ).sorted()
        val actualFieldNames = form.allFields.mapNotNull { it.name }.sorted()

        Assertions.assertIterableEquals(expectedFieldNames, actualFieldNames)
    }

    @Test
    fun `test find all fields (with json includes)`() {
        val form = fixture.createForm("rootForm", "test", """
            {
              "groups": [
                {
                  "rows": [
                    {
                      "fields": [
                        {
                          "name": "field1"
                        },
                        "json-flat://includes/forms/test/fields.json"
                      ]
                    },
                    "json://includes/forms/test/row.json"
                  ]
                },
                {
                  "rows": "json://includes/forms/test/rows.json"
                },
                "json-flat://includes/forms/test/flatGroups.json",
                "json://includes/forms/test/commonGroup.json"
              ]
            }
        """.trimIndent()).toFormElement<FormRootFile>()!!

        fixture.createIncludedForm("fields", "test", """
            [
              {
                "name": "field2"
              },
              {
                "name": "field3"
              }
            ]
        """.trimIndent())

        fixture.createIncludedForm("row", "test", """
            {
              "fields": [
                {
                  "name": "field4"
                }
              ]
            }
        """.trimIndent())

        fixture.createIncludedForm("rows", "test", """
            [
              {
                "fields": [
                  {
                    "name": "field5"
                  }
                ]
              },
              {
                "fields": [
                  "json://includes/forms/test/commonField.json"
                ]
              }
            ]
        """.trimIndent())

        fixture.createIncludedForm("flatGroups", "test", """
            [
              {
                "rows": [
                  {
                    "fields": [
                      {
                        "name": "field6"
                      }
                    ]
                  },
                  {
                    "fields": [
                      {
                        "name": "field7"
                      }
                    ]
                  }
                ]
              },
              "json://includes/forms/test/commonGroup.json"
            ]
        """.trimIndent())

        fixture.createIncludedForm("commonGroup", "test", """
            {
              "rows": [
                {
                  "fields": [
                    "json://includes/forms/test/commonField.json",
                    {
                      "name": "commonField2"
                    }
                  ]
                }
              ]
            }
        """.trimIndent())

        fixture.createIncludedForm("commonField", "test", """
            {
              "name": "commonField"
            }
        """.trimIndent())

        val expectedFieldNames = listOf(
            "field1", "field2", "field3", "field4", "field5", "field6", "field7",
            "commonField", "commonField", "commonField", "commonField2", "commonField2"
        ).sorted()
        val actualFieldNames = form.allFields.mapNotNull { it.name }.sorted()

        Assertions.assertIterableEquals(expectedFieldNames, actualFieldNames)
    }

}