package com.solanteq.solar.plugin.l10n.generator

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty

/**
 * @author nbundin
 * @since %CURRENT_VERSION%
 */
@ConsistentCopyVisibility
data class L10nPlacement private constructor(
    val file: JsonFile,
    val property: JsonProperty?,
    val position: PlacementPosition,
) {

    companion object {

        fun before(file: JsonFile, property: JsonProperty) = L10nPlacement(file, property, PlacementPosition.BEFORE)

        fun after(file: JsonFile, property: JsonProperty) = L10nPlacement(file, property, PlacementPosition.AFTER)

        fun endOfFile(file: JsonFile) = L10nPlacement(file, null, PlacementPosition.AFTER)

        fun startOfFile(file: JsonFile) = L10nPlacement(file, null, PlacementPosition.BEFORE)

    }

}

enum class PlacementPosition {
    BEFORE,
    AFTER
}