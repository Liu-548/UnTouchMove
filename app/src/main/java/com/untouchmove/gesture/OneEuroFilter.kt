package com.untouchmove.gesture

import kotlin.math.PI
import kotlin.math.abs

/**
 * Loc "One Euro" (Casiez et al. 2012) - giam rung khi gia tri thay doi cham,
 * giu do tre thap khi gia tri thay doi nhanh. Dung cho vi tri con tro M2 (SPEC
 * muc 4.2). Thuan Kotlin, khong phu thuoc Android.
 *
 * minCutoff thap = loc manh hon luc tay gan nhu dung yen (giam rung), beta cao
 * = phan hoi nhanh hon luc tay di chuyen that (giam do tre). Quan trong hon
 * binh thuong o day vi CURSOR_GAIN co the rat lon (nguoi dung tu nhap, da
 * thu toi 5000) - nhieu con sot lai sau loc bi nhan len DUNG THEO TY LE gain,
 * nen gain cang lon thi loc cang phai chat. TODO(untouch): 0.05/0.01 la doan
 * chinh lai lan 2 (2026-09-20) sau khi 0.3/0.015 van con giat o gain cao -
 * CHUA CO GIOI HAN: gain cang tang thi du loc manh co nao cung se lai thay
 * ro nhieu, day la danh doi vat ly chu khong phai loc chua du tot. Neu van
 * giat o gain vua phai (vd duoi 1000), can do lai.
 */
class OneEuroFilter(
    private val minCutoff: Float = 0.05f,
    private val beta: Float = 0.01f,
    private val dCutoff: Float = 1.0f,
) {
    private var lastValue: Float? = null
    private var lastDerivative = 0f
    private var lastTimeMs: Long? = null

    fun filter(value: Float, timeMs: Long): Float {
        val prevTime = lastTimeMs
        val prevValue = lastValue
        if (prevTime == null || prevValue == null) {
            lastValue = value
            lastTimeMs = timeMs
            return value
        }

        val dtSec = (timeMs - prevTime).coerceAtLeast(1) / 1000f
        val derivative = (value - prevValue) / dtSec
        val smoothDerivative = lastDerivative + alpha(dCutoff, dtSec) * (derivative - lastDerivative)
        val cutoff = minCutoff + beta * abs(smoothDerivative)
        val smoothValue = prevValue + alpha(cutoff, dtSec) * (value - prevValue)

        lastValue = smoothValue
        lastDerivative = smoothDerivative
        lastTimeMs = timeMs
        return smoothValue
    }

    private fun alpha(cutoff: Float, dtSec: Float): Float {
        val tau = 1f / (2f * PI.toFloat() * cutoff)
        return 1f / (1f + tau / dtSec)
    }
}
