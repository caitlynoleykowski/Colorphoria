package com.oo0o0o.colorphoria.effects

import com.oo0o0o.colorphoria.effecthelpers.EffectTypes

class FadeTintFillEffect : FadeFillEffect(tintFill = true) {

    companion object {
        val effectType = EffectTypes.RANDOM_FADE_TINT_FILL
    }

    override val effectType: EffectTypes = FadeTintFillEffect.effectType

}