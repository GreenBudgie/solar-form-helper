package com.solanteq.solar.plugin

import com.intellij.ide.starters.shared.JUNIT_TEST_RUNNER
import com.intellij.testFramework.RunsInEdt
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase4
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase5
import com.intellij.tests.JUnit5Runner
import com.solanteq.solar.plugin.reference.request.ServiceMethodReference
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.runner.RunWith

@RunsInEdt
class FormReferenceTest : LightJavaCodeInsightFixtureTestCase5() {

    override fun getTestDataPath() = testDataPathWithSuffix("reference")

    @ParameterizedTest
    @ValueSource(strings = [
        "request",
        "countRequest",
        "source",
        "save",
        "remove"
    ])
    fun testValidReferenceRightAfterRequestLiteral(requestLiteral: String) {
        fixture.configureByFile("serviceNameReference/ServiceImpl.kt")
        fixture.configureByFormText("""
            {
              "$requestLiteral": "test.service.<caret>findData"
            }
        """.trimIndent())

        val methodReference = fixture.file.findReferenceAt(fixture.caretOffset)

        val errorMessage = "Reference for request literal \"$requestLiteral\" is invalid or not found"
        assertNotNull(methodReference, errorMessage)
        assertTrue(methodReference is ServiceMethodReference, errorMessage)
    }

}