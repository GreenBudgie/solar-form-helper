package com.solanteq.solar.plugin

import com.intellij.json.psi.JsonProperty
import com.solanteq.solar.plugin.base.*
import com.solanteq.solar.plugin.element.FormRequest
import com.solanteq.solar.plugin.inspection.form.InvalidRequestInspection
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
    fun `test valid reference right after request literal with java service`() = with(fixture) {
        doTestMethodReference("ServiceImpl.java",
            """
            {
              "request": "test.service.<caret>findData"
            }
        """.trimIndent())
    }

    @Test
    fun `test valid reference inside request object after name literal with kotlin service`() = with(fixture) {
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
    fun `test service name reference with different namings`(serviceName: String) = with(fixture) {
        configureByText("TestService.kt", """
            import com.solanteq.solar.commons.annotations.CallableService

            @CallableService
            interface $serviceName {

                @Callable
                fun findData(viewParams: ViewParams): List<TestData>

            }
        """.trimIndent())

        configureByText("TestServiceImpl.kt", """
            import org.springframework.stereotype.Service

            @Service("test.testService")
            class ${serviceName}Impl : $serviceName {

                override fun findData(viewParams: ViewParams): List<TestData> {
                    return listOf()
                }

            }
        """.trimIndent())

        configureByFormText(
            """
            {
              "request": "<caret>test.testService.findData"
            }
        """.trimIndent())

        assertReferencedElementNameEquals("${serviceName}Impl")
    }

    @Test
    fun `test reference to super method`() = with(fixture) {
        configureByFiles("ServiceImpl.kt", "SuperServiceImpl.kt")
        configureByFormText("""
            {
              "request": "test.service.<caret>superMethod"
            }
        """.trimIndent())

        assertReferencedElementNameEquals("superMethod")
    }

    @Test
    fun `test service method completion`() = with(fixture) {
        configureServicesForCompletion()

        configureByFormText("""
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
    fun `test service name completion`(request: String) = with(fixture) {
        configureServicesForCompletion()

        configureByFormText("""
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
    fun `test unresolved method inspection`(): Unit = with(fixture) {
        configureByFiles("TestService.kt", "TestServiceImpl.kt")
        createFormAndConfigure("testForm", "abc", """
                {
                  "source": "test.testService.<warning>nonExistentMethod</warning>"
                }
            """.trimIndent())

        enableInspections(InvalidRequestInspection::class.java)
        checkHighlighting()
    }

    @Test
    fun `test unresolved request inspection does not report resolved methods`(): Unit = with(fixture) {
        configureByFiles("TestService.kt", "TestServiceImpl.kt")
        createFormAndConfigure("testForm", "abc", """
                {
                  "source": "test.testService.callableMethod1"
                }
            """.trimIndent())

        enableInspections(InvalidRequestInspection::class.java)
        checkHighlighting()
    }

    @Test
    fun `test non callable method inspection`(): Unit = with(fixture) {
        configureByFiles("TestService.kt", "TestServiceImpl.kt")
        createFormAndConfigure("testForm", "abc", """
                {
                  "source": "test.testService.<warning>nonCallableMethod</warning>"
                }
            """.trimIndent())

        enableInspections(InvalidRequestInspection::class.java)
        checkHighlighting()
    }

    @Test
    fun `test non callable service inspection`(): Unit = with(fixture) {
        configureByFiles("TestServiceNonCallable.kt", "TestServiceNonCallableImpl.kt")
        createFormAndConfigure("testForm", "abc", """
                {
                  "source": "<warning>test.testServiceNonCallable</warning>.callableMethod1"
                }
            """.trimIndent())

        enableInspections(InvalidRequestInspection::class.java)
        checkHighlighting()
    }

    @Test
    fun `test non callable service and non callable method inspection`(): Unit = with(fixture) {
        configureByFiles("TestServiceNonCallable.kt", "TestServiceNonCallableImpl.kt")
        createFormAndConfigure("testForm", "abc", """
                {
                  "source": "<warning>test.testServiceNonCallable</warning>.<warning>nonCallableMethod</warning>"
                }
            """.trimIndent())

        enableInspections(InvalidRequestInspection::class.java)
        checkHighlighting()
    }

    @Test
    fun `test non callable service and unresolved method inspection`(): Unit = with(fixture) {
        configureByFiles("TestServiceNonCallable.kt", "TestServiceNonCallableImpl.kt")
        createFormAndConfigure("testForm", "abc", """
                {
                  "source": "<warning>test.testServiceNonCallable</warning>.<warning>nonExistentMethod</warning>"
                }
            """.trimIndent())

        enableInspections(InvalidRequestInspection::class.java)
        checkHighlighting()
    }

    @Test
    fun `test no warning if service is not found`(): Unit = with(fixture) {
        configureByFiles("TestService.kt", "TestServiceImpl.kt")
        createFormAndConfigure("testForm", "abc", """
                {
                  "source": "test.testService2.method"
                }
            """.trimIndent())

        enableInspections(InvalidRequestInspection::class.java)
        checkHighlighting()
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
    fun `test invalid request string error`(request: String) = with(fixture) {
        configureByFiles("TestService.kt", "TestServiceImpl.kt")
        createFormAndConfigure(
            "testForm", "abc", """
                {
                  "source": "<error>$request</error>"
                }
            """.trimIndent()
        )

        enableInspections(InvalidRequestInspection::class.java)
        checkHighlighting()
    }

    @Test
    fun `test no warning for callable method and service with multiple interfaces`(): Unit = with(fixture) {
        configureByFiles(
            "multipleInterfaces/CallableInterface.kt",
            "multipleInterfaces/NonCallableInterface.kt",
            "multipleInterfaces/AbstractService.kt",
            "multipleInterfaces/ServiceImpl.kt",
        )

        createFormAndConfigure(
            "testForm", "abc", """
                {
                  "source": "test.service.find"
                }
            """.trimIndent()
        )

        enableInspections(InvalidRequestInspection::class.java)
        checkHighlighting()
    }

    @Test
    fun `test java enum dropdown reference`() = with(fixture) {
        configureByFiles("DropdownJava.java")
        createFormAndConfigure("testForm", "abc", """
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
    fun `test kotlin enum dropdown reference`() = with(fixture) {
        configureByFiles("DropdownKotlin.kt")
        createFormAndConfigure("testForm", "abc", """
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
    fun `test only enum dropdown completion`(request: String) = with(fixture) {
        configureServicesForCompletion()
        configureByFiles("DropdownKotlin.kt", "DropdownJava.java")
        createFormAndConfigure("testForm", "abc", """
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
    fun `test no enum dropdown completion`() = with(fixture) {
        configureServicesForCompletion()
        configureByFiles("DropdownKotlin.kt", "DropdownJava.java")
        createFormAndConfigure("testForm", "abc", """
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
    fun `test both service and enum dropdown completion`() = with(fixture) {
        configureServicesForCompletion()
        configureByFiles("DropdownKotlin.kt", "DropdownJava.java")
        createFormAndConfigure("testForm", "abc", """
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
    fun `test do not create request element for 'remove' inline action`() = with(fixture) {
        createFormAndConfigure("testForm", "abc", """
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
    fun `test create request element for 'remove' form request`() = with(fixture) {
        createFormAndConfigure("testForm", "abc", """
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

        fixture.assertReferencedElementNameEquals("findData")
    }

}