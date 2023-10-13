package com.solanteq.solar.plugin.performance

import com.intellij.util.FileContentUtilCore
import com.solanteq.solar.plugin.base.JavaPluginTestBase
import com.solanteq.solar.plugin.base.configureByForms
import com.solanteq.solar.plugin.base.createFormAndConfigure
import com.solanteq.solar.plugin.l10n.L10nTestUtils
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * Performance benchmark isn't meant to be a test. This is just a playground with execution time,
 * and it should always be @[Disabled] in the final build.
 */
@Disabled
class PerformanceBenchmark : JavaPluginTestBase()  {

    override fun getTestDataSuffix() = "l10n/realFields"

    @Test
    fun `test parent performance`() {
        fixture.createFormAndConfigure("testForm", "abc", """
            {
              "groupRows": [
                {
                  "groups": [
                    {
                      "name": "<caret>group1"
                    },
                    "json://includes/forms/test2/group2.json"
                  ]
                },
                "json-flat://includes/forms/test3/groups.json"
              ]
            }
        """.trimIndent())

        val element = fixture.file.findElementAt(fixture.caretOffset)!!

        element.parent.containingFile

        runBenchmark(10000,
            { element.parent.parent.parent.parent.parent.parent.parent.parent.parent.parent },
            { FileContentUtilCore.reparseFiles(fixture.file.virtualFile) }
        )

        runBenchmark(10000,
            { element.node.treeParent.treeParent.treeParent.treeParent.treeParent.treeParent.treeParent.treeParent.treeParent.treeParent.psi },
            { FileContentUtilCore.reparseFiles(fixture.file.virtualFile) }
        )

        runBenchmark(10000,
            { element.parent.containingFile },
            { FileContentUtilCore.reparseFiles(fixture.file.virtualFile) }
        )
    }

    @Test
    fun `test l10n reference performance`() {
        fixture.configureByFiles(
            "TestService.kt",
            "TestServiceImpl.kt",
            "DataClass.kt",
            "NestedDataClass.kt",
        )

        fixture.configureByForms("fieldsForm.json", module = "test")

        L10nTestUtils.createL10nFileAndConfigure(fixture, "l10n",
            "test.form.fieldsForm.group2.realFieldWithNested.<caret>realNestedField" to "Field Name!"
        )

        val reference = fixture.file.findReferenceAt(fixture.caretOffset)!!


        // ~1s
        runBenchmark(
            10000,
            { reference.resolve() },
            { FileContentUtilCore.reparseFiles(fixture.file.virtualFile) })
    }

    private inline fun runBenchmark(crossinline action: () -> Unit) =
        runBenchmark(1_000_000, action)

    private inline fun runBenchmark(iterations: Int, crossinline action: () -> Unit) {
        val startTime = System.nanoTime()
        repeat(iterations) {
            action()
        }
        val endTime = System.nanoTime()
        val spentTimeNs = endTime - startTime
        val averagePerCallNs = spentTimeNs / iterations
        printBenchmarkResults(iterations, spentTimeNs, averagePerCallNs)
    }

    private inline fun runBenchmark(iterations: Int, crossinline action: () -> Unit, crossinline afterEach: () -> Unit) {
        val startTime = System.nanoTime()
        var afterEachOverhead = 0L
        repeat(iterations) {
            action()
            val startTimeAfter = System.nanoTime()
            afterEach()
            val endTimeAfter = System.nanoTime()
            afterEachOverhead += endTimeAfter - startTimeAfter
        }
        val endTime = System.nanoTime()
        val spentTimeNs = endTime - startTime - afterEachOverhead
        val averagePerCallNs = spentTimeNs / iterations
        printBenchmarkResults(iterations, spentTimeNs, averagePerCallNs)
    }

    private fun printBenchmarkResults(iterations: Int, spentTimeNs: Long, averagePerCallNs: Long) {
        println(
            """
                -------- BENCHMARK RESULTS ---------

                Iterations:         $iterations
                Overall time:       ${spentTimeNs.formatNs()}
                Average per call:   ${averagePerCallNs.formatNs()}
                
                ------------------------------------
            """.trimIndent()
        )
    }

    private fun Long.formatNs() = when {
        this >= 1_000_000_000 -> "${toS()}s"
        this >= 1_000_000 -> "${toMs()}ms"
        else -> "${this}ns"
    }

    private fun Long.toMs() = this / 1_000_000

    private fun Long.toS() = this / 1_000_000_000

}