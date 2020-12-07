package com.oo0o0o.colorphoria.activities.loggedinactivities.edit

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
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.oo0o0o.colorphoria.R
import com.oo0o0o.colorphoria.activities.loggedinactivities.LoggedInActivity
import com.oo0o0o.colorphoria.activities.loggedinactivities.upload.UploadActivity
import com.oo0o0o.colorphoria.effecthelpers.BitmapEffectAsyncTask
import com.oo0o0o.colorphoria.effecthelpers.BitmapEffectPreviewAsyncTask
import com.oo0o0o.colorphoria.effecthelpers.BitmapWrapper
import com.oo0o0o.colorphoria.effecthelpers.PicEditBitmapLoaderAsyncTask
import com.oo0o0o.colorphoria.effects.*
import com.oo0o0o.colorphoria.reusableuserinterface.PaletteChooserDialog
import com.oo0o0o.colorphoria.utils.Utils
import com.oo0o0o.colorphoria.utils.Utils.SavecolorphoriaBitmapHandler
import kotlinx.android.synthetic.main.activity_pic_edit.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import java.util.*

class PicEditActivityKotlin : LoggedInActivity() {
    private var bmW: BitmapWrapper? = null
    private var undoQueue = ArrayDeque<BitmapWrapper>()
    private var redoQueue = ArrayDeque<BitmapWrapper>()
    private var processingImageDone = false
    private var sourceUri: Uri? = null
    private var randomSeed = 0
    private val colorPalette = ArrayList<Int>()
    //var adView: AdView? = null
//    var interstitialAd: InterstitialAd? = null
    private var firebaseAnalytics: FirebaseAnalytics? = null

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i("OpenCV", "OpenCV loaded successfully")
                    val imageMat = Mat()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    // Begin activity lifecycle handlers
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pic_edit)
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
        Utils.setupcolorphoriaRecentAppsBar(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp)
        undoQueue = ArrayDeque()
        redoQueue = ArrayDeque()

        // So that the effects aren't the same every time the activity is restarted,
        // the random seed itself needs to be random
        val randSeedGen = Random()
        randomSeed = randSeedGen.nextInt()
        setUIforImageDoneProcessing(false)
//        interstitialAd = InterstitialAd(this)
//        interstitialAd?.adUnitId = resources.getString(R.string.admob_ad_unit_id_interstitial_share)
//        interstitialAd?.adListener = object : AdListener() {
//            override fun onAdLoaded() {
//                // Code to be executed when an ad finishes loading.
//            }
//            override fun onAdFailedToLoad(errorCode: Int) {
//                // Code to be executed when an ad request fails.
//                Log.w(TAG, "Interstitial ad failed to load, error code: $errorCode")
//            }
//            override fun onAdOpened() {
//                // Code to be executed when the ad is displayed.
//            }
//            override fun onAdLeftApplication() {
//                // Code to be executed when the user has left the app.
//            }
//            override fun onAdClosed() {
//                interstitialAd?.loadAd(AdRequest.Builder().build())
//                // Code to be executed when when the interstitial ad is closed.
//                // This function call below assumes that permission has already been procured
//                // for storage access, because the ad would not have been shown without it
//                saveImageAndStartShareActivity()
//            }
//        }
//        interstitialAd?.loadAd(AdRequest.Builder().build())
        if (savedInstanceState != null) {
            sourceUri = savedInstanceState.getParcelable(SOURCE_URI_STATE_KEY)

            // Have to do this in place of the to do above. Same steps as new intent below
            bmW = BitmapWrapper()
            val bmwAsync = PicEditBitmapLoaderAsyncTask(this,
                    applicationContext,
                    theImage,
                    sourceUri,
                    bmW,
                    original_button,
                    random_bleed_button,
                    threshold_button,
                    random_line_button,
                    random_flood_button,
                    invert_button,
                    light_outline_button,
                    outline_button,
                    fade_fill_button,
                    fade_tint_button,
                    effectCtrl, randomSeed)
            bmwAsync.execute()
        } else if (intent.hasExtra(SOURCE_URI)) {
            sourceUri = intent.getParcelableExtra<Parcelable>(SOURCE_URI) as Uri
            val iv = findViewById<View>(R.id.theImage) as ImageView
            bmW = BitmapWrapper()
            val bmwAsync = PicEditBitmapLoaderAsyncTask(this, applicationContext, iv, sourceUri, bmW,
                    original_button,
                    random_bleed_button,
                    threshold_button,
                    random_line_button,
                    random_flood_button,
                    invert_button,
                    light_outline_button,
                    outline_button,
                    fade_fill_button,
                    fade_tint_button,
                    effectCtrl, randomSeed)
            bmwAsync.execute()
        } else {
            // TODO: Error/bug, shouldn't get here
            setUIforImageDoneProcessing(false)
            val ctrlLayout = findViewById<View>(R.id.effect_ctrl_layout) as LinearLayout
            ctrlLayout.visibility = View.GONE
        }

        effect_ctrl_seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                refreshEffectThumbnails(randomSeed)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            }
        })

        original_button.setOnClickListener { view ->
            if (processingImageDone) onOriginal(view)
        }
        random_bleed_button.setOnClickListener { view ->
            if (processingImageDone) onRandomBleedFill(view)
        }
        threshold_button.setOnClickListener { view ->
            if (processingImageDone) onThreshold(view)
        }
        random_line_button.setOnClickListener { view ->
            if (processingImageDone) onRandomLineFill(view)
        }
        random_flood_button.setOnClickListener { view ->
            if (processingImageDone) onRandomFloodFill(view)
        }
        invert_button.setOnClickListener { view ->
            if (processingImageDone) onInvert(view)
        }
        light_outline_button.setOnClickListener { view ->
            if (processingImageDone) onLightOutlineEffect(view)
        }
        outline_button.setOnClickListener { view ->
            if (processingImageDone) onOutlineEffect(view)
        }
        fade_fill_button.setOnClickListener { view ->
            if (processingImageDone) onFadeFillEffect(view)
        }
        fade_tint_button.setOnClickListener { view ->
            if (processingImageDone) onFadeTintEffect(view)
        }

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, PicEditActivityKotlin::class.java.name)
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }
//
//    override fun onPause() {
//        adView?.pause()
//        super.onPause()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        adView?.resume()
//    }
//
//    override fun onDestroy() {
//        adView?.destroy()
//        super.onDestroy()
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (bmW != null) {
            outState.putParcelable(SOURCE_URI_STATE_KEY, sourceUri)
            outState.putInt(ROTATION_ANGLE_STATE_KEY, bmW?.rotationAngle ?: 0)
        } else {
            outState.putParcelable(SOURCE_URI_STATE_KEY, null)
            outState.putInt(ROTATION_ANGLE_STATE_KEY, 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.pic_edit_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // Only enable the menu items (which act on the image) when the image is done processing
        val enableMenuItems = processingImageDone
        for (i in 0 until menu.size()) {
            if (menu.getItem(i).itemId == R.id.home) {
                // do nothing
            } else {
                menu.getItem(i).isEnabled = enableMenuItems
            }
        }
        if (enableMenuItems) {
            if (undoQueue.size > 0) {
                val undoItem = menu.findItem(R.id.pic_edit_menu_undo)
                undoItem.isEnabled = true
            } else {
                val undoItem = menu.findItem(R.id.pic_edit_menu_undo)
                undoItem.isEnabled = false
            }
            if (redoQueue.size > 0) {
                val undoItem = menu.findItem(R.id.pic_edit_menu_redo)
                undoItem.isEnabled = true
            } else {
                val undoItem = menu.findItem(R.id.pic_edit_menu_redo)
                undoItem.isEnabled = false
            }
        }
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.pic_edit_menu_undo -> {
                onUndo()
                true
            }
            R.id.pic_edit_menu_redo -> {
                onRedo()
                true
            }
            R.id.pic_edit_menu_save -> {
                onSave()
                true
            }
            R.id.pic_edit_menu_share -> {
                onShare()
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }

    // End activity lifecycle handlers
    private fun setUIforImageDoneProcessing(processingImageDone: Boolean) {
        this.processingImageDone = processingImageDone
        invalidateOptionsMenu()
        if (processingImageDone) {
            progress_bar.visibility = View.GONE
        } else {
            progress_bar.visibility = View.VISIBLE
        }

        // Disable buttons in bottom control panel (effects buttons/rotate button/palette button)
        icon_linear_layout.childCount
        for (i in 0 until icon_linear_layout.childCount) {
            icon_linear_layout.getChildAt(i).isClickable = processingImageDone
        }
        choose_palette.isClickable = processingImageDone
        rotate_right.isClickable = processingImageDone
        effect_ctrl_seekbar.isEnabled = processingImageDone
    }

    private fun onRedo() {
        undoEnqueue()
        bmW = redoQueue.removeLast()
        if (redoQueue.isEmpty()) {
            invalidateOptionsMenu()
        }
        invalidateOptionsMenu()
        theImage.setImageBitmap(bmW?.modifiedBitmap)
    }

    private fun onUndo() {
        redoEnqueue()
        bmW = undoQueue.removeLast()
        if (undoQueue.isEmpty()) {
            invalidateOptionsMenu()
        }
        theImage.setImageBitmap(bmW?.modifiedBitmap)
    }

    private fun onSave() {
        saveCurrImageStoragePermission()
    }

    private fun onShare() {
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SHARE, Bundle())
        shareCurrImageStoragePermission()
    }

    fun onRotateRight(v: View?) {
        applyEffect(RotateEffect())
    }

    fun onChoosePalette(v: View?) {
        val colorPaletteDialog = PaletteChooserDialog(this, colorPalette)
        // inside of the PaletteChooserDialog class
        colorPaletteDialog.setOnDismissListener {
            if (colorPaletteDialog.isColorPaletteChanged) {
                refreshEffectThumbnails(randomSeed)
            }
        }
        colorPaletteDialog.show()
    }

    private fun onOriginal(v: View?) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, RandomBleedFillEffect::class.java.name)
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
        applyEffect(ResetEffect())
    }

    private fun onRandomBleedFill(v: View?) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, RandomBleedFillEffect::class.java.name)
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
        applyEffect(RandomBleedFillEffect())
    }

    private fun onThreshold(v: View?) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, ThresholdEffect::class.java.name)
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
        applyEffect(ThresholdEffect())
    }

    private fun onRandomLineFill(v: View?) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, RandomLineFillEffect::class.java.name)
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
        applyEffect(RandomLineFillEffect())
    }

    private fun onRandomFloodFill(v: View?) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, RandomFloodFillEffect::class.java.name)
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
        applyEffect(RandomFloodFillEffect())
    }

    private fun onInvert(v: View?) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, InvertEffect::class.java.name)
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
        applyEffect(InvertEffect())
    }

    private fun onLightOutlineEffect(v: View?) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "light" + OutlineEffect::class.java.name)
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
        applyEffect(OutlineEffect(false))
    }

    private fun onOutlineEffect(v: View?) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, OutlineEffect::class.java.name)
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
        applyEffect(OutlineEffect(true))
    }

    private fun onFadeFillEffect(v: View?) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, FadeFillEffect::class.java.name)
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
        applyEffect(FadeFillEffect())
    }

    private fun onFadeTintEffect(v: View?) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "tint" + FadeFillEffect::class.java.name)
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
        applyEffect(FadeFillEffect(true))
    }

    private fun refreshEffectThumbnails(randomSeed: Int) {
        // Populate the effect preview buttons
        BitmapEffectPreviewAsyncTask(this,
                original_button,
                ResetEffect(), bmW?.sourceBitmap, effectCtrl,
                randomSeed, colorPalette).execute()
        BitmapEffectPreviewAsyncTask(this,
                random_bleed_button,
                RandomBleedFillEffect(), bmW?.sourceBitmap, effectCtrl,
                randomSeed, colorPalette).execute()
        BitmapEffectPreviewAsyncTask(this,
                random_flood_button,
                RandomFloodFillEffect(), bmW?.sourceBitmap, effectCtrl,
                randomSeed, colorPalette).execute()
        BitmapEffectPreviewAsyncTask(this,
                random_line_button,
                RandomLineFillEffect(), bmW?.sourceBitmap, effectCtrl,
                randomSeed, colorPalette).execute()
        BitmapEffectPreviewAsyncTask(this,
                findViewById<View>(R.id.invert_button) as ImageButton,
                InvertEffect(), bmW?.sourceBitmap, effectCtrl,
                randomSeed, colorPalette).execute()
        BitmapEffectPreviewAsyncTask(this,
                light_outline_button,
                OutlineEffect(false), bmW?.sourceBitmap, effectCtrl,
                randomSeed, colorPalette).execute()
        BitmapEffectPreviewAsyncTask(this,
                outline_button,
                OutlineEffect(true), bmW?.sourceBitmap, effectCtrl,
                randomSeed, colorPalette).execute()
        BitmapEffectPreviewAsyncTask(this,
                threshold_button,
                ThresholdEffect(), bmW?.sourceBitmap, effectCtrl,
                randomSeed, colorPalette).execute()
        BitmapEffectPreviewAsyncTask(this,
                fade_fill_button,
                FadeFillEffect(), bmW?.sourceBitmap, effectCtrl,
                randomSeed, colorPalette).execute()
        BitmapEffectPreviewAsyncTask(this,
                fade_tint_button,
                FadeFillEffect(true), bmW?.sourceBitmap, effectCtrl,
                randomSeed, colorPalette).execute()
    }

    // public to be used by BitmapEffectAsyncTask
    fun doneProgress() {
        val progressBar = findViewById<View>(R.id.progress_bar) as ProgressBar
        progressBar.visibility = View.GONE
        setUIforImageDoneProcessing(true)
    }

    // End UI Helpers
    // Begin business logic
    private fun undoEnqueue() {
        if (undoQueue.size >= MAX_UNDO_QUEUE_SIZE) {
            undoQueue.removeFirst()
        }
        val undoCopy = BitmapWrapper(bmW?.sourceBitmap?.copy(bmW?.sourceBitmap?.config, true))
        undoCopy.modifiedBitmap = bmW?.modifiedBitmap?.copy(bmW?.modifiedBitmap?.config, true)
        undoQueue.addLast(undoCopy)
        invalidateOptionsMenu()
    }

    private fun redoEnqueue() {
        // TODO: Do we need to check if the size gets exceeded? think its handled by undo checking
        val redoCopy = BitmapWrapper(bmW?.sourceBitmap?.copy(bmW?.sourceBitmap?.config, true))
        redoCopy.modifiedBitmap = bmW?.modifiedBitmap?.copy(bmW?.modifiedBitmap?.config, true)
        redoQueue.addLast(redoCopy)
        invalidateOptionsMenu()
    }

    private fun updateUndoRedoQueues() {
        redoQueue.clear()
        invalidateOptionsMenu()
        undoEnqueue()
    }

    private fun applyEffect(effect: BitmapEffect) {
        (findViewById<View>(R.id.progress_bar) as ProgressBar).visibility = View.VISIBLE
        updateUndoRedoQueues()
        setUIforImageDoneProcessing(false)
        val ctrl = effectCtrl
        BitmapEffectAsyncTask(this, theImage, effect, bmW, ctrl, randomSeed, colorPalette).execute()
        val randSeedGen = Random()
        randomSeed = randSeedGen.nextInt()
        refreshEffectThumbnails(randomSeed)
    }

    private val effectCtrl: Double
        private get() {
            val progress = effect_ctrl_seekbar.progress.toDouble()
            val max = effect_ctrl_seekbar.max.toDouble()
            return 1 - progress / max
        }

    private fun saveCurrImageStoragePermission() {
        if (Utils.getPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        getString(R.string.permission_write_external_storage_explanation),
                        REQUEST_PERMISSIONS_CALLBACK_SAVE_WRITE_EXTERNAL_STORAGE
                ) { Utils.showCustomToast(this@PicEditActivityKotlin, getString(R.string.unable_to_save_image)) }) {
            saveCurrImage()
        }
    }

    private fun saveCurrImage() {
        Utils.savecolorphoriaBitmap(bmW?.modifiedBitmap, this, object : SavecolorphoriaBitmapHandler {
            override fun onSuccess(uri: Uri) {
                Utils.showCustomToast(this@PicEditActivityKotlin, getString(R.string.image_saved))
            }

            override fun onFailure() {
                // do nothing
                Utils.showCustomToast(this@PicEditActivityKotlin, "Save failed.")
            }
        })
    }

    private fun shareCurrImageStoragePermission() {
        if (Utils.getPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        getString(R.string.permission_write_external_storage_explanation),
                        REQUEST_PERMISSIONS_CALLBACK_SAVE_WRITE_EXTERNAL_STORAGE
                ) { Utils.showCustomToast(this@PicEditActivityKotlin, getString(R.string.unable_to_save_image)) }) {
            showInterstitial()
        }
    }

    private fun showInterstitial() {
//        if (interstitialAd?.isLoaded ?: false) {
//            interstitialAd?.show()
//        } else {
//            Log.d(TAG, "The interstitial wasn't loaded yet.")
            saveImageAndStartShareActivity()
//        }
    }

    private fun saveImageAndStartShareActivity() {
        Utils.savecolorphoriaBitmap(bmW?.modifiedBitmap, this, object : SavecolorphoriaBitmapHandler {
            override fun onSuccess(uri: Uri) {
                startShareActivity(uri)
            }
            override fun onFailure() {
                // do nothing
                Utils.showCustomToast(this@PicEditActivityKotlin, "Save failed.")
            }
        })
    }

    private fun startShareActivity(uri: Uri) {
        val intent = Intent(this, UploadActivity::class.java)
        intent.putExtra(UploadActivity.IMAGE_URI, uri)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSIONS_CALLBACK_SAVE_WRITE_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, so continue.
                    saveCurrImage()
                } else {
                    Utils.showCustomToast(this@PicEditActivityKotlin, getString(R.string.unable_to_save_image))
                }
                return
            }
        }
    }

    companion object {
        private val TAG = PicEditActivityKotlin::class.java.name
        const val SOURCE_URI = "SOURCE_URI"
        private const val SOURCE_URI_STATE_KEY = "SOURCE_STATE_KEY"
        private const val ROTATION_ANGLE_STATE_KEY = "ROTATION_ANGLE_STATE_KEY"
        const val REQUEST_PERMISSIONS_CALLBACK_SAVE_WRITE_EXTERNAL_STORAGE = 0
        private const val MAX_UNDO_QUEUE_SIZE = 2
    }
}