package com.solanteq.solar.plugin.l10n.editor

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty

/**
 * Determines the position in l10n [file] in which new generated l10n property can be placed.
 *
 * @param file L10n file
 * @param anchorProperty Anchor property
 * @param position Where to place the property: before or after [anchorProperty].
 * If [anchorProperty] is null, [PlacementPosition.BEFORE] means "place it at the start of the file", and
 * [PlacementPosition.AFTER] means "place it at the end of the file".
 */
@ConsistentCopyVisibility
data class L10nPlacement private constructor(
    val file: JsonFile,
    val anchorProperty: JsonProperty?,
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