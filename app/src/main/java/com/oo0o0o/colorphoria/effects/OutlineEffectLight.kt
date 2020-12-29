package com.oo0o0o.colorphoria.effects

import com.oo0o0o.colorphoria.effecthelpers.EffectTypes

/**
 * Created by oo0o0o on 4/12/2018.
 */

class OutlineEffectLight : OutlineEffectBase(dark = false) {
    companion object {
        val effectType = EffectTypes.RANDOM_OUTLINE_LIGHT
    }

    override val effectType: EffectTypes = OutlineEffectLight.effectType
}