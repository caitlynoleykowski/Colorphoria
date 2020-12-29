package com.oo0o0o.colorphoria.effects

import android.graphics.Bitmap

import com.oo0o0o.colorphoria.effecthelpers.ColorHelper
import com.oo0o0o.colorphoria.effecthelpers.EffectTypes

import java.util.ArrayList

/**
 * Created by oo0o0o on 4/12/2018.
 */

class InvertEffect : BitmapEffect {

    companion object {
        val effectType = EffectTypes.INVERT
    }

    override val effectType: EffectTypes = InvertEffect.effectType

    override fun applyEffect(bm: Bitmap, ctrl: Double, randomSeed: Int, colorPalette: ArrayList<Int>?, rotationAngle: Int): Bitmap {
        for (y in 0 until bm.height) {
            for (x in 0 until bm.width) {
                val color = ColorHelper.getPixelColor(bm, x, y)

                val red = color shr 16 and 0xFF
                val green = color shr 8 and 0xFF
                val blue = color shr 0 and 0xFF

                // Only invert if below the threshold (ctrl is inverted, i.e. ctrl = 1 - seekbar)
                if ((red + green + blue) / 3 <= ctrl * 0xff) {
                    val invertedRed = 0xff - red
                    val invertedGreen = 0xff - green
                    val invertedBlue = 0xff - blue

                    val invertedColor = (invertedRed shl 16) + (invertedGreen shl 8) + invertedBlue

                    ColorHelper.setPixelColor(bm, x, y, invertedColor)
                }
            }
        }
        return bm
    }
}
