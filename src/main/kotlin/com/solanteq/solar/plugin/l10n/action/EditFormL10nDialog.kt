package com.solanteq.solar.plugin.l10n.action

import com.intellij.ide.util.TreeFileChooserFactory
import com.intellij.json.psi.JsonFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.GuiUtils
import com.intellij.ui.JBColor
import com.intellij.ui.TextFieldWithHistory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text
import com.intellij.ui.util.minimumHeight
import com.solanteq.solar.plugin.asset.Icons
import com.solanteq.solar.plugin.element.FormRootFile
import com.solanteq.solar.plugin.element.base.FormLocalizableElement
import com.solanteq.solar.plugin.file.L10nFileType
import com.solanteq.solar.plugin.l10n.FormL10n
import com.solanteq.solar.plugin.l10n.FormL10nEntry
import com.solanteq.solar.plugin.l10n.L10nLocale
import com.solanteq.solar.plugin.l10n.editor.FormL10nEditor
import com.solanteq.solar.plugin.l10n.search.FormL10nSearch
import org.jetbrains.kotlin.idea.base.util.projectScope
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import java.awt.Component
import java.awt.Dimension
import javax.swing.*

class EditFormL10nDialog(
    private val project: Project,
    private val element: FormLocalizableElement<*>,
) : DialogWrapper(project) {

    val l10nData = mutableMapOf<FormL10nEntry, L10nData>()

    init {
        title = "Edit Localization"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val entriesWithAllLocales = element.l10nEntries
            .groupBy { it.key }
            .map { (key, entries) ->
                L10nEntryAllLocales(
                    entries.first().rootForm,
                    key,
                    entries.map { it.locale }
                )
            }
        return panel {
            entriesWithAllLocales.forEach { (rootForm, key, locales) ->
                val formFullName = rootForm.fullName ?: "Unknown form"
                group(JBLabel(formFullName, Icons.ROOT_FORM_ICON, SwingConstants.LEFT)) {
                    row("Key: ") {
                        label(key).bold()
                    }
                    locales.forEach { locale ->
                        val l10nEntry = FormL10nEntry(rootForm, key, locale)
                        val l10n = FormL10nSearch.search(project, l10nEntry)
                            .inScope(project.projectScope())
                            .findFirstObject()
                        var initialL10nFile = if (l10n?.file == null) {
                            val bestPlacement = FormL10nEditor.findBestPlacement(element, l10nEntry)
                             bestPlacement?.file
                        } else {
                            l10n.file
                        }
                        row(locale.displayName) {
                            val fileChooserField = TextFieldWithHistory()
                            fileChooserField.setHistorySize(-1)
                            fileChooserField.setEditable(false)
                            if (l10n != null) {
                                fileChooserField.isEnabled = false
                            }
                            if (initialL10nFile != null) {
                                val selectedPath = FileUtil.toSystemDependentName(initialL10nFile.virtualFile.path)
                                fileChooserField.text = selectedPath
                                fileChooserField.selectedItem = selectedPath
                                fileChooserField.addCurrentTextToHistory()
                            }
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

                                    val file = getFile(value as String?)
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
                                    getFile(fileChooserField) ?: initialL10nFile,
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
                            if (l10n != null) {
                                fileBrowser.components.last().isEnabled = false
                            }

                            cell(fileBrowser)
                                .widthGroup("fileBrowser")

                            val valueField = expandableTextField({ mutableListOf(it) })
                                .onChanged {
                                    val data = l10nData[l10nEntry] ?: return@onChanged
                                    var updatedStatus = data.currentStatus
                                    if (it.text.isBlank()) {
                                        updatedStatus = if (data.originalL10n != null) {
                                            L10nEntryStatus.DELETED
                                        } else {
                                            L10nEntryStatus.ABSENT
                                        }
                                    } else {
                                        updatedStatus = if (data.originalL10n != null) {
                                            if (it.text == data.originalL10n.value) {
                                                L10nEntryStatus.PRESENT
                                            } else {
                                                L10nEntryStatus.MODIFIED
                                            }
                                        } else {
                                            L10nEntryStatus.NEW
                                        }
                                    }
                                    if (updatedStatus != data.currentStatus) {
                                        setStatus(data, updatedStatus)
                                    }
                                }
                                .columns(COLUMNS_LARGE * 2)
                            val initialValue = l10n?.value
                            if (initialValue != null) {
                                valueField.text(initialValue)
                            }

                            val status = if (l10n != null) {
                                L10nEntryStatus.PRESENT
                            } else {
                                L10nEntryStatus.ABSENT
                            }

                            val statusLabel = label(status.text)
                            statusLabel.component.foreground = status.color

                            l10nData += l10nEntry to L10nData(
                                fileChooserField = fileChooserField,
                                valueField = valueField.component,
                                statusLabel = statusLabel.component,
                                originalL10n = l10n,
                                originalStatus = status,
                                currentStatus = status
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setStatus(data: L10nData, newStatus: L10nEntryStatus) {
        data.statusLabel.text = newStatus.text
        data.statusLabel.foreground = newStatus.color
        data.currentStatus = newStatus

        val isAnyL10nModified = l10nData.values.any { it.currentStatus.modified }
        val isAllFilesSelected = l10nData.values.all { it.fileChooserField.selectedItem != null }
        isOKActionEnabled = isAnyL10nModified && isAllFilesSelected
    }

    fun getFile(fileChooserField: TextFieldWithHistory): JsonFile? {
        val path = fileChooserField.selectedItem as String? ?: return null
        return getFile(path)
    }

    fun getFile(path: String?): JsonFile? {
        path ?: return null
        return LocalFileSystem.getInstance().findFileByPath(path)?.toPsiFile(project) as? JsonFile
    }

    data class L10nData(
        val fileChooserField: TextFieldWithHistory,
        val valueField: ExpandableTextField,
        val statusLabel: JLabel,
        val originalL10n: FormL10n?,
        val originalStatus: L10nEntryStatus,
        var currentStatus: L10nEntryStatus,
    )

    enum class L10nEntryStatus(val text: String, val color: JBColor, val modified: Boolean) {

        PRESENT("present", JBColor.BLACK, false),
        ABSENT("absent", JBColor.DARK_GRAY, false),
        NEW("new", JBColor.GREEN, true),
        DELETED("deleted", JBColor.RED, true),
        MODIFIED("modified", JBColor.BLUE, true),

    }

    private data class L10nEntryAllLocales(
        val rootForm: FormRootFile,
        val key: String,
        val locales: List<L10nLocale>
    )

}