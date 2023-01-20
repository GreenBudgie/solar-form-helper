package com.solanteq.solar.plugin.l10n

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.solanteq.solar.plugin.element.FormField
import com.solanteq.solar.plugin.element.FormGroup
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.element.toFormElement
import com.solanteq.solar.plugin.util.isForm
import javax.swing.JPanel

class L10nInlayProvider : InlayHintsProvider<NoSettings> {

    override val key = SettingsKey<NoSettings>("solar.l10n.inlayHint")

    override val name = "Localization string"

    override val previewText = "TODO"

    override fun createSettings() = NoSettings()

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink
    ) : InlayHintsCollector? {
        if(file.isForm()) return Collector()
        return null
    }

    override fun createConfigurable(settings: NoSettings) = object : ImmediateConfigurable {

        override fun createComponent(listener: ChangeListener) = JPanel()

    }

    private class Collector : InlayHintsCollector {

        override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
            if(element !is JsonElement) return true
            if(element !is JsonFile && element !is JsonObject) return true
            if(tryAddHint<FormField>(element, editor, sink)) return true
            if(tryAddHint<FormGroup>(element, editor, sink)) return true
            if(tryAddHint<FormRootFile>(element, editor, sink)) return true
            return true
        }

        private inline fun <reified T : FormLocalizableElement<*>> tryAddHint(
            element: JsonElement,
            editor: Editor,
            sink: InlayHintsSink
        ): Boolean {
            element.toFormElement<T>()?.let {
                addHint(it, editor, sink)
                return true
            }
            return false
        }

        private fun addHint(formElement: FormLocalizableElement<*>,
                            editor: Editor,
                            sink: InlayHintsSink) {
            if(editor !is EditorImpl) {
                return
            }
            //TODO add ru/en selection support
            val localization = formElement.localizations.firstOrNull() ?: return
            val valueElement = formElement.namePropertyValue ?: return
            val offset = valueElement.textRange.endOffset

            val factory = PresentationFactory(editor)
            val presentation = factory.smallText(localization)
            sink.addInlineElement(offset, false, presentation, true)
        }

    }

}