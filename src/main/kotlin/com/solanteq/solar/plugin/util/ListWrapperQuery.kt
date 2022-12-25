package com.solanteq.solar.plugin.util

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.AbstractQuery
import com.intellij.util.Processor

/**
 * A query that helps to process pre-calculated values of any type in a safe read action
 */
class ListWrapperQuery<T>(
    private val elementsToProcess: List<T>
) : AbstractQuery<T>() {

    override fun processResults(consumer: Processor<in T>): Boolean {
        return runReadAction {
            elementsToProcess.forEach {
                ProgressManager.checkCanceled()
                if(!consumer.process(it)) {
                    return@runReadAction false
                }
            }
            return@runReadAction true
        }
    }

}