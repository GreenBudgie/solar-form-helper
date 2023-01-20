package com.solanteq.solar.plugin

import com.solanteq.solar.plugin.base.JavaPluginTestBase
import com.solanteq.solar.plugin.base.configureByFormText
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