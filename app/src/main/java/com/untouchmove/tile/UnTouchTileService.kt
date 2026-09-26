package com.untouchmove.tile

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.untouchmove.service.GestureForegroundService

/** O bat/tat nhanh o thanh Quick Settings. Bat di qua [QuickStartActivity], tat thi dung service truc tiep. */
class UnTouchTileService : TileService() {

    override fun onStartListening() = refreshTile()

    override fun onClick() {
        if (GestureForegroundService.isRunning) {
            stopService(Intent(this, GestureForegroundService::class.java))
            refreshTile(active = false)
            return
        }
        val intent = Intent(this, QuickStartActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= 34) {
            startActivityAndCollapse(
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE),
            )
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    private fun refreshTile(active: Boolean = GestureForegroundService.isRunning) {
        qsTile?.apply {
            state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTile()
        }
    }
}
