package com.solanteq.solar.plugin.l10n.action

import com.intellij.execution.target.textFieldWithBrowseTargetButton
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text
import com.intellij.ui.dsl.validation.Level
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.file.L10nFileType
import com.solanteq.solar.plugin.l10n.L10nLocale
import java.awt.Dimension
import javax.swing.JComponent

class CreateFormL10nDialog(project: Project, private val element: FormLocalizableElement<*>) : DialogWrapper(project) {

    init {
        title = "Generate Localization"
        init()
    }

    override fun getInitialSize(): Dimension {
        return Dimension(800, 600)
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            element.l10nKeys.forEach { key ->
                group {
                    row("Key:") {
                        textField()
                            .text(key)
                            .columns(COLUMNS_LARGE)
                            .cellValidation {
                                addInputRule("Key modification is not recommended", Level.WARNING) {
                                    it.text != key
                                }
                            }
                    }
                    indent {
                        L10nLocale.entries.forEach { locale ->
                            row("${locale.displayName}:") {
                                textField()
                                    .columns(COLUMNS_LARGE)
                            }
                        }
                    }
                    row {
                        label("File:")

                        cell()
                        textFieldWithBrowseButton(
                            fileChooserDescriptor = FileChooserDescriptorFactory
                                .createSingleFileDescriptor(L10nFileType)
                                .withTitle("Select L10n File")
                                .withRoots(element.project.guessProjectDir()),
                            project = element.project,
                        )
                    }
                }
            }
        }
    }

}