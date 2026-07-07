package com.typlx.keyboard

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class AutoSuggestTileService : TileService() {

    private val prefs by lazy { PreferencesManager(applicationContext) }

    override fun onStartListening() {
        super.onStartListening()
        syncTile()
    }

    override fun onClick() {
        super.onClick()
        prefs.autoSuggestEnabled = !prefs.autoSuggestEnabled
        syncTile()
    }

    private fun syncTile() {
        val tile = qsTile ?: return
        val enabled = prefs.autoSuggestEnabled
        tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.tile_auto_suggest_label)
        tile.subtitle = getString(if (enabled) R.string.tile_state_on else R.string.tile_state_off)
        tile.icon = Icon.createWithResource(this, R.drawable.ic_tile_grammar)
        tile.updateTile()
    }
}
