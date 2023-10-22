package com.solanteq.solar.plugin

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.jetbrains.rd.util.first
import com.solanteq.solar.plugin.base.LightPluginTestBase
import com.solanteq.solar.plugin.base.createForm
import com.solanteq.solar.plugin.base.createIncludedForm
import com.solanteq.solar.plugin.search.FormGraphSearch
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FormGraphSearchTest : LightPluginTestBase() {

    private val context = mutableMapOf<Int, VirtualFile>()

    @AfterEach
    fun tearDown() = context.clear()

    @Test
    fun `findParentForms - no parents are found for root form`() {
        graph()
            .rootForm(1)
            .includedForm(2).parents(1)
            .build()

        FormGraphSearch.findParentForms(fixture.project, form(1)).checkEmpty()
    }

    @Test
    fun `findParentForms - only direct parents are found`() {
        graph()
            .rootForm(1)
            .includedForm(2).parents(1)
            .includedForm(3).parents(1)
            .includedForm(4).parents(3)
            .includedForm(5).parents(2, 4)
            .build()

        FormGraphSearch.findParentForms(fixture.project, form(5)).checkIndices(2, 4)
    }

    @Test
    fun `findParentForms - no parents are found for form with cycle`() {
        graph()
            .rootForm(1)
            // An infinite cycle formed, but should be ok even with incorrect configuration like this
            .includedForm(2).parents(2)
            .build()

        FormGraphSearch.findParentForms(fixture.project, form(2)).checkEmpty()
    }

    @Test
    fun `findParentForms - only one parent found for form with cycle`() {
        graph()
            .rootForm(1)
            // An infinite cycle formed, but should be ok even with incorrect configuration like this
            .includedForm(2).parents(3)
            .includedForm(3).parents(2)
            .build()

        FormGraphSearch.findParentForms(fixture.project, form(3)).checkIndices(2)
    }

    @Test
    fun `findParentFormsRecursively - no parents are found for root form`() {
        graph()
            .rootForm(1)
            .includedForm(2).parents(1)
            .build()

        FormGraphSearch.findParentFormsRecursively(fixture.project, form(1)).checkEmpty()
    }

    @Test
    fun `findParentFormsRecursively - all parents are found`() {
        graph()
            .rootForm(1)
            .includedForm(2).parents(1)
            .includedForm(3).parents(1)
            .includedForm(4).parents(3)
            .includedForm(5).parents(2, 4)
            .includedForm(6).parents(5)
            .includedForm(7).parents(1, 6)
            .build()

        FormGraphSearch.findParentFormsRecursively(fixture.project, form(5))
            .checkIndices(1, 2, 3, 4)
    }

    @Test
    fun `findParentFormsRecursively - all parents are found with infinite loops`() {
        graph()
            .rootForm(1)
            .rootForm(8)
            .includedForm(2).parents(1, 8)
            .includedForm(3).parents(1, 4)
            .includedForm(4).parents(3)
            .includedForm(5).parents(2, 4, 5)
            .includedForm(6).parents(5)
            .includedForm(7).parents(1, 6)
            .build()

        FormGraphSearch.findParentFormsRecursively(fixture.project, form(5))
            .checkIndices(1, 2, 3, 4, 8)
    }

    @Test
    fun `findChildForms - no children are found`() {
        graph()
            .rootForm(1)
            .includedForm(2).parents(1)
            .includedForm(3).parents(1, 2)
            .build()

        FormGraphSearch.findChildForms(fixture.project, form(3)).checkEmpty()
    }

    @Test
    fun `findChildForms - only direct children are found`() {
        graph()
            .rootForm(1)
            .includedForm(2).parents(1)
            .includedForm(3).parents(1, 2)
            .includedForm(4).parents(3)
            .includedForm(5).parents(2, 4)
            .build()

        FormGraphSearch.findChildForms(fixture.project, form(2)).checkIndices(3, 5)
    }

    @Test
    fun `findChildForms - only direct children are found with infinite loops`() {
        graph()
            .rootForm(1)
            .includedForm(2).parents(1, 2)
            .includedForm(3).parents(1, 2, 4)
            .includedForm(4).parents(3)
            .includedForm(5).parents(2, 4)
            .build()

        FormGraphSearch.findChildForms(fixture.project, form(2)).checkIndices(3, 5)
    }

    @Test
    fun `findChildFormsRecursively - no forms are found`() {
        graph()
            .rootForm(1)
            .includedForm(2).parents(1)
            .includedForm(3).parents(2, 1)
            .build()

        FormGraphSearch.findChildFormsRecursively(fixture.project, form(3)).checkEmpty()
    }

    @Test
    fun `findChildFormsRecursively - all forms are found`() {
        graph()
            .rootForm(1)
            .rootForm(7)
            .includedForm(2).parents(1, 2)
            .includedForm(3).parents(1, 2, 4)
            .includedForm(4).parents(3)
            .includedForm(5).parents(2, 4)
            .includedForm(6).parents(7)
            .build()

        FormGraphSearch.findChildFormsRecursively(fixture.project, form(1))
            .checkIndices(2, 3, 4, 5)
    }

    @Test
    fun `findTopmostRootForms - no forms are found for root form`() {
        graph()
            .rootForm(1)
            .rootForm(2)
            .includedForm(3).parents(1, 2)
            .build()

        FormGraphSearch.findTopmostRootForms(fixture.project, form(1)).checkEmpty()
    }

    @Test
    fun `findTopmostRootForms - all forms are found`() {
        graph()
            .rootForm(1)
            .rootForm(2)
            .rootForm(3)
            .includedForm(4).parents(1)
            .includedForm(5).parents(2)
            .includedForm(6).parents(3)
            .includedForm(7).parents(4)
            .includedForm(8).parents(5)
            .includedForm(9).parents(7, 8)
            .build()

        FormGraphSearch.findTopmostRootForms(fixture.project, form(9)).checkIndices(1, 2)
    }

    @Test
    fun `findAllRelatedForms - all forms are found`() {
        graph()
            .rootForm(1)
            .includedForm(2).parents(1, 8)
            .includedForm(3).parents(2)
            .includedForm(4).parents(2)
            .includedForm(5).parents(1)
            .includedForm(6).parents(5)
            .includedForm(7).parents(5)
            .rootForm(8)
            .includedForm(9).parents(8)
            .includedForm(10).parents(1, 8)
            .includedForm(11).parents(9)
            .includedForm(12).parents(11, 4)
            .build()

        FormGraphSearch.findAllRelatedForms(fixture.project, form(5))
            .checkIndices(1, 2, 3, 4, 6, 7, 10, 12)
    }

    @Test
    fun `processAllRelatedForms - all forms are processed including self`() {
        graph()
            .rootForm(1)
            .includedForm(2).parents(1, 8)
            .includedForm(3).parents(2)
            .includedForm(4).parents(2)
            .includedForm(5).parents(1)
            .includedForm(6).parents(5)
            .includedForm(7).parents(5)
            .rootForm(8)
            .includedForm(9).parents(8)
            .includedForm(10).parents(1, 8)
            .includedForm(11).parents(9)
            .includedForm(12).parents(11, 4)
            .build()

        val processedIndices = mutableListOf<Int>()
        FormGraphSearch.processAllRelatedForms(fixture.project, form(5), true) {
            processedIndices += index(it)
            true
        }

        processedIndices.checkIndicesProcessedOnlyOnce(1, 2, 3, 4, 5, 6, 7, 10, 12)
    }

    @Test
    fun `processAllRelatedForms - all forms are processed excluding self`() {
        graph()
            .rootForm(1)
            .includedForm(2).parents(1, 8)
            .includedForm(3).parents(2)
            .includedForm(4).parents(2)
            .includedForm(5).parents(1)
            .includedForm(6).parents(5)
            .includedForm(7).parents(5)
            .rootForm(8)
            .includedForm(9).parents(8)
            .includedForm(10).parents(1, 8)
            .includedForm(11).parents(9)
            .includedForm(12).parents(11, 4)
            .build()

        val processedIndices = mutableListOf<Int>()
        FormGraphSearch.processAllRelatedForms(fixture.project, form(5), false) {
            processedIndices += index(it)
            true
        }

        processedIndices.checkIndicesProcessedOnlyOnce(1, 2, 3, 4, 6, 7, 10, 12)
    }

    @Test
    fun `findAllRelatedForms - start from parent, all included forms are found including self`() {
        graph()
            .rootForm(1)
            .includedForm(2).parents(1)
            .includedForm(3).parents(1)
            .includedForm(4).parents(3)
            .build()

        FormGraphSearch.findAllRelatedForms(fixture.project, form(1), true)
            .checkIndices(1, 2, 3, 4)
    }

    @Test
    fun `findAllRelatedForms - start from parent, all included forms are found excluding self`() {
        graph()
            .rootForm(1)
            .includedForm(2).parents(1)
            .includedForm(3).parents(1)
            .includedForm(4).parents(3)
            .build()

        FormGraphSearch.findAllRelatedForms(fixture.project, form(1), false)
            .checkIndices(2, 3, 4)
    }

    @Test
    fun `processAllRelatedForms - start from parent, all included forms are found including self`() {
        graph()
            .rootForm(1)
            .includedForm(2).parents(1)
            .includedForm(3).parents(1)
            .includedForm(4).parents(3)
            .build()

        val processedIndices = mutableListOf<Int>()
        FormGraphSearch.processAllRelatedForms(fixture.project, form(1), true) {
            processedIndices += index(it)
            true
        }

        processedIndices.checkIndicesProcessedOnlyOnce(1, 2, 3, 4)
    }

    @Test
    fun `processAllRelatedForms - start from parent, all included forms are found excluding self`() {
        graph()
            .rootForm(1)
            .includedForm(2).parents(1)
            .includedForm(3).parents(1)
            .includedForm(4).parents(3)
            .build()

        val processedIndices = mutableListOf<Int>()
        FormGraphSearch.processAllRelatedForms(fixture.project, form(1), false) {
            processedIndices += index(it)
            true
        }

        processedIndices.checkIndicesProcessedOnlyOnce(2, 3, 4)
    }

    private fun index(form: VirtualFile) =
        context.filter { (_, file) -> file == form }.first().key

    private fun List<Int>.checkIndicesProcessedOnlyOnce(vararg expectedIndices: Int) {
        assertEquals(
            size,
            distinct().size,
            "Some forms were processed multiple times. " +
                "Processed form indices: ${joinToString()}"
        )
        assertEquals(
            size,
            expectedIndices.size,
            "Number of processed forms differs from expected size. " +
                "Processed form indices: ${joinToString()}"
        )
        expectedIndices.forEach {
            assertTrue(it in this, "Index $it is not present in the result")
        }
    }

    private fun Set<VirtualFile>.checkIndices(vararg expectedIndices: Int) {
        val foundFormsIndices = map { index(it) }
        assertEquals(
            size,
            expectedIndices.size,
            "Number of found forms differs from expected size. " +
                "Found form indices: ${foundFormsIndices.joinToString()}"
        )
        expectedIndices.forEach {
            assertTrue(form(it) in this, "Index $it is not present in the result")
        }
    }

    private fun Set<VirtualFile>.checkEmpty() {
        assertTrue(isEmpty(), "Expected no results, but found $size")
    }

    private fun form(index: Int) = context.getValue(index)

    private fun graph(): FormGraphBuilder {
        if (context.isNotEmpty()) {
            throw IllegalStateException("Form graph should only be built once per test case")
        }
        return FormGraphBuilder(fixture)
    }

    private inner class FormGraphBuilder(private val fixture: CodeInsightTestFixture) {

        private val formName = "form"
        private val rootFormModule = "module"
        private val includedFormRelativePath = "dir"
        private val jsonIncludePrefix = "json://includes/forms/$includedFormRelativePath/"

        private val rootFormIndices = mutableSetOf<Int>()
        private val includedFormIndexToParentsMap = mutableMapOf<Int, Set<Int>>()

        fun rootForm(index: Int): FormGraphBuilder {
            validateIndexUniqueness(index)
            rootFormIndices += index
            return this
        }

        fun includedForm(index: Int): IncludedFormNode {
            return IncludedFormNode(index, this)
        }

        fun build() {
            validateParentIndices()
            rootFormIndices.forEach {
                val formText = createFormText(it)
                val form = fixture.createForm(
                    "$formName$it",
                    rootFormModule,
                    formText
                ).virtualFile
                context += it to form
            }
            includedFormIndexToParentsMap.keys.map {
                val formText = createFormText(it)
                val form = fixture.createIncludedForm(
                    "$formName$it",
                    includedFormRelativePath,
                    formText
                ).virtualFile
                context += it to form
            }
        }

        private fun findChildIndicesFor(index: Int): Set<Int> {
            val childIndices = mutableSetOf<Int>()
            includedFormIndexToParentsMap.forEach { (currentIndex, parents) ->
                if (index in parents) {
                    childIndices += currentIndex
                }
            }
            return childIndices
        }

        private fun createFormText(index: Int): String {
            val childIndices = findChildIndicesFor(index)
            val jsonIncludes = childIndices.joinToString {
                "\"$jsonIncludePrefix$formName$it.json\""
            }
            return "{\"includes\":[$jsonIncludes]}"
        }

        private fun validateIndexUniqueness(index: Int) {
            if(index in rootFormIndices || index in includedFormIndexToParentsMap.keys) {
                throw IllegalArgumentException("Index $index is already used")
            }
        }

        private fun validateParentIndices() {
            val parentIndices = includedFormIndexToParentsMap.values.flatten().distinct()
            val allIndices = rootFormIndices + includedFormIndexToParentsMap.keys
            parentIndices.forEach {
                if (it !in allIndices) {
                    throw IllegalStateException(
                        "Index $it is set as parent, but no form with this index was created"
                    )
                }
            }
        }

        inner class IncludedFormNode(private val index: Int, private val builder: FormGraphBuilder) {

            fun parents(vararg indices: Int): FormGraphBuilder {
                builder.validateIndexUniqueness(index)
                builder.includedFormIndexToParentsMap += index to indices.toSet()
                return builder
            }

        }

    }

}