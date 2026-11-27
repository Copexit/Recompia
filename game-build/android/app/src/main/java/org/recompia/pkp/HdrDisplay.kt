// SPDX-License-Identifier: GPL-3.0-or-later
package org.recompia.pkp

import android.content.Context
import android.os.Build
import android.view.Display
import java.io.File
import org.recompia.pkp.ui.Ini

/** The active display must accept the HDR10 signal used by the game. */
internal fun hdrDisplayCapabilities(display: Display?): Display.HdrCapabilities? {
    val hdr = display?.hdrCapabilities ?: return null
    @Suppress("DEPRECATION")
    val types = if (Build.VERSION.SDK_INT >= 34) display.mode.supportedHdrTypes else hdr.supportedHdrTypes
    return hdr.takeIf {
        types.any { type ->
            type == Display.HdrCapabilities.HDR_TYPE_HDR10 || type == Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS
        }
    }
}


internal fun reportedHdrPeak(hdr: Display.HdrCapabilities): Int? =
    hdr.desiredMaxLuminance.takeIf { it in 200f..10000f }?.toInt()

/** Earlier Android builds wrote Auto even when the player never selected HDR. */
internal fun migrateAndroidHdrDefault(context: Context) {
    if (!Storage.hasAllFilesAccess()) return
    val marker = File(context.filesDir, "hdr-opt-in-migrated")
    if (marker.exists()) return
    val controls = Paths.controlsIni
    val ini = if (controls.exists()) Ini.parse(controls.readText()) else Ini.parse("")
    if (ini.get("hdr", "mode").equals("auto", ignoreCase = true)) {
        ini.set("hdr", "mode", "off")
        ini.save(controls)
    }
    marker.createNewFile()
}
