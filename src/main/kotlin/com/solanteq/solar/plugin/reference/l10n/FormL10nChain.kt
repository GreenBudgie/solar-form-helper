package com.solanteq.solar.plugin.reference.l10n

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.solanteq.solar.plugin.util.findFormByModuleAndName
import org.jetbrains.kotlin.idea.base.util.projectScope
import org.jetbrains.kotlin.idea.core.util.toPsiFile

/**
 * TODO for now, only supports form -> group -> field l10ns
 */
class FormL10nChain(
    val element: JsonStringLiteral,
    val module: String,
    private val chain: List<String>,
    private val textRangeStartIndex: Int
) {

    private val project = element.project

    val referencedForm by lazy {
        if(chain.isEmpty()) return@lazy null
        return@lazy findFormByModuleAndName(
            project,
            module,
            chain[formChainIndex],
            project.projectScope()
        )
    }

    val referencedFormPsiFile by lazy {
        referencedForm?.toPsiFile(project) as? JsonFile
    }

    val formNameReference by lazy {
        val form = referencedFormPsiFile ?: return@lazy null
        val topLevelObject = form.topLevelValue as? JsonObject ?: return@lazy null
        val nameProperty = topLevelObject.findProperty("name") ?: return@lazy null
        return@lazy nameProperty.value as? JsonStringLiteral ?: return@lazy null
    }

    val formTextRange by lazy {
        getTextRangeForChainEntryByIndex(formChainIndex)
    }

    private fun getTextRangeForChainEntryByIndex(index: Int): TextRange? {
        if(index >= chain.size) return null
        val currentLiteralLength = chain[index].length
        val prevLiteralsLength = chain.take(index).joinToString().length
        val startPos = textRangeStartIndex + prevLiteralsLength + index + 1
        val endPos = startPos + currentLiteralLength
        return TextRange(startPos, endPos)
    }

    companion object {

        /**
         * Creates new l10n chain for the given element if it's possible, or returns null
         */
        fun fromElement(element: JsonStringLiteral): FormL10nChain? {
            val textSplit = element.value.split(".")
            if(textSplit.size < 3) return null
            val l10nType = textSplit[1]
            if(l10nType != "form") return null
            val l10nModule = textSplit[0]
            val l10nChain = textSplit.drop(2)
            return FormL10nChain(element, l10nModule, l10nChain, l10nModule.length + 6)
        }

        private val formChainIndex = 0
        private val groupChainIndex = 1
        private val firstFieldChainIndex = 2

    }

}