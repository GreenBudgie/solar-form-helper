package com.solanteq.solar.plugin

import com.intellij.testFramework.RunsInEdt
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase5
import com.solanteq.solar.plugin.reference.request.ServiceMethodReference
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElement
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@RunsInEdt
class FormReferenceTest : LightJavaCodeInsightFixtureTestCase5(DEFAULT_DESCRIPTOR) {

    override fun getTestDataPath() = testDataPathWithSuffix("reference")

    @ParameterizedTest
    @ValueSource(strings = [
        "request",
        "countRequest",
        "source",
        "save",
        "remove"
    ])
    fun `test valid reference right after request literal`(requestLiteral: String) {
        doTest("serviceNameReference/ServiceImpl.java",
            """
            {
              "$requestLiteral": "test.service.<caret>findData"
            }
        """.trimIndent())
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "request",
        "countRequest",
        "source",
        "save",
        "remove"
    ])
    fun `test valid reference inside request object after name literal`(requestLiteral: String) {
        doTest("serviceNameReference/ServiceImpl.kt",
            """
            {
              "$requestLiteral": {
                "name": "test.service.<caret>findData"
              }
            }
        """.trimIndent())
    }

    private fun doTest(serviceClassPath: String, formText: String) {
        fixture.configureByFile(serviceClassPath)
        fixture.configureByFormText(formText)

        val methodReference = fixture.file.findReferenceAt(fixture.caretOffset)

        assertNotNull(methodReference)
        assertTrue(methodReference is ServiceMethodReference)

        val referencedMethod = methodReference!!.resolve().toUElement() as? UMethod

        assertNotNull(referencedMethod)
        assertTrue(referencedMethod is UMethod)
        assertEquals("findData", referencedMethod!!.name)
    }

}