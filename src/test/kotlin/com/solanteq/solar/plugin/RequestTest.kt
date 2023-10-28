package com.solanteq.solar.plugin

import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.base.JavaPluginTestBase
import com.solanteq.solar.plugin.base.configureByFormText
import com.solanteq.solar.plugin.base.createFormAndConfigure
import com.solanteq.solar.plugin.element.FormRequest
import com.solanteq.solar.plugin.inspection.InvalidRequestInspection
import com.solanteq.solar.plugin.reference.request.CallableMethodReference
import com.solanteq.solar.plugin.reference.request.CallableServiceReference
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class RequestTest : JavaPluginTestBase() {

    override fun getTestDataSuffix() = "request"

    @Test
    fun `test valid reference right after request literal with java service`() {
        doTestMethodReference("ServiceImpl.java",
            """
            {
              "request": "test.service.<caret>findData"
            }
        """.trimIndent())
    }

    @Test
    fun `test valid reference inside request object after name literal with kotlin service`() {
        doTestMethodReference("ServiceImpl.kt",
            """
            {
              "save": {
                "name": "test.service.<caret>findData"
              }
            }
        """.trimIndent())
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "TestService",
        "NonConventionalServiceName"
    ])
    fun `test service name reference with different namings`(serviceName: String) {
        fixture.configureByText("TestService.kt", """
            import com.solanteq.solar.commons.annotations.CallableService

            @CallableService
            interface $serviceName {

                @Callable
                fun findData(viewParams: ViewParams): List<TestData>

            }
        """.trimIndent())

        fixture.configureByText("TestServiceImpl.kt", """
            import org.springframework.stereotype.Service

            @Service("test.testService")
            class ${serviceName}Impl : $serviceName {

                override fun findData(viewParams: ViewParams): List<TestData> {
                    return listOf()
                }

            }
        """.trimIndent())

        fixture.configureByFormText(
            """
            {
              "request": "<caret>test.testService.findData"
            }
        """.trimIndent())

        assertReferencedElementNameEquals("${serviceName}Impl")
    }

    @Test
    fun `test reference to super method`() {
        fixture.configureByFiles("ServiceImpl.kt", "SuperServiceImpl.kt")
        fixture.configureByFormText("""
            {
              "request": "test.service.<caret>superMethod"
            }
        """.trimIndent())

        assertReferencedElementNameEquals("superMethod")
    }

    @Test
    fun `test service method completion`() {
        configureServicesForCompletion()

        fixture.configureByFormText("""
            {
              "request": "test.testService.<caret>"
            }
        """.trimIndent())

        assertCompletionsContainsExact(
            "callableMethod1",
            "callableMethod2"
        )
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "<caret>",
        "test.<caret>.",
        "test.<caret>.callableMethod1"
    ])
    fun `test service name completion`(request: String) {
        configureServicesForCompletion()

        fixture.configureByFormText("""
            {
              "request": "$request"
            }
        """.trimIndent())

        assertCompletionsContainsExact(
            "test.testService",
            "test.testServiceJava"
        )
    }

    @Test
    fun `test unresolved method inspection`() {
        fixture.configureByFiles("TestService.kt", "TestServiceImpl.kt")
        fixture.createFormAndConfigure("testForm", "abc", """
                {
                  "source": "test.testService.<error>nonExistentMethod</error>"
                }
            """.trimIndent())

        fixture.enableInspections(InvalidRequestInspection::class.java)
        fixture.checkHighlighting()
    }

    @Test
    fun `test unresolved request inspection does not report resolved methods`() {
        fixture.configureByFiles("TestService.kt", "TestServiceImpl.kt")
        fixture.createFormAndConfigure("testForm", "abc", """
                {
                  "source": "test.testService.callableMethod1"
                }
            """.trimIndent())

        fixture.enableInspections(InvalidRequestInspection::class.java)
        fixture.checkHighlighting()
    }

    @Test
    fun `test non callable method inspection`() {
        fixture.configureByFiles("TestService.kt", "TestServiceImpl.kt")
        fixture.createFormAndConfigure("testForm", "abc", """
                {
                  "source": "test.testService.<warning>nonCallableMethod</warning>"
                }
            """.trimIndent())

        fixture.enableInspections(InvalidRequestInspection::class.java)
        fixture.checkHighlighting()
    }

    @Test
    fun `test non callable service inspection`() {
        fixture.configureByFiles("TestServiceNonCallable.kt", "TestServiceNonCallableImpl.kt")
        fixture.createFormAndConfigure("testForm", "abc", """
                {
                  "source": "<warning>test.testServiceNonCallable</warning>.callableMethod1"
                }
            """.trimIndent())

        fixture.enableInspections(InvalidRequestInspection::class.java)
        fixture.checkHighlighting()
    }

    @Test
    fun `test non callable service and non callable method inspection`() {
        fixture.configureByFiles("TestServiceNonCallable.kt", "TestServiceNonCallableImpl.kt")
        fixture.createFormAndConfigure("testForm", "abc", """
                {
                  "source": "<warning>test.testServiceNonCallable</warning>.<warning>nonCallableMethod</warning>"
                }
            """.trimIndent())

        fixture.enableInspections(InvalidRequestInspection::class.java)
        fixture.checkHighlighting()
    }

    @Test
    fun `test non callable service and unresolved method inspection`() {
        fixture.configureByFiles("TestServiceNonCallable.kt", "TestServiceNonCallableImpl.kt")
        fixture.createFormAndConfigure("testForm", "abc", """
                {
                  "source": "<warning>test.testServiceNonCallable</warning>.<error>nonExistentMethod</error>"
                }
            """.trimIndent())

        fixture.enableInspections(InvalidRequestInspection::class.java)
        fixture.checkHighlighting()
    }

    @Test
    fun `test no warning if service is not found`() {
        fixture.configureByFiles("TestService.kt", "TestServiceImpl.kt")
        fixture.createFormAndConfigure("testForm", "abc", """
                {
                  "source": "test.testService2.method"
                }
            """.trimIndent())

        fixture.enableInspections(InvalidRequestInspection::class.java)
        fixture.checkHighlighting()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "",
            "   ",
            "test",
            "test.testService",
            "test.testService.",
            "test.testService..",
            "test.testService.nonCallableMethod.",
            "test..nonCallableMethod",
            "..nonCallableMethod",
            "...",
            "test.testService.   ",
        ]
    )
    fun `test invalid request string error`(request: String) {
        fixture.configureByFiles("TestService.kt", "TestServiceImpl.kt")
        fixture.createFormAndConfigure(
            "testForm", "abc", """
                {
                  "source": "<error>$request</error>"
                }
            """.trimIndent()
        )

        fixture.enableInspections(InvalidRequestInspection::class.java)
        fixture.checkHighlighting()
    }

    @Test
    fun `test no warning for callable method and service with multiple interfaces`() {
        fixture.configureByFiles(
            "multipleInterfaces/CallableInterface.kt",
            "multipleInterfaces/NonCallableInterface.kt",
            "multipleInterfaces/AbstractService.kt",
            "multipleInterfaces/ServiceImpl.kt",
        )

        fixture.createFormAndConfigure(
            "testForm", "abc", """
                {
                  "source": "test.service.find"
                }
            """.trimIndent()
        )

        fixture.enableInspections(InvalidRequestInspection::class.java)
        fixture.checkHighlighting()
    }

    @Test
    fun `test java enum dropdown reference`() {
        fixture.configureByFiles("DropdownJava.java")
        fixture.createFormAndConfigure("testForm", "abc", """
                {
                  "source": {
                    "name": "test.<caret>dropdownJava.findAll",
                    "group": "${'$'}dropdown"
                  }
                }
            """.trimIndent())

        assertReferencedElementNameEquals("DropdownJava")
    }

    @Test
    fun `test kotlin enum dropdown reference`() {
        fixture.configureByFiles("DropdownKotlin.kt")
        fixture.createFormAndConfigure("testForm", "abc", """
                {
                  "source": {
                    "name": "test.<caret>dropdownKotlin.findAll",
                    "group": "${'$'}dropdown"
                  }
                }
            """.trimIndent())

        assertReferencedElementNameEquals("DropdownKotlin")
    }

    //Only add dropdown completions if the $dropdown group is specified
    @ParameterizedTest
    @ValueSource(strings = [
        "<caret>",
        "test.<caret>.",
        "test.<caret>.findAll"
    ])
    fun `test only enum dropdown completion`(request: String) {
        configureServicesForCompletion()
        fixture.configureByFiles("DropdownKotlin.kt", "DropdownJava.java")
        fixture.createFormAndConfigure("testForm", "abc", """
                {
                  "source": {
                    "name": "$request",
                    "group": "${'$'}dropdown"
                  }
                }
            """.trimIndent())

        assertCompletionsContainsExact("test.dropdownKotlin.findAll", "test.dropdownJava.findAll")
    }

    //Do not add dropdown completions if $dropdown is not specified
    @Test
    fun `test no enum dropdown completion`() {
        configureServicesForCompletion()
        fixture.configureByFiles("DropdownKotlin.kt", "DropdownJava.java")
        fixture.createFormAndConfigure("testForm", "abc", """
                {
                  "source": {
                    "name": "<caret>",
                    "group": "test"
                  }
                }
            """.trimIndent())

        assertCompletionsContainsExact("test.testService", "test.testServiceJava")
    }

    //Add both dropdowns and services to completions if no group is specified
    @Test
    fun `test both service and enum dropdown completion`() {
        configureServicesForCompletion()
        fixture.configureByFiles("DropdownKotlin.kt", "DropdownJava.java")
        fixture.createFormAndConfigure("testForm", "abc", """
                {
                  "source": {
                    "name": "<caret>"
                  }
                }
            """.trimIndent())

        assertCompletionsContainsExact(
            "test.testService",
            "test.testServiceJava",
            "test.dropdownKotlin.findAll",
            "test.dropdownJava.findAll"
        )
    }

    @Test
    fun `test do not create request element for 'remove' inline action`() {
        fixture.createFormAndConfigure("testForm", "abc", """
                {
                  "remove": {
                    "name": "test<caret>"
                    "request": [
                      "name": "test"
                    ]
                  }
                }
            """.trimIndent())

        val stringLiteralAtCaret = getJsonStringLiteralAtCaret()
        val requestProperty = stringLiteralAtCaret.parent.parent.parent as JsonProperty

        assertTrue(stringLiteralAtCaret.references.none { it is CallableMethodReference })
        assertTrue(stringLiteralAtCaret.references.none { it is CallableServiceReference })
        assertFalse(FormRequest.canBeCreatedFrom(requestProperty))
    }

    @Test
    fun `test create request element for 'remove' form request`() {
        fixture.createFormAndConfigure("testForm", "abc", """
                {
                  "remove": {
                    "name": "test<caret>"
                  }
                }
            """.trimIndent())

        val stringLiteralAtCaret = getJsonStringLiteralAtCaret()
        val requestProperty = stringLiteralAtCaret.parent.parent.parent as JsonProperty

        assertTrue(stringLiteralAtCaret.references.any {
            it is CallableMethodReference || it is CallableServiceReference
        })
        assertTrue(FormRequest.canBeCreatedFrom(requestProperty))
    }

    private fun configureServicesForCompletion() {
        fixture.configureByFiles(
            "TestService.kt",
            "TestServiceImpl.kt",
            "TestServiceJava.java",
            "TestServiceJavaImpl.java",
            "TestServiceNonCallable.kt",
            "TestServiceNonCallableImpl.kt",
        )
    }

    private fun doTestMethodReference(serviceClassPath: String, formText: String) {
        fixture.configureByFile(serviceClassPath)
        fixture.configureByFormText(formText)

        assertReferencedElementNameEquals("findData")
    }

}