package com.oo0o0o.colorphoria.effects

import android.graphics.Bitmap
import com.oo0o0o.colorphoria.effecthelpers.EffectTypes

interface BitmapEffect {
    val effectType: EffectTypes
    fun applyEffect(bm: Bitmap, ctrl: Double, randomSeed: Int, colorPalette: ArrayList<Int>?, rotationAngle: Int): Bitmap?
}