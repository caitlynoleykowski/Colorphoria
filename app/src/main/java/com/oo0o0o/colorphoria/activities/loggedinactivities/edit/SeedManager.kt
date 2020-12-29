package com.oo0o0o.colorphoria.activities.loggedinactivities.edit

import android.os.Parcelable
import com.oo0o0o.colorphoria.effecthelpers.EffectTypes
import kotlinx.android.parcel.Parcelize
import java.util.*
import com.oo0o0o.colorphoria.effecthelpers.EffectTypes.*

//@Parcelize
//class SeedManager : Parcelable {
//
//    private var randomSeedsMap = mutableMapOf(
//           NONE to 0,
//           RESET to 0,
//           ROTATE to 0,
//           RANDOM_TINT_FILL to 0,
//           THRESHOLD to 0,
//           RANDOM_LINE_FILL to 0,
//           RANDOM_FLOOD_FILL to 0,
//           INVERT to 0,
//           RANDOM_OUTLINE_LIGHT to 0,
//           RANDOM_OUTLINE_DARK to 0,
//           RANDOM_FADE_FILL to 0,
//           RANDOM_FADE_TINT_FILL to 0
//    )
//
//    fun nextSeed() {
//        // So that the effects aren't the same every time the activity is restarted,
//        // the random seed itself needs to be random
//        val randSeedGen = Random()
//        val randomSeed = randSeedGen.nextInt()
//        randomSeedsMap.mapValues { randomSeed }
//        println("test")
//    }
//
//    fun nextSeed(effectType: EffectTypes) {
//        val randSeedGen = Random()
//        val randomSeed = randSeedGen.nextInt()
//        randomSeedsMap[effectType] = randomSeed
//    }
//}