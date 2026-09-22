package com.untouchmove.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.untouchmove.gesture.GestureThresholds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Luu cac nguong do nhay/thoi gian nguoi dung chinh duoc qua man hinh Cai dat
 * (SPEC muc 4.1, muc 6) bang DataStore Preferences (ARCHITECTURE muc 1). Doc/
 * ghi rieng tung khoa, khong gop thanh 1 object lon vi moi thanh truot trong
 * SettingsScreen chi doc lap 1 gia tri.
 */
class SettingsRepository(context: Context) {
    private val dataStore = context.applicationContext.dataStore

    private val keyVelUp = floatPreferencesKey("swipe_vel_min_up")
    private val keyVelDown = floatPreferencesKey("swipe_vel_min_down")
    private val keyVelLeft = floatPreferencesKey("swipe_vel_min_left")
    private val keyVelRight = floatPreferencesKey("swipe_vel_min_right")
    private val keyHandDetectionConfidence = floatPreferencesKey("hand_detection_confidence")
    private val keySwipeCooldownMs = longPreferencesKey("swipe_cooldown_ms")
    private val keyCursorGainTranslation = floatPreferencesKey("cursor_gain_translation")
    private val keyCursorGainPointing = floatPreferencesKey("cursor_gain_pointing")
    private val keyCursorPointingMode = booleanPreferencesKey("cursor_pointing_mode")
    private val keyGOpen = floatPreferencesKey("g_open")
    private val keySysVelMin = floatPreferencesKey("sys_vel_min")
    private val keyEnableM1Vertical = booleanPreferencesKey("enable_m1_vertical")
    private val keyEnableM1Horizontal = booleanPreferencesKey("enable_m1_horizontal")
    private val keyEnableM2Cursor = booleanPreferencesKey("enable_m2_cursor")
    private val keyEnableM5System = booleanPreferencesKey("enable_m5_system")
    private val keyEnableM6ScreenOff = booleanPreferencesKey("enable_m6_screen_off")
    private val keyEnableScreenOffReopenGesture = booleanPreferencesKey("enable_screen_off_reopen_gesture")

    val velUp: Flow<Float> = dataStore.data.map { it[keyVelUp] ?: GestureThresholds.DEFAULT_SWIPE_VEL_MIN_UP }
    val velDown: Flow<Float> = dataStore.data.map { it[keyVelDown] ?: GestureThresholds.DEFAULT_SWIPE_VEL_MIN_DOWN }
    val velLeft: Flow<Float> = dataStore.data.map { it[keyVelLeft] ?: GestureThresholds.DEFAULT_SWIPE_VEL_MIN_LEFT }
    val velRight: Flow<Float> = dataStore.data.map { it[keyVelRight] ?: GestureThresholds.DEFAULT_SWIPE_VEL_MIN_RIGHT }
    val handDetectionConfidence: Flow<Float> =
        dataStore.data.map { it[keyHandDetectionConfidence] ?: GestureThresholds.DEFAULT_HAND_DETECTION_CONFIDENCE }
    val swipeCooldownMs: Flow<Long> =
        dataStore.data.map { it[keySwipeCooldownMs] ?: GestureThresholds.DEFAULT_SWIPE_COOLDOWN_MS }
    val cursorGainTranslation: Flow<Float> =
        dataStore.data.map { it[keyCursorGainTranslation] ?: GestureThresholds.DEFAULT_CURSOR_GAIN_TRANSLATION }
    val cursorGainPointing: Flow<Float> =
        dataStore.data.map { it[keyCursorGainPointing] ?: GestureThresholds.DEFAULT_CURSOR_GAIN_POINTING }
    val cursorPointingMode: Flow<Boolean> = dataStore.data.map { it[keyCursorPointingMode] ?: false }
    val gOpen: Flow<Float> = dataStore.data.map { it[keyGOpen] ?: GestureThresholds.DEFAULT_G_OPEN }
    val sysVelMin: Flow<Float> = dataStore.data.map { it[keySysVelMin] ?: GestureThresholds.DEFAULT_SYS_VEL_MIN }
    val enableM1Vertical: Flow<Boolean> = dataStore.data.map { it[keyEnableM1Vertical] ?: true }
    val enableM1Horizontal: Flow<Boolean> = dataStore.data.map { it[keyEnableM1Horizontal] ?: true }
    val enableM2Cursor: Flow<Boolean> = dataStore.data.map { it[keyEnableM2Cursor] ?: true }
    val enableM5System: Flow<Boolean> = dataStore.data.map { it[keyEnableM5System] ?: true }
    val enableM6ScreenOff: Flow<Boolean> = dataStore.data.map { it[keyEnableM6ScreenOff] ?: true }
    val enableScreenOffReopenGesture: Flow<Boolean> =
        dataStore.data.map { it[keyEnableScreenOffReopenGesture] ?: false }

    suspend fun setVelUp(value: Float) = dataStore.edit { it[keyVelUp] = value }
    suspend fun setVelDown(value: Float) = dataStore.edit { it[keyVelDown] = value }
    suspend fun setVelLeft(value: Float) = dataStore.edit { it[keyVelLeft] = value }
    suspend fun setVelRight(value: Float) = dataStore.edit { it[keyVelRight] = value }
    suspend fun setHandDetectionConfidence(value: Float) = dataStore.edit { it[keyHandDetectionConfidence] = value }
    suspend fun setSwipeCooldownMs(value: Long) = dataStore.edit { it[keySwipeCooldownMs] = value }
    suspend fun setCursorGainTranslation(value: Float) = dataStore.edit { it[keyCursorGainTranslation] = value }
    suspend fun setCursorGainPointing(value: Float) = dataStore.edit { it[keyCursorGainPointing] = value }
    suspend fun setCursorPointingMode(value: Boolean) = dataStore.edit { it[keyCursorPointingMode] = value }
    suspend fun setGOpen(value: Float) = dataStore.edit { it[keyGOpen] = value }
    suspend fun setSysVelMin(value: Float) = dataStore.edit { it[keySysVelMin] = value }
    suspend fun setEnableM1Vertical(value: Boolean) = dataStore.edit { it[keyEnableM1Vertical] = value }
    suspend fun setEnableM1Horizontal(value: Boolean) = dataStore.edit { it[keyEnableM1Horizontal] = value }
    suspend fun setEnableM2Cursor(value: Boolean) = dataStore.edit { it[keyEnableM2Cursor] = value }
    suspend fun setEnableM5System(value: Boolean) = dataStore.edit { it[keyEnableM5System] = value }
    suspend fun setEnableM6ScreenOff(value: Boolean) = dataStore.edit { it[keyEnableM6ScreenOff] = value }
    suspend fun setEnableScreenOffReopenGesture(value: Boolean) =
        dataStore.edit { it[keyEnableScreenOffReopenGesture] = value }
}
