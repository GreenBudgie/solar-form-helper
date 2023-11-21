package com.solanteq.solar.plugin.ui.component.util

interface Refreshable {

    /**
     * Refreshes the form component.
     * This method is called whenever the component needs to be updated
     * due to changes in form preview configuration.
     * This method **is not** called when form PSI changes.
     * Most common actions in this method are:
     * - Changing component visibility when related expression changes
     * - Changing component style when related expression changes
     * - Refreshing its child components
     * - Validate/repaint calls
     */
    fun refresh()

}

/**
 * If not null, performs [Refreshable.refresh] on all provided components
 */
fun List<Refreshable>?.refreshAll() = this?.forEach { it.refresh() }