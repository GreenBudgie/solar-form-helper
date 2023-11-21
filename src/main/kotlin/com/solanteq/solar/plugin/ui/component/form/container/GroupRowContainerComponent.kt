package com.solanteq.solar.plugin.ui.component.form.container

import com.solanteq.solar.plugin.element.FormGroupRow
import com.solanteq.solar.plugin.ui.component.form.GroupRowComponent
import com.solanteq.solar.plugin.ui.component.util.refreshAll
import com.solanteq.solar.plugin.ui.editor.FormEditor

class GroupRowContainerComponent(
    editor: FormEditor,
    groupRows: List<FormGroupRow>
) : VerticalGridContainerComponent<FormGroupRow>(editor, groupRows, GroupContainerComponent.GROUP_INSET) {

    override fun createComponent(element: FormGroupRow) = GroupRowComponent(editor, element)

    override fun refresh() {
        formComponents.refreshAll()
    }

}