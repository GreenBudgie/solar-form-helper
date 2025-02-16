package com.solanteq.solar.plugin.l10n.action

import com.intellij.ide.util.TreeFileChooser
import com.intellij.ide.util.TreeFileChooserFactory
import com.intellij.lang.properties.PropertiesImplUtil
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.ui.GuiUtils
import com.intellij.ui.TextFieldWithHistory
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.util.minimumHeight
import com.intellij.ui.util.minimumWidth
import com.intellij.ui.util.preferredWidth
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.file.L10nFileType
import com.solanteq.solar.plugin.l10n.L10nLocale
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import java.awt.Component
import java.awt.Dimension
import javax.swing.DefaultListCellRenderer
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.ListCellRenderer

class EditFormL10nDialog(
    private val project: Project,
    private val element: FormLocalizableElement<*>,
) : DialogWrapper(project) {

    private val fileChooserFields = mutableMapOf<L10nKey, TextFieldWithHistory>()

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
                        val key = L10nKey(key, locale)
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
                                    cellHasFocus: Boolean
                                ): Component? {
                                    super.getListCellRendererComponent(
                                        list,
                                        value,
                                        index,
                                        isSelected,
                                        cellHasFocus
                                    )

                                    val file = getFile(key)?.toPsiFile(project)
                                    if (file == null) {
                                        text = "No file selected"
                                        return this
                                    }

                                    text = file.name
                                    icon = file.getIcon(0)

                                    return this
                                }

                            }
                            fileChooserFields += key to fileChooserField

                            val fileBrowser = GuiUtils.constructFieldWithBrowseButton(fileChooserField) {
                                val fileChooser = TreeFileChooserFactory.getInstance(project).createFileChooser(
                                    "Choose Localization File",
                                    getFile(key)?.toPsiFile(project),
                                    L10nFileType
                                ) { file -> L10nLocale.getByFile(file) == locale }
                                fileChooser.showDialog()
                                val selectedFile = fileChooser.selectedFile
                                if (selectedFile != null) {
                                    val fileChooserField = fileChooserFields.getValue(key)
                                    val selectedPath = FileUtil.toSystemDependentName(selectedFile.virtualFile.path)
                                    fileChooserField.text = selectedPath
                                    fileChooserField.selectedItem = selectedPath
                                    fileChooserField.addCurrentTextToHistory()
                                }
                            }
                            fileBrowser.minimumSize = Dimension(250, fileBrowser.minimumHeight)

                            cell(fileBrowser)
                                .widthGroup("fileBrowser")

                            expandableTextField({ mutableListOf(it) })
                                .columns(COLUMNS_LARGE * 2)
                        }
                    }
                }
            }
        }
    }

    private fun getFile(key: L10nKey): VirtualFile? {
        val path = getFilePath(key) ?: return null
        return LocalFileSystem.getInstance().findFileByPath(path)
    }

    private fun getFilePath(key: L10nKey): String? {
        val fileChooserField = fileChooserFields.getValue(key)
        return fileChooserField.selectedItem as String?
    }

    private data class L10nKey(
        val key: String,
        val locale: L10nLocale,
    )

}