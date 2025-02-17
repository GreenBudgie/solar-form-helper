package com.solanteq.solar.plugin.l10n.action

import com.intellij.ide.util.TreeFileChooserFactory
import com.intellij.json.psi.JsonFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.GuiUtils
import com.intellij.ui.TextFieldWithHistory
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.util.minimumHeight
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.file.L10nFileType
import com.solanteq.solar.plugin.l10n.L10nEntry
import com.solanteq.solar.plugin.l10n.L10nLocale
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import java.awt.Component
import java.awt.Dimension
import javax.swing.DefaultListCellRenderer
import javax.swing.JComponent
import javax.swing.JList

class EditFormL10nDialog(
    private val project: Project,
    private val element: FormLocalizableElement<*>,
) : DialogWrapper(project) {

    private val l10nData = mutableMapOf<L10nEntry, L10nData>()

    init {
        title = "Edit Localization"
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            element.l10nKeys.forEach { key ->
                group {
                    row("Key: ") {
                        label(key).bold()
                    }
                    L10nLocale.entries.forEach { locale ->
                        val key = L10nEntry(element.containingRootForms.first(), key, locale)
                        row(locale.displayName) {
                            icon(locale.icon)

                            val fileChooserField = TextFieldWithHistory()
                            fileChooserField.setHistorySize(-1)
                            fileChooserField.setEditable(false)
                            fileChooserField.renderer = object : DefaultListCellRenderer() {

                                override fun getListCellRendererComponent(
                                    list: JList<*>?,
                                    value: Any?,
                                    index: Int,
                                    isSelected: Boolean,
                                    cellHasFocus: Boolean,
                                ): Component? {
                                    super.getListCellRendererComponent(
                                        list,
                                        value,
                                        index,
                                        isSelected,
                                        cellHasFocus
                                    )

                                    val file = getFile(fileChooserField)
                                    if (file == null) {
                                        text = "No file selected"
                                        return this
                                    }

                                    text = file.name
                                    icon = file.getIcon(0)

                                    return this
                                }

                            }

                            val fileBrowser = GuiUtils.constructFieldWithBrowseButton(fileChooserField) {
                                val fileChooser = TreeFileChooserFactory.getInstance(project).createFileChooser(
                                    "Choose Localization File",
                                    getFile(fileChooserField),
                                    L10nFileType
                                ) { file -> L10nLocale.getByFile(file) == locale }
                                fileChooser.showDialog()
                                val selectedFile = fileChooser.selectedFile
                                if (selectedFile != null) {
                                    val fileChooserField = fileChooserField
                                    val selectedPath = FileUtil.toSystemDependentName(selectedFile.virtualFile.path)
                                    fileChooserField.text = selectedPath
                                    fileChooserField.selectedItem = selectedPath
                                    fileChooserField.addCurrentTextToHistory()
                                }
                            }
                            fileBrowser.minimumSize = Dimension(250, fileBrowser.minimumHeight)

                            cell(fileBrowser)
                                .widthGroup("fileBrowser")

                            val valueField = expandableTextField({ mutableListOf(it) })
                                .columns(COLUMNS_LARGE * 2)
                                .component

                            l10nData += key to L10nData(
                                fileChooserField,
                                valueField
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getFile(fileChooserField: TextFieldWithHistory): JsonFile? {
        val path = fileChooserField.selectedItem as String? ?: return null
        return LocalFileSystem.getInstance().findFileByPath(path)?.toPsiFile(project) as? JsonFile
    }

    private data class L10nData(
        var fileChooserField: TextFieldWithHistory,
        var valueField: ExpandableTextField,
    )

}