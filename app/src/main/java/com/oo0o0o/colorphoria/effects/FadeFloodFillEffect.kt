package com.oo0o0o.colorphoria.effects

import com.oo0o0o.colorphoria.effecthelpers.EffectTypes

class FadeFloodFillEffect : FadeFillEffect(tintFill = false) {

    companion object {
        val effectType = EffectTypes.RANDOM_FADE_FLOOD_FILL
    }

    override val effectType: EffectTypes = FadeFloodFillEffect.effectType

}