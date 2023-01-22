package com.solanteq.solar.plugin.l10n

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonPsiUtil
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.project.Project
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import com.intellij.util.ProcessingContext
import com.solanteq.solar.plugin.file.L10nFileType
import com.solanteq.solar.plugin.search.FormModuleSearch
import com.solanteq.solar.plugin.util.withCondition
import org.jetbrains.kotlin.idea.core.util.toPsiDirectory

class L10nCompletionContributor : CompletionContributor() {

    private val pattern = PlatformPatterns.psiElement().inFile(
        PlatformPatterns.psiFile().withFileType(StandardPatterns.`object`(L10nFileType))
    ).withCondition("isParentPropertyKey") {
        val parent = it.parent as? JsonStringLiteral ?: return@withCondition false
        return@withCondition JsonPsiUtil.isPropertyKey(parent)
    }

    init {
        this.extend(
            CompletionType.BASIC,
            pattern,
            ModuleCompletionProvider()
        )
    }

    class ModuleCompletionProvider : CompletionProvider<CompletionParameters>() {

        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            val propertyKey = parameters.position.parent as? JsonStringLiteral ?: return
            val value = propertyKey.value
            val delimiterPositions = value.mapIndexedNotNull { index, char ->
                if(char != '.') return@mapIndexedNotNull null
                return@mapIndexedNotNull index
            }
            val firstDelimiterPosition = delimiterPositions.getOrNull(0)
            val secondDelimiterPosition = delimiterPositions.getOrNull(1)
            val offsetInElementValue = parameters.offset - propertyKey.textOffset - 1

            if(firstDelimiterPosition == null || offsetInElementValue < firstDelimiterPosition) {
                addModuleCompletions(propertyKey.project, result)
            } else {
                if(secondDelimiterPosition == null || offsetInElementValue < secondDelimiterPosition) {
                    val prefix = value.substring(firstDelimiterPosition + 1, offsetInElementValue)
                    addTypeCompletions(result, prefix)
                }
            }
        }

        private fun addModuleCompletions(
            project: Project,
            result: CompletionResultSet
        ) {
            val allModules = FormModuleSearch.findProjectRootFormModules(project)
            val distinctModulesByName = allModules
                .distinctBy { it.name }
            val psiDirectories = distinctModulesByName.mapNotNull {
                it.toPsiDirectory(project)
            }
            val lookupElements = psiDirectories.map {
                LookupElementBuilder
                    .create(it.name)
                    .withIcon(it.getIcon(0))
            }

            result.addAllElements(lookupElements)
        }

        private fun addTypeCompletions(
            result: CompletionResultSet,
            prefix: String
        ) {
            val lookups = listOf(
                LookupElementBuilder.create("form"),
                LookupElementBuilder.create("dd")
            )
            result.withPrefixMatcher(prefix).addAllElements(lookups)
        }

    }
}