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
    private val keyVelLeftRight = floatPreferencesKey("swipe_vel_min_left_right")
    private val keyHandDetectionConfidence = floatPreferencesKey("hand_detection_confidence")
    private val keySwipeCooldownMs = longPreferencesKey("swipe_cooldown_ms")
    private val keyCursorGainTranslation = floatPreferencesKey("cursor_gain_translation")
    private val keyCursorGainPointing = floatPreferencesKey("cursor_gain_pointing")
    private val keyCursorPointingMode = booleanPreferencesKey("cursor_pointing_mode")
    private val keyGOpen = floatPreferencesKey("g_open")
    private val keySysVelMin = floatPreferencesKey("sys_vel_min")

    val velUp: Flow<Float> = dataStore.data.map { it[keyVelUp] ?: GestureThresholds.DEFAULT_SWIPE_VEL_MIN_UP }
    val velDown: Flow<Float> = dataStore.data.map { it[keyVelDown] ?: GestureThresholds.DEFAULT_SWIPE_VEL_MIN_DOWN }
    val velLeftRight: Flow<Float> =
        dataStore.data.map { it[keyVelLeftRight] ?: GestureThresholds.DEFAULT_SWIPE_VEL_MIN_LEFT_RIGHT }
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

    suspend fun setVelUp(value: Float) = dataStore.edit { it[keyVelUp] = value }
    suspend fun setVelDown(value: Float) = dataStore.edit { it[keyVelDown] = value }
    suspend fun setVelLeftRight(value: Float) = dataStore.edit { it[keyVelLeftRight] = value }
    suspend fun setHandDetectionConfidence(value: Float) = dataStore.edit { it[keyHandDetectionConfidence] = value }
    suspend fun setSwipeCooldownMs(value: Long) = dataStore.edit { it[keySwipeCooldownMs] = value }
    suspend fun setCursorGainTranslation(value: Float) = dataStore.edit { it[keyCursorGainTranslation] = value }
    suspend fun setCursorGainPointing(value: Float) = dataStore.edit { it[keyCursorGainPointing] = value }
    suspend fun setCursorPointingMode(value: Boolean) = dataStore.edit { it[keyCursorPointingMode] = value }
    suspend fun setGOpen(value: Float) = dataStore.edit { it[keyGOpen] = value }
    suspend fun setSysVelMin(value: Float) = dataStore.edit { it[keySysVelMin] = value }
}
