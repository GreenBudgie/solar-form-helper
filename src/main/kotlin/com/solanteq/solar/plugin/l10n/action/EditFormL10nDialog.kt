package com.solanteq.solar.plugin.l10n.action

import com.intellij.ide.util.TreeFileChooserFactory
import com.intellij.json.psi.JsonFile
import com.intellij.openapi.application.readAction
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.platform.util.progress.reportProgress
import com.intellij.profile.codeInspection.ui.addScrollPaneIfNecessary
import com.intellij.psi.PsiFile
import com.intellij.ui.GuiUtils
import com.intellij.ui.JBColor
import com.intellij.ui.TextFieldWithHistory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.util.minimumHeight
import com.solanteq.solar.plugin.asset.Icons
import com.solanteq.solar.plugin.bundle.SolarBundle
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

    private val preparedData: Map<FormL10nEntry, PreparedL10nData>

    private var synchronizeL10nsCheckbox: JCheckBox? = null

    init {
        title = SolarBundle.message("dialog.l10n.edit.title")

        preparedData = runWithModalProgressBlocking(
            project,
            SolarBundle.message("dialog.l10n.edit.finding.files")
        ) { prepareData() }

        init()
    }

    private suspend fun prepareData(): Map<FormL10nEntry, PreparedL10nData> {
        val l10nEntries = readAction { element.l10nEntries }
        return reportProgress(l10nEntries.size) { reporter ->
            l10nEntries.associateWith { entry ->
                checkCanceled()
                reporter.itemStep(SolarBundle.message("dialog.l10n.edit.processing.l10n.entry", entry.toString())) {
                    readAction {
                        val originalL10n = FormL10nSearch.search(project, entry)
                            .inScope(project.projectScope())
                            .findFirstObject()

                        val initialL10nFile = if (originalL10n?.file == null) {
                            val bestPlacement = FormL10nEditor.findBestPlacement(element, entry)
                            bestPlacement?.file
                        } else {
                            originalL10n.file
                        }

                        PreparedL10nData(
                            initialL10nFile,
                            initialL10nFile?.getIcon(0),
                            originalL10n
                        )
                    }
                }
            }
        }
    }


    fun getFile(fileChooserField: TextFieldWithHistory): JsonFile? {
        val path = fileChooserField.selectedItem as String? ?: return null
        return getFile(path)
    }

    fun getFile(path: String?): JsonFile? {
        path ?: return null
        return LocalFileSystem.getInstance().findFileByPath(path)?.toPsiFile(project) as? JsonFile
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
        return addScrollPaneIfNecessary(panel { constructPanel(entriesWithAllLocales) })
    }

    private fun Panel.constructPanel(entriesWithAllLocales: List<L10nEntryAllLocales>) {
        if (entriesWithAllLocales.size > 1) {
            row {
                synchronizeL10nsCheckbox = checkBox(SolarBundle.message("dialog.l10n.edit.synchronize.l10ns"))
                    .onChanged { checkbox ->
                        l10nData.ifEmpty { return@onChanged }

                        if (!checkbox.isSelected) {
                            return@onChanged
                        }

                        val entry = entriesWithAllLocales.first()
                        entry.locales.forEach { locale ->
                            synchronizeLocalizations(FormL10nEntry(entry.rootForm, entry.key, locale))
                        }
                    }.selected(areAllL10nValuesEqual())
                    .component
            }
        }

        entriesWithAllLocales.forEach { (rootForm, key, locales) ->
            val formFullName = rootForm.fullName ?: SolarBundle.message("dialog.l10n.edit.unknown.form")
            group(JBLabel(formFullName, Icons.ROOT_FORM_ICON, SwingConstants.LEFT)) {
                l10nEntryGroup(rootForm, key, locales)
            }
        }
    }

    private fun areAllL10nValuesEqual(): Boolean {
        val l10nEntriesByLocale = preparedData.keys.groupBy { it.locale }
        return l10nEntriesByLocale.values.all { keysByLocale ->
            val l10nValues = keysByLocale.map { keyByLocale ->
                preparedData.getValue(keyByLocale).originalL10n?.value
            }

            l10nValues.distinct().size <= 1
        }
    }

    private var synchronizingEntry: FormL10nEntry? = null

    private fun synchronizeLocalizations(withEntry: FormL10nEntry) {
        if (synchronizingEntry == withEntry) {
            // Prevents infinite recursion, since setting the text fires onChange event
            // If you stumble upon this code and know a better solution - please fix it
            return
        }

        val l10nDataToSynchronize = l10nData[withEntry] ?: return
        val valueToSynchronize = l10nDataToSynchronize.valueField.text
        l10nData
            .filterKeys { it != withEntry }
            .filterKeys { it.locale == withEntry.locale }
            .forEach {
                synchronizingEntry = it.key
                it.value.valueField.text = valueToSynchronize
                synchronizingEntry = null
            }
    }

    private fun Panel.l10nEntryGroup(
        rootForm: FormRootFile,
        key: String,
        locales: List<L10nLocale>,
    ) {
        row(SolarBundle.message("dialog.l10n.edit.key")) {
            label(key).bold()
        }
        locales.forEach { locale ->
            rowForLocale(rootForm, key, locale)
        }
    }

    private fun Panel.rowForLocale(
        rootForm: FormRootFile,
        key: String,
        locale: L10nLocale,
    ) {
        val l10nEntry = FormL10nEntry(rootForm, key, locale)
        val preparedL10nData = preparedData.getValue(l10nEntry)
        val originalL10n = preparedL10nData.originalL10n

        row(locale.displayName) {
            val fileChooserField = constructFileChooserField(originalL10n, preparedL10nData.file)
            val fileBrowser = constructFileBrowser(fileChooserField, preparedL10nData.file, locale, originalL10n)
            cell(fileBrowser)

            val valueField = constructL10nValueField(l10nEntry, originalL10n)

            val originalStatus = if (originalL10n != null) {
                L10nEntryStatus.PRESENT
            } else {
                L10nEntryStatus.ABSENT
            }

            val statusLabel = label(originalStatus.text)
            statusLabel.component.foreground = originalStatus.color
            statusLabel.component.minimumSize = Dimension(80, statusLabel.component.minimumHeight)

            l10nData += l10nEntry to L10nData(
                fileChooserField = fileChooserField,
                valueField = valueField.component,
                statusLabel = statusLabel.component,
                originalL10n = originalL10n,
                originalStatus = originalStatus,
                currentStatus = originalStatus
            )
        }
    }

    private fun Row.constructL10nValueField(
        l10nEntry: FormL10nEntry,
        originalL10n: FormL10n?,
    ): Cell<ExpandableTextField> {
        val valueField = expandableTextField({ mutableListOf(it) })
            .onChanged {
                handleL10nValueTextChange(l10nEntry, it.text)
            }
            .columns(COLUMNS_LARGE * 2)

        val initialValue = originalL10n?.value
        if (initialValue != null) {
            valueField.text(initialValue)
        }

        return valueField
    }

    private fun handleL10nValueTextChange(
        l10nEntry: FormL10nEntry,
        text: String,
    ) {
        val data = l10nData[l10nEntry] ?: return

        if (synchronizeL10nsCheckbox?.isSelected == true) {
            synchronizeLocalizations(l10nEntry)
        }

        if (text.isBlank()) {
            if (data.originalL10n != null) {
                updateStatus(data, L10nEntryStatus.DELETED)
                return
            }

            updateStatus(data, L10nEntryStatus.ABSENT)
            return
        }

        if (data.originalL10n == null) {
            updateStatus(data, L10nEntryStatus.NEW)
            return
        }

        if (text == data.originalL10n.value) {
            updateStatus(data, L10nEntryStatus.PRESENT)
            return
        }

        updateStatus(data, L10nEntryStatus.MODIFIED)
    }

    private fun constructFileChooserField(
        originalL10n: FormL10n?,
        initialL10nFile: JsonFile?,
    ): TextFieldWithHistory {
        val fileChooserField = TextFieldWithHistory()
        fileChooserField.setHistorySize(-1)
        fileChooserField.setEditable(false)

        if (originalL10n != null) {
            fileChooserField.isEnabled = false
        }

        if (initialL10nFile != null) {
            selectPath(initialL10nFile, fileChooserField)
        }

        fileChooserField.renderer = L10nFileListCellRenderer()
        return fileChooserField
    }

    private fun constructFileBrowser(
        fileChooserField: TextFieldWithHistory,
        initialL10nFile: JsonFile?,
        locale: L10nLocale,
        originalL10n: FormL10n?,
    ): JPanel {
        val fileBrowser = GuiUtils.constructFieldWithBrowseButton(fileChooserField) {
            onBrowseButtonClick(fileChooserField, initialL10nFile, locale)
        }

        fileBrowser.minimumSize = Dimension(250, fileBrowser.minimumHeight)
        if (originalL10n != null) {
            fileBrowser.components.last().isEnabled = false
        }

        return fileBrowser
    }

    private fun onBrowseButtonClick(
        fileChooserField: TextFieldWithHistory,
        initialL10nFile: JsonFile?,
        locale: L10nLocale,
    ) {
        val fileChooser = TreeFileChooserFactory.getInstance(project).createFileChooser(
            SolarBundle.message("dialog.l10n.edit.choose.file"),
            getFile(fileChooserField) ?: initialL10nFile,
            L10nFileType
        ) { L10nLocale.getByFile(it) == locale }
        fileChooser.showDialog()

        val selectedFile = fileChooser.selectedFile ?: return

        selectPath(selectedFile, fileChooserField)
    }

    private fun selectPath(selectedFile: PsiFile, fileChooserField: TextFieldWithHistory) {
        val selectedPath = FileUtil.toSystemDependentName(selectedFile.virtualFile.path)
        fileChooserField.text = selectedPath
        fileChooserField.selectedItem = selectedPath
        fileChooserField.addCurrentTextToHistory()
    }

    private fun updateStatus(data: L10nData, newStatus: L10nEntryStatus) {
        if (data.currentStatus == newStatus) {
            return
        }

        data.statusLabel.text = newStatus.text
        data.statusLabel.foreground = newStatus.color
        data.currentStatus = newStatus

        val isAnyL10nModified = l10nData.values.any { it.currentStatus.modified }
        val isAllFilesSelected = l10nData.values.all { it.fileChooserField.selectedItem != null }
        isOKActionEnabled = isAnyL10nModified && isAllFilesSelected
    }

    private inner class L10nFileListCellRenderer : DefaultListCellRenderer() {

        override fun getListCellRendererComponent(
            list: JList<*>?,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): Component {
            super.getListCellRendererComponent(
                list,
                value,
                index,
                isSelected,
                cellHasFocus
            )

            val file = getFile(value as String?)
            if (file == null) {
                text = SolarBundle.message("dialog.l10n.edit.file.not.selected")
                return this
            }

            val preparedData = preparedData.values.find { it.file == file }

            text = file.name
            icon = preparedData?.fileIcon ?: file.getIcon(0)

            return this
        }

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

        PRESENT(SolarBundle.message("l10n.entry.status.present"), JBColor.BLACK, false),
        ABSENT(SolarBundle.message("l10n.entry.status.absent"), JBColor.DARK_GRAY, false),
        NEW(SolarBundle.message("l10n.entry.status.new"), JBColor.GREEN, true),
        DELETED(SolarBundle.message("l10n.entry.status.deleted"), JBColor.RED, true),
        MODIFIED(SolarBundle.message("l10n.entry.status.modified"), JBColor.BLUE, true),

    }

    private data class L10nEntryAllLocales(
        val rootForm: FormRootFile,
        val key: String,
        val locales: List<L10nLocale>,
    )

    private data class PreparedL10nData(
        val file: JsonFile?,
        // If we don't compute file icon beforehand, we get slow operations exception
        val fileIcon: Icon?,
        val originalL10n: FormL10n?,
    )

}