package com.solanteq.solar.plugin.element.expression

/**
 * Enum of properties that may contain a reference to expression.
 */
enum class ExpressionType(val key: String) {

    REQUIRED_WHEN("requiredWhen"),
    VISIBLE_WHEN("visibleWhen"),
    EDITABLE_WHEN("editableWhen"),
    REMOVABLE_WHEN("removableWhen"),
    EDIT_MODE_WHEN("editModeWhen"),
    SUCCESS("success"),
    WARNING("warning"),
    ERROR("error"),
    INFO("info"),
    MUTED("muted");

    companion object {

        val expressionProperties = entries.map { it.key }

    }

}