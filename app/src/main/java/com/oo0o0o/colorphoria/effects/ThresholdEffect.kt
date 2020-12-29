package com.oo0o0o.colorphoria.effects

import android.graphics.Bitmap

import com.oo0o0o.colorphoria.effecthelpers.ColorHelper
import com.oo0o0o.colorphoria.effecthelpers.EffectTypes

import java.util.ArrayList

/**
 * Created by oo0o0o on 4/12/2018.
 */

class ThresholdEffect : BitmapEffect {

    companion object {
        val effectType = EffectTypes.THRESHOLD
    }

    override val effectType: EffectTypes = ThresholdEffect.effectType

    override fun applyEffect(bm: Bitmap, ctrl: Double, randomSeed: Int, colorPalette: ArrayList<Int>?, rotationAngle: Int): Bitmap {
        for (y in 0 until bm.height) {
            for (x in 0 until bm.width) {
                val color = ColorHelper.getPixelColor(bm, x, y)
                val red = color shr 16 and 0xFF
                val green = color shr 8 and 0xFF
                val blue = color shr 0 and 0xFF
                val average = (red + blue + green) / 3
                if (average <= 255 * ctrl) {
                    ColorHelper.setPixelColor(bm, x, y, 0x000000)
                } else {
                    ColorHelper.setPixelColor(bm, x, y, 0xffffff)
                }
            }
        }
        return bm
    }
}
