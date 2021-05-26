/*
 * InfoItem.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.info.model

import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.ui.linkpreview.LPLinkMetadata
import java.io.Serializable

enum class CelestiaAction(val value: Int) : Serializable {
    GoTo(103),
    GoToSurface(7),
    Center(99),
    PlayPause(32),
    Reverse(106),
    Slower(107),
    Faster(108),
    CurrentTime(33),
    SyncOrbit(121),
    Lock(58),
    Chase(34),
    Follow(102),
    RunDemo(100),
    CancelScript(27),
    Home(104);

    val title: String
        get() {
            val orig = when (this) {
                GoTo -> "Go"
                GoToSurface -> "Land"
                PlayPause -> "Resume/Pause"
                CurrentTime -> "Current Time"
                SyncOrbit -> "Sync Orbit"
                RunDemo -> "Run Demo"
                CancelScript -> "Cancel Script"
                Home -> "Home (Sol)"
                Reverse -> "Reverse Time"
                else -> this.toString()
            }
            return CelestiaString(orig, "")
        }

    companion object {
        val allActions: List<CelestiaAction>
            get() = listOf(
                GoTo, Center,Follow, Chase, SyncOrbit, Lock, GoToSurface
            )
    }
}

interface InfoItem

interface InfoActionItem : InfoItem {
    val title: String

    companion object {
        val infoActions: List<InfoActionItem>
            get() = listOf(InfoSelectActionItem()) + CelestiaAction.allActions.map { return@map InfoNormalActionItem(it) }
    }
}

class InfoNormalActionItem(val item: CelestiaAction) : InfoActionItem {
    override val title: String
        get() = item.title
}
class InfoSelectActionItem : InfoActionItem {
    override val title: String
        get() = CelestiaString("Select", "")
}
class InfoWebActionItem : InfoActionItem {
    override val title: String
        get() = CelestiaString("Web Info", "")
}
class SubsystemActionItem : InfoActionItem {
    override val title: String
        get() = CelestiaString("Subsystem", "")
}
class AlternateSurfacesItem : InfoActionItem {
    override val title: String
        get() = CelestiaString("Alternate Surfaces", "")
}
class MarkItem : InfoActionItem {
    override val title: String
        get() = CelestiaString("Mark", "")
}
class InfoDescriptionItem(val name: String, val overview: String, val hasWebInfo: Boolean, val hasAlternateSurfaces: Boolean) : InfoItem, Serializable
class InfoMetadataItem(val metadata: LPLinkMetadata) : InfoItem, Serializable