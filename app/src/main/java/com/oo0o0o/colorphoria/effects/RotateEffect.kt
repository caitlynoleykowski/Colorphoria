package com.oo0o0o.colorphoria.effects

import android.graphics.Bitmap
import android.graphics.Matrix

import com.oo0o0o.colorphoria.effecthelpers.BitmapWrapper
import com.oo0o0o.colorphoria.effecthelpers.EffectTypes

import java.util.ArrayList

/**
 * Created by oo0o0o on 4/12/2018.
 */

class RotateEffect : BitmapEffect {

    override fun applyEffect(bm: Bitmap, ctrl: Double, randomSeed: Int, colorPalette: ArrayList<Int>?, rotationAngle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotationAngle.toFloat())
        return Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, matrix, true)
    }
}
