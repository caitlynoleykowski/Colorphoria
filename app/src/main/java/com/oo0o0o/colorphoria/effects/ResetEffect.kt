package com.oo0o0o.colorphoria.effects

import android.graphics.Bitmap

import com.oo0o0o.colorphoria.effecthelpers.BitmapWrapper
import com.oo0o0o.colorphoria.effecthelpers.EffectTypes

import java.util.ArrayList

/**
 * Created by oo0o0o on 4/12/2018.
 */

class ResetEffect : BitmapEffect {

    override fun applyEffect(bm: Bitmap, ctrl: Double, randomSeed: Int, colorPalette: ArrayList<Int>?, rotationAngle: Int): Bitmap {
        return bm // Yeah this is weird but it's because the rotation angle is in the BitmapWrapper
    }
}
