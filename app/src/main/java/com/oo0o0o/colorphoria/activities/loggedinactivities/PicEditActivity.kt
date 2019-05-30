package com.oo0o0o.colorphoria.activities.loggedinactivities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.oo0o0o.colorphoria.effecthelpers.BitmapEffectAsyncTask
import com.oo0o0o.colorphoria.effecthelpers.BitmapEffectPreviewAsyncTask
import com.oo0o0o.colorphoria.effecthelpers.PicEditBitmapLoaderAsyncTask
import com.oo0o0o.colorphoria.effecthelpers.BitmapWrapper
import com.oo0o0o.colorphoria.R
import com.oo0o0o.colorphoria.effects.BitmapEffect
import com.oo0o0o.colorphoria.effects.InvertEffect
import com.oo0o0o.colorphoria.effects.OutlineEffect
import com.oo0o0o.colorphoria.effects.RandomBleedFillEffect
import com.oo0o0o.colorphoria.effects.RandomFloodFillEffect
import com.oo0o0o.colorphoria.effects.RandomLineFillEffect
import com.oo0o0o.colorphoria.effects.ResetEffect
import com.oo0o0o.colorphoria.effects.RotateEffect
import com.oo0o0o.colorphoria.effects.ThresholdEffect
import com.oo0o0o.colorphoria.reusableuserinterface.PaletteChooserDialog
import com.oo0o0o.colorphoria.utils.Utils
import kotlinx.android.synthetic.main.activity_pic_edit.*

import java.util.ArrayDeque
import java.util.ArrayList
import java.util.Deque
import java.util.HashMap
import java.util.Random

class PicEditActivity : LoggedInActivity() {

    val SOURCE_URI = "SOURCE_URI"

    private val SOURCE_URI_STATE_KEY = "SOURCE_STATE_KEY"
    private val MOD_URI_STATE_KEY = "MOD_STATE_KEY"
    private val ROTATION_ANGLE_STATE_KEY = "ROTATION_ANGLE_STATE_KEY"

    val REQUEST_PERMISSIONS_CALLBACK_SAVE_WRITE_EXTERNAL_STORAGE = 0

    private val MAX_UNDO_QUEUE_SIZE = 2

    private var bmW: BitmapWrapper? = null
    private var undoQueue: Deque<BitmapWrapper>? = null
    private var redoQueue: Deque<BitmapWrapper>? = null
    private var processingImageDone = false

    private var sourceUri: Uri? = null

    private var randomSeed: Int = 0

    private val colorPalette = ArrayList<Int>()

    var adView: AdView? = null

    var interstitialAd: InterstitialAd? = null

    private var firebaseAnalytics: FirebaseAnalytics? = null

    private val effectCtrl: Double
        get() {
            val ctrlSeekbar = findViewById<View>(R.id.effect_ctrl_seekbar) as SeekBar
            val progress = ctrlSeekbar.progress.toDouble()
            val max = ctrlSeekbar.max.toDouble()
            return 1 - progress / max
        }

    // Begin activity lifecycle handlers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pic_edit)

        Utils.setupcolorphoriaRecentAppsBar(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp)

        undoQueue = ArrayDeque()
        redoQueue = ArrayDeque()

        // So that the effects aren't the same every time the activity is restarted,
        // the random seed itself needs to be random
        val randSeedGen = Random()
        randomSeed = randSeedGen.nextInt()

//        setUIforImageDoneProcessing(false)

        interstitialAd = InterstitialAd(this)
        interstitialAd?.adUnitId = resources.getString(R.string.admob_ad_unit_id_interstitial_share)
//        interstitialAd?.adListener = object : AdListener() {
////            override fun onAdLoaded() {
////                // Code to be executed when an ad finishes loading.
////            }
////
////            override fun onAdFailedToLoad(errorCode: Int) {
////                // Code to be executed when an ad request fails.
////                Log.w(TAG, "Interstitial ad failed to load, error code: $errorCode")
////            }
////
////            override fun onAdOpened() {
////                // Code to be executed when the ad is displayed.
////            }
////
////            override fun onAdLeftApplication() {
////                // Code to be executed when the user has left the app.
////            }
////
////            override fun onAdClosed() {
////                interstitialAd?.loadAd(AdRequest.Builder().build())
////                // Code to be executed when when the interstitial ad is closed.
////                // This function call below assumes that permission has already been procured
////                // for storage access, because the ad would not have been shown without it
////                saveImageAndStartShareActivity()
////            }
////        }
//        interstitialAd?.loadAd(AdRequest.Builder().build())

        if (savedInstanceState != null) {
            sourceUri = savedInstanceState.getParcelable(SOURCE_URI_STATE_KEY)

            // Have to do this in place of the to do above. Same steps as new intent below
            bmW = BitmapWrapper()
            val bmwAsync = PicEditBitmapLoaderAsyncTask(this, applicationContext, imageView, sourceUri, bmW,
                    randomBleedButton,
                    thresholdButton,
                    randomLineButton,
                    randomFloodButton,
                    invertButton,
                    outlineButton,
                    effectCtrl, randomSeed)
            bmwAsync.execute()
        } else if (intent.hasExtra(SOURCE_URI)) {
            sourceUri = intent.getParcelableExtra<Parcelable>(SOURCE_URI) as Uri
            bmW = BitmapWrapper()
            val bmwAsync = PicEditBitmapLoaderAsyncTask(this, applicationContext, imageView, sourceUri, bmW,
                    randomBleedButton,
                    thresholdButton,
                    randomLineButton,
                    randomFloodButton,
                    invertButton,
                    outlineButton,
                    effectCtrl, randomSeed)
            bmwAsync.execute()
        } else {
            // TODO: Error/bug, shouldn't get here
//            setUIforImageDoneProcessing(false)
            val ctrlLayout = findViewById<View>(R.id.effect_ctrl_layout) as LinearLayout
            ctrlLayout.visibility = View.GONE
        }

//        val pb = findViewById<View>(R.id.effect_ctrl_seekbar) as SeekBar
//        pb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//
//            override fun onStopTrackingTouch(seekBar: SeekBar) {
//                // TODO Auto-generated method stub
//                refreshEffectThumbnails(randomSeed)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar) {
//                // TODO Auto-generated method stub
//            }
//
//            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//                // TODO Auto-generated method stub
//            }
//        })
//
//        randomBleedButton.setOnClickListener {
//            if (processingImageDone) {
//                onRandomBleedFill(it)
//            }
//        }
//        thresholdButton.setOnClickListener {
//            if (processingImageDone) {
//                onThreshold(it)
//            }
//        }
//        randomLineButton.setOnClickListener {
//            if (processingImageDone) {
//                onRandomLineFill(it)
//            }
//        }
//        randomFloodButton.setOnClickListener {
//            if (processingImageDone) {
//                onRandomFloodFill(it)
//            }
//        }
//        invertButton.setOnClickListener {
//            if (processingImageDone) {
//                onInvert(it)
//            }
//        }
//        outlineButton.setOnClickListener {
//            if (processingImageDone) {
//                onOutlineEffect(it)
//            }
//        }
//
//        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
//        val bundle = Bundle()
//        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, PicEditActivity::class.java.name)
//        firebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }
//
//    /** Called when leaving the activity  */
//    public override fun onPause() {
//        if (adView != null) {
//            adView!!.pause()
//        }
//        super.onPause()
//    }
//
//    /** Called when returning to the activity  */
//    public override fun onResume() {
//        super.onResume()
//        if (adView != null) {
//            adView!!.resume()
//        }
//    }
//
//    /** Called before the activity is destroyed  */
//    public override fun onDestroy() {
//        if (adView != null) {
//            adView!!.destroy()
//        }
//        super.onDestroy()
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        if (bmW != null) {
//            outState.putParcelable(SOURCE_URI_STATE_KEY, sourceUri)
//            //            outState.putParcelable(MOD_URI_STATE_KEY, modUri);
//            outState.putInt(ROTATION_ANGLE_STATE_KEY, bmW!!.rotationAngle)
//        } else {
//            outState.putParcelable(SOURCE_URI_STATE_KEY, null)
//            //            outState.putParcelable(MOD_URI_STATE_KEY, modUri);
//            outState.putInt(ROTATION_ANGLE_STATE_KEY, 0)
//        }
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        val inflater = menuInflater
//        inflater.inflate(R.menu.pic_edit_menu, menu)
//        return true
//    }
//
//    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
//        // Only enable the menu items (which act on the image) when the image is done processing
//        val enableMenuItems = processingImageDone
//        for (i in 0 until menu.size()) {
//            if (menu.getItem(i).itemId == R.id.home) {
//                // do nothing
//            } else {
//                menu.getItem(i).isEnabled = enableMenuItems
//            }
//        }
//        if (enableMenuItems) {
//            if (undoQueue!!.size > 0) {
//                val undoItem = menu.findItem(R.id.pic_edit_menu_undo)
//                undoItem.isEnabled = true
//            } else {
//                val undoItem = menu.findItem(R.id.pic_edit_menu_undo)
//                undoItem.isEnabled = false
//            }
//            if (redoQueue!!.size > 0) {
//                val undoItem = menu.findItem(R.id.pic_edit_menu_redo)
//                undoItem.isEnabled = true
//            } else {
//                val undoItem = menu.findItem(R.id.pic_edit_menu_redo)
//                undoItem.isEnabled = false
//            }
//        }
//        return true
//    }
//
//    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
//        when (menuItem.itemId) {
//            android.R.id.home -> {
//                finish()
//                return true
//            }
//            R.id.pic_edit_menu_reset -> {
//                onReset()
//                return true
//            }
//            R.id.pic_edit_menu_undo -> {
//                onUndo()
//                return true
//            }
//            R.id.pic_edit_menu_redo -> {
//                onRedo()
//                return true
//            }
//            R.id.pic_edit_menu_save -> {
//                onSave()
//                return true
//            }
//            R.id.pic_edit_menu_share -> {
//                onShare()
//                return true
//            }
//            else -> return super.onOptionsItemSelected(menuItem)
//        }
//    }
//
//    // End activity lifecycle handlers
//
//    private fun setUIforImageDoneProcessing(processingImageDone: Boolean) {
//        this.processingImageDone = processingImageDone
//        invalidateOptionsMenu()
//
//        if (processingImageDone) {
//            (findViewById<View>(R.id.progress_bar) as ProgressBar).visibility = View.GONE
//        } else {
//            (findViewById<View>(R.id.progress_bar) as ProgressBar).visibility = View.VISIBLE
//        }
//
//        // Disable buttons in bottom control panel (effects buttons/rotate button/palette button)
//        val ll2 = findViewById<View>(R.id.icon_linear_layout) as LinearLayout
//        ll2.childCount
//        for (i in 0 until ll2.childCount) {
//            ll2.getChildAt(i).isClickable = processingImageDone
//        }
//        findViewById<View>(R.id.choose_palette).isClickable = processingImageDone
//        findViewById<View>(R.id.rotate_right).isClickable = processingImageDone
//        findViewById<View>(R.id.effect_ctrl_seekbar).isEnabled = processingImageDone
//    }
//
//    // Begin UI handlers
//    fun onReset() {
//        applyEffect(ResetEffect())
//    }
//
//    fun onRedo() {
//        undoEnqueue()
//        bmW = redoQueue!!.removeLast()
//        if (redoQueue!!.isEmpty()) {
//            invalidateOptionsMenu()
//        }
//        invalidateOptionsMenu()
//        imageView.setImageBitmap(bmW!!.modifiedBitmap)
//    }
//
//    fun onUndo() {
//        redoEnqueue()
//        bmW = undoQueue!!.removeLast()
//        if (undoQueue!!.isEmpty()) {
//            invalidateOptionsMenu()
//        }
//        imageView.setImageBitmap(bmW!!.modifiedBitmap)
//    }
//
//    fun onSave() {
//        saveCurrImageStoragePermission()
//    }
//
//    fun onShare() {
//        if (firebaseAnalytics != null) {
//            firebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SHARE, Bundle())
//        }
//        shareCurrImageStoragePermission()
//    }
//
//    fun onRotateRight() {
//        applyEffect(RotateEffect())
//    }
//
//    fun onChoosePalette(v: View) {
//        val colorPaletteDialog = PaletteChooserDialog(this, colorPalette)
//        // TODO: Make an onPaletteChange callback that is called in the ondismiss in case of a change
//        // inside of the PaletteChooserDialog class
//        colorPaletteDialog.setOnDismissListener {
//            if (colorPaletteDialog.isColorPaletteChanged) {
//                refreshEffectThumbnails(randomSeed)
//            }
//        }
//        colorPaletteDialog.show()
//    }
//
//    fun onRandomBleedFill(v: View) {
//        if (firebaseAnalytics != null) {
//            val bundle = Bundle()
//            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, RandomBleedFillEffect::class.java.name)
//            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
//        }
//        applyEffect(RandomBleedFillEffect())
//    }
//
//    fun onThreshold(v: View) {
//        if (firebaseAnalytics != null) {
//            val bundle = Bundle()
//            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, ThresholdEffect::class.java.name)
//            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
//        }
//        applyEffect(ThresholdEffect())
//    }
//
//    fun onRandomLineFill(v: View) {
//        if (firebaseAnalytics != null) {
//            val bundle = Bundle()
//            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, RandomLineFillEffect::class.java.name)
//            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
//        }
//        applyEffect(RandomLineFillEffect())
//    }
//
//    fun onRandomFloodFill(v: View) {
//        if (firebaseAnalytics != null) {
//            val bundle = Bundle()
//            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, RandomFloodFillEffect::class.java.name)
//            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
//        }
//        applyEffect(RandomFloodFillEffect())
//    }
//
//    fun onInvert(v: View) {
//        if (firebaseAnalytics != null) {
//            val bundle = Bundle()
//            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, InvertEffect::class.java.name)
//            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
//        }
//        applyEffect(InvertEffect())
//    }
//
//    fun onOutlineEffect(v: View) {
//        if (firebaseAnalytics != null) {
//            val bundle = Bundle()
//            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, OutlineEffect::class.java.name)
//            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
//        }
//        applyEffect(OutlineEffect())
//    }
//
//    // End UI Handlers
//
//    // Begin UI Helpers
//
//    private fun refreshEffectThumbnails(randomSeed: Int) {
//        // Populate the effect preview buttons
//        BitmapEffectPreviewAsyncTask(this,
//                randomBleedButton,
//                RandomBleedFillEffect(), bmW!!.sourceBitmap, effectCtrl,
//                randomSeed, colorPalette).execute()
//        BitmapEffectPreviewAsyncTask(this,
//                thresholdButton,
//                ThresholdEffect(), bmW!!.sourceBitmap, effectCtrl,
//                randomSeed, colorPalette).execute()
//        BitmapEffectPreviewAsyncTask(this,
//                randomLineButton,
//                RandomLineFillEffect(), bmW!!.sourceBitmap, effectCtrl,
//                randomSeed, colorPalette).execute()
//        BitmapEffectPreviewAsyncTask(this,
//                randomFloodButton,
//                RandomFloodFillEffect(), bmW!!.sourceBitmap, effectCtrl,
//                randomSeed, colorPalette).execute()
//        BitmapEffectPreviewAsyncTask(this,
//                invertButton,
//                InvertEffect(), bmW!!.sourceBitmap, effectCtrl,
//                randomSeed, colorPalette).execute()
//        BitmapEffectPreviewAsyncTask(this,
//                outlineButton,
//                OutlineEffect(), bmW!!.sourceBitmap, effectCtrl,
//                randomSeed, colorPalette).execute()
//    }
//
//    // public to be used by BitmapEffectAsyncTask
//    fun doneProgress() {
//        val progressBar = findViewById<View>(R.id.progress_bar) as ProgressBar
//        progressBar.visibility = View.GONE
//        setUIforImageDoneProcessing(true)
//    }
//
//    // End UI Helpers
//
//    // Begin business logic
//
//    private fun undoEnqueue() {
//        if (undoQueue!!.size >= MAX_UNDO_QUEUE_SIZE) {
//            undoQueue!!.removeFirst()
//        }
//        val undoCopy = BitmapWrapper(bmW!!.sourceBitmap.copy(bmW!!.sourceBitmap.config, true))
//        undoCopy.modifiedBitmap = bmW!!.modifiedBitmap.copy(bmW!!.modifiedBitmap.config, true)
//        undoQueue!!.addLast(undoCopy)
//        invalidateOptionsMenu()
//    }
//
//    private fun redoEnqueue() {
//        // TODO: Do we need to check if the size gets exceeded? think its handled by undo checking
//        val redoCopy = BitmapWrapper(bmW!!.sourceBitmap.copy(bmW!!.sourceBitmap.config, true))
//        redoCopy.modifiedBitmap = bmW!!.modifiedBitmap.copy(bmW!!.modifiedBitmap.config, true)
//        redoQueue!!.addLast(redoCopy)
//        invalidateOptionsMenu()
//    }
//
//    private fun updateUndoRedoQueues() {
//        redoQueue!!.clear()
//        invalidateOptionsMenu()
//        undoEnqueue()
//    }
//
//    private fun applyEffect(effect: BitmapEffect) {
//        (findViewById<View>(R.id.progress_bar) as ProgressBar).visibility = View.VISIBLE
//        updateUndoRedoQueues()
//        setUIforImageDoneProcessing(false)
//        val ctrl = effectCtrl
//        BitmapEffectAsyncTask(this, imageView, effect, bmW, ctrl, randomSeed, colorPalette).execute()
//        val randSeedGen = Random()
//        randomSeed = randSeedGen.nextInt()
//        refreshEffectThumbnails(randomSeed)
//    }
//
//    private fun saveCurrImageStoragePermission() {
////        if (Utils.getPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
////                        getString(R.string.permission_write_external_storage_explanation),
////                        REQUEST_PERMISSIONS_CALLBACK_SAVE_WRITE_EXTERNAL_STORAGE
////                ) { Utils.showCustomToast(this@PicEditActivity, getString(R.string.unable_to_save_image)) }) {
////            saveCurrImage()
////        }
//    }
//
//    private fun saveCurrImage() {
////        Utils.savecolorphoriaBitmap(bmW!!.modifiedBitmap, this, object : Utils.SavecolorphoriaBitmapHandler {
////            override fun onSuccess(uri: Uri) {
////                Utils.showCustomToast(this@PicEditActivity, getString(R.string.image_saved))
////            }
////
////            override fun onFailure() {
////                // do nothing
////                Utils.showCustomToast(this@PicEditActivity, "Save failed.")
////            }
////        })
//    }
//
//    private fun shareCurrImageStoragePermission() {
////        if (Utils.getPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
////                        getString(R.string.permission_write_external_storage_explanation),
////                        REQUEST_PERMISSIONS_CALLBACK_SAVE_WRITE_EXTERNAL_STORAGE
////                ) { Utils.showCustomToast(this@PicEditActivity, getString(R.string.unable_to_save_image)) }) {
////            showInterstitial()
////        }
//    }
//
//    private fun showInterstitial() {
//        if (interstitialAd?.isLoaded == true) {
//            interstitialAd?.show()
//        } else {
//            Log.d(TAG, "The interstitial wasn't loaded yet.")
//            saveImageAndStartShareActivity()
//        }
//    }
//
//    private fun saveImageAndStartShareActivity() {
////        Utils.savecolorphoriaBitmap(bmW!!.modifiedBitmap, this, object : Utils.SavecolorphoriaBitmapHandler {
////            override fun onSuccess(uri: Uri) {
////                Utils.showCustomToast(this@PicEditActivity, getString(R.string.image_saved))
////                startShareActivity(uri)
////            }
////
////            override fun onFailure() {
////                // do nothing
////                Utils.showCustomToast(this@PicEditActivity, "Save failed.")
////            }
////        })
//    }
//
//    private fun startShareActivity(uri: Uri) {
//        val intent = Intent(this, UploadActivity::class.java)
//        intent.putExtra(UploadActivity.IMAGE_URI, uri)
//        startActivity(intent)
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int,
//                                            permissions: Array<String>, grantResults: IntArray) {
//        when (requestCode) {
//            REQUEST_PERMISSIONS_CALLBACK_SAVE_WRITE_EXTERNAL_STORAGE -> {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // Permission was granted, so continue.
//                    saveCurrImage()
//                } else {
//                    Utils.showCustomToast(this@PicEditActivity, getString(R.string.unable_to_save_image))
//                }
//                return
//            }
//        }
//    }
//
//    // End business logic
//
//    // Begin network calls
//
//    private fun getData() {
////        // Instantiate the RequestQueue.
////        val queue = Volley.newRequestQueue(this)
////        val url = "https://black-obelisk-161316.appspot.com/_ah/api/echo/v1/echo"
////
////        // Request a string response from the provided URL.
////        val stringRequest = object : StringRequest(Request.Method.POST, url,
////                Response.Listener { response ->
////                    // Display the first 500 characters of the response string.
////                    Log.i(this@PicEditActivity.javaClass.name, "Response is: $response")
////                }, Response.ErrorListener { error -> Log.i(this@PicEditActivity.javaClass.name, "Error when calling endpoint: " + error.message) }) {
////            override fun getParams(): Map<String, String> {
////                val params = HashMap<String, String>()
////                params["message"] = "data"
////                params["n"] = "9"
////
////                return params
////            }
////        }
////        // Add the request to the RequestQueue.
////        queue.add(stringRequest)
//    }
//

    // End network calls
}