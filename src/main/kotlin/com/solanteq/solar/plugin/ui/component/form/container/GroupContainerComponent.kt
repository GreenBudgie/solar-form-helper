package com.solanteq.solar.plugin.ui.component.form.container

import com.solanteq.solar.plugin.element.FormGroup
import com.solanteq.solar.plugin.ui.component.form.GroupComponent
import com.solanteq.solar.plugin.ui.component.util.refreshAll
import com.solanteq.solar.plugin.ui.editor.FormEditor

class GroupContainerComponent(
    editor: FormEditor,
    groups: List<FormGroup>
) : VerticalGridContainerComponent<FormGroup>(editor, groups, GROUP_INSET) {

    override fun createComponent(element: FormGroup) = GroupComponent(editor, element)

    override fun refresh() {
        formComponents.refreshAll()
    }

    companion object {

        const val GROUP_INSET = 17

    }

}