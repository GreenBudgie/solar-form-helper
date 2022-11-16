package com.solanteq.solar.plugin

import com.intellij.testFramework.RunsInEdt
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@RunsInEdt
class FormCompletionTest : FormTestBase() {

    override fun getTestDataSuffix() = "completion"

    @Test
    fun `test service method completion`() {
        configureServices()

        fixture.configureByFormText("""
            {
              "request": "test.testService.<caret>"
            }
        """.trimIndent())

        val completions = fixture.completeBasic().map { it.lookupString }

        assertEquals(2, completions.size)
        assertTrue(completions.containsAll(listOf(
            "callableMethod1",
            "callableMethod2"
        )))
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "<caret>",
        "test.<caret>",
        "test.<caret>.",
        "<caret>.callableMethod1",
        "test.<caret>.callableMethod1"
    ])
    fun `test service name completion`(request: String) {
        configureServices()

        fixture.configureByFormText("""
            {
              "request": "$request"
            }
        """.trimIndent())

        val completions = fixture.completeBasic().map { it.lookupString }

        assertEquals(2, completions.size)
        assertTrue(completions.containsAll(listOf(
            "test.testService",
            "test.testServiceJava"
        )))
    }

    @Test
    fun `test form completion`() {
        fixture.createForm(
            "testFormNoGroup",
            "{}"
        )
        fixture.createForm(
            "testFormWithGroup",
            "{}",
            "test"
        )
        fixture.createForm(
            "testFormWithGroup",
            "{}",
            "test2"
        )

        fixture.createForm(
            "includedForm",
            "{}",
            "test2",
            true
        )

        fixture.configureByFormText("""
            {
              "form": "<caret>"
            }
        """.trimIndent()
        )

        val completions = fixture.completeBasic().map { it.lookupString }

        assertTrue("testFormNoGroup" in completions)
        assertTrue("test.testFormWithGroup" in completions)
        assertTrue("test2.testFormWithGroup" in completions)
        assertEquals(3, completions.size)
    }

    private fun configureServices() {
        fixture.configureByFiles(
            "TestService.kt",
            "TestServiceImpl.kt",
            "TestServiceJava.java",
            "TestServiceJavaImpl.java",
            "TestServiceNonCallable.kt",
            "TestServiceNonCallableImpl.kt",
        )
    }

}