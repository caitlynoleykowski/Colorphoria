package com.oo0o0o.colorphoria.activities.loggedinactivities.edit

import android.os.Parcelable
import com.oo0o0o.colorphoria.effecthelpers.EffectTypes
import kotlinx.android.parcel.Parcelize
import java.util.*
import com.oo0o0o.colorphoria.effecthelpers.EffectTypes.*

//@Parcelize
//data class SeedManager(private var randomSeedsMap: MutableMap<EffectTypes, Int> = mutableMapOf()): Parcelable {
data class SeedManager(private var randomSeedsMap: MutableMap<EffectTypes, Int> = mutableMapOf()) {

    val randomSeeds: Map<EffectTypes, Int>
        get() = randomSeedsMap

    fun nextSeed() {
        // So that the effects aren't the same every time the activity is restarted,
        // the random seed itself needs to be random
        val randSeedGen = Random()
        val randomSeed = randSeedGen.nextInt()
//        randomSeedsMap = mutableMapOf(
//                NONE to randomSeed,
//                RESET to randomSeed,
//                ROTATE to randomSeed,
//                RANDOM_TINT_FILL to randomSeed,
//                THRESHOLD to randomSeed,
//                RANDOM_LINE_FILL to randomSeed,
//                RANDOM_FLOOD_FILL to randomSeed,
//                INVERT to randomSeed,
//                RANDOM_OUTLINE_LIGHT to randomSeed,
//                RANDOM_OUTLINE_DARK to randomSeed,
//                RANDOM_FADE_FLOOD_FILL to randomSeed,
//                RANDOM_FADE_TINT_FILL to randomSeed
//        )
        EffectTypes.values().forEach {
            randomSeedsMap[it] = randomSeed
        }
        println("test")
    }

    fun nextSeed(effectType: EffectTypes) {
        val randSeedGen = Random()
        val randomSeed = randSeedGen.nextInt()
        randomSeedsMap[effectType] = randomSeed
    }
}