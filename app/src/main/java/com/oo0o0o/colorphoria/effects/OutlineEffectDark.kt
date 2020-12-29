package com.oo0o0o.colorphoria.effects

import com.oo0o0o.colorphoria.effecthelpers.EffectTypes

/**
 * Created by oo0o0o on 4/12/2018.
 */

class OutlineEffectDark : OutlineEffectBase(dark = true) {
    companion object {
        val effectType = EffectTypes.RANDOM_OUTLINE_DARK
    }

    override val effectType: EffectTypes = OutlineEffectDark.effectType
}