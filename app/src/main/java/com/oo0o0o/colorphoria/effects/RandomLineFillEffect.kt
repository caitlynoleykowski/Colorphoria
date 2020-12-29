package com.oo0o0o.colorphoria.effects

import android.graphics.Bitmap

import com.oo0o0o.colorphoria.effecthelpers.ColorHelper
import com.oo0o0o.colorphoria.effecthelpers.EffectTypes

import java.util.ArrayList
import java.util.Random

/**
 * Created by oo0o0o on 4/12/2018.
 */

class RandomLineFillEffect : BitmapEffect {

    companion object {
        val effectType = EffectTypes.RANDOM_LINE_FILL
    }

    override val effectType: EffectTypes = RandomLineFillEffect.effectType

    override fun applyEffect(bm: Bitmap, ctrl: Double, randomSeed: Int, colorPalette: ArrayList<Int>?, rotationAngle: Int): Bitmap {
        ThresholdEffect().applyEffect(bm, ctrl, randomSeed, colorPalette, rotationAngle)
        applyRandomLineFill(bm, ctrl, randomSeed, colorPalette)
        return bm
    }

    private fun applyRandomLineFill(bm: Bitmap, ctrl: Double, randomSeed: Int, colorPalette: ArrayList<Int>?) {
        var prevPixelColor = 0x000000
        var currColor = 0
        val r = Random(randomSeed.toLong())
        for (y in 0 until bm.height) {
            for (x in 0 until bm.width) {
                val color = ColorHelper.getPixelColor(bm, x, y)
                if (color == 0xffffff && (prevPixelColor == 0x000000 || x == 0)) {
                    currColor = ColorHelper.getNextColor(r, colorPalette)
                    prevPixelColor = color
                    ColorHelper.setPixelColor(bm, x, y, currColor)
                } else if (color == 0xffffff && prevPixelColor == 0xffffff) {
                    prevPixelColor = color
                    ColorHelper.setPixelColor(bm, x, y, currColor)
                } else if (color == 0x000000) {
                    prevPixelColor = color
                } else {
                    // this condition is only hit if the current pixel is a different color
                    prevPixelColor = color
                }
            }
        }
    }
}