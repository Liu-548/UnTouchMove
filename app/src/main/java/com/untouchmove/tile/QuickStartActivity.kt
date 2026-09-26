package com.untouchmove.tile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.untouchmove.MainActivity
import com.untouchmove.service.GestureForegroundService

/**
 * Activity trong suot cho Quick Settings Tile (ARCHITECTURE muc 5): Android 14+
 * khong cho tao foreground service loai camera tu nen, nen Tile mo Activity
 * nay (dang o truoc) de bat service roi dong ngay. Chua cap quyen Camera thi
 * chuyen sang MainActivity de xin quyen.
 */
class QuickStartActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            GestureForegroundService.requestStart(this)
        }
        finish()
    }
}
