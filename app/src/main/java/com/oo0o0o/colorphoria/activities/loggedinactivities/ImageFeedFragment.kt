package com.oo0o0o.colorphoria.activities.loggedinactivities

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.room.Room
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson

import com.oo0o0o.colorphoria.R
import com.oo0o0o.colorphoria.backgroundtasks.GetImageAsyncTask
import com.oo0o0o.colorphoria.backgroundtasks.RestHelper
import com.oo0o0o.colorphoria.backgroundtasks.RestHelperKotlinRx
import com.oo0o0o.colorphoria.data.AppDatabase
import com.oo0o0o.colorphoria.data.MediaEntity
import com.oo0o0o.colorphoria.reusableuserinterface.ProportionalImageView
import com.oo0o0o.colorphoria.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_image_feed.*
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormat
import java.util.ArrayList

class ImageFeedFragment : Fragment() {

    // TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    val ARG_PARAM1 = "param1"
    val ARG_PARAM2 = "param2"

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AccountFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                ImageFeedFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }

    private val TAG = ImageFeedFragment::class.java.name

    private var adapter: ImageAdapter? = null

    private var refreshingFeed = false

    private var firebaseAnalytics: FirebaseAnalytics? = null

    private lateinit var viewModel: ImageFeedViewModel

    private var disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_feed, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Utils.setupcolorphoriaRecentAppsBar(activity)

        (activity as AppCompatActivity?)?.supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayShowCustomEnabled(true)
            setCustomView(R.layout.actionbar_logo)
        }

        MobileAds.initialize(activity, resources.getString(R.string.admob_app_id))

        viewModel = ViewModelProviders.of(this).get(ImageFeedViewModel::class.java)

        // Handle image feed
        // Create the adapter to convert the array to views
        adapter = ImageAdapter(activity!!.applicationContext)
        // Attach the adapter to a ListView
        val listView = imageListView

        listView.adapter = adapter

        swipeRefreshLayout.setOnRefreshListener { getMyImages() }
        swipeRefreshLayout.setColorSchemeColors(
                ResourcesCompat.getColor(resources, R.color.colorSecondary, activity?.theme)
        )

        firebaseAnalytics = FirebaseAnalytics.getInstance((activity as AppCompatActivity))
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, BottomNavActivity::class.java.name)
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    override fun onResume() {
        super.onResume()

        viewModel.getData(activity?.application?.applicationContext) {
            if (it.isNotEmpty()) {
                loadFeed(it)
            }
            getMyImages(false)
        }
    }

    override fun onPause() {
        super.onPause()

        viewModel.unsubscribe()
        disposables.clear()
    }

    private fun getMyImages(showRefreshing: Boolean = true) {
        if (!refreshingFeed) {
            adapter?.isDoneLoading = false
            refreshingFeed = true
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.isRefreshing = showRefreshing

                val appContext = activity!!.applicationContext
                val user = FirebaseAuth.getInstance().currentUser

                user ?: return

                RestHelperKotlinRx.recentImages(appContext, user)
                        .subscribeOn(Schedulers.io())
                        .map { response ->
                            val db = Room.databaseBuilder(appContext,
                                    AppDatabase::class.java, "colorphoria-database").build()
                            db.mediaDao().deleteAll()

                            val mediaEntities = ArrayList<MediaEntity>()

                            try {
                                val responseJson = JSONObject(response)
                                if (responseJson.has("mediaEntities")) {
                                    // TODO: Handle this better in case somehow it has "urls" but the array is empty

                                    val mediaEntitiesJson = responseJson.getJSONArray("mediaEntities")
                                    val gson = Gson()
                                    for (i in 0 until mediaEntitiesJson.length()) {
                                        val mediaEntity = gson.fromJson(mediaEntitiesJson.get(i).toString(), MediaEntity::class.java)
                                        mediaEntity.id = i.toLong()
                                        db.mediaDao().insertAll(mediaEntity) // Change id from i
                                        mediaEntities.add(mediaEntity)
                                    }
                                }
                            } catch (e: JSONException) {
                                // TODO: Handle this
                                Log.i(this.javaClass.name, "Error parsing json.", e)
                            }

                            return@map mediaEntities
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe ({
                            loadFeed(it)
                        }, {
                            // TODO
                            object : AsyncTask<String, String, List<MediaEntity>>() {
                                override fun doInBackground(vararg strings: String): List<MediaEntity> {
                                    val db = Room.databaseBuilder(appContext,
                                            AppDatabase::class.java, "colorphoria-database").build()
                                    return db.mediaDao().all
                                }

                                override fun onPostExecute(mediaEntities: List<MediaEntity>) {
                                    super.onPostExecute(mediaEntities)
                                    // TODO: Figure out why in the world networkResponse is null and come back to this
                                    // TODO: so that we don't have to just loadFeed as if there's no images for today
                                    Utils.showCustomToast(activity, "Unable to refresh feed")
                                    loadFeed(mediaEntities)
                                    refreshingFeed = false
                                    swipeRefreshLayout?.isRefreshing = false
                                }
                            }.execute()
                        }).addTo(disposables)
            }
        }
    }

    private fun loadFeed(mediaEntities: List<MediaEntity>) {
        adapter?.isDoneLoading = true
        adapter?.clear() // Remove all images (image urls) from the adapter
        for (i in mediaEntities.indices) {
            adapter?.addItem(mediaEntities[i])
        }
        adapter?.notifyDataSetChanged() // Make list view pick up changes
        refreshingFeed = false
        swipeRefreshLayout?.isRefreshing = false
    }
}

// Image Adapter
// Adapter for Grid View
class ImageAdapter(private val context: Context) : BaseAdapter() {
    private val imageUrlList: ArrayList<MediaEntity>
    var isDoneLoading = false

    init {
        this.imageUrlList = ArrayList()
    }

    fun addItem(mediaEntity: MediaEntity) {
        imageUrlList.add(mediaEntity)
    }

    fun addAll(mediaEntities: List<MediaEntity>) {
        imageUrlList.addAll(mediaEntities)
    }

    fun clear() {
        imageUrlList.clear()
    }

    override fun getCount(): Int {
        // If doneLoading and the list is empty,
        // return 1 in order to show the feed unavailable image
        return if (imageUrlList.size == 0 && isDoneLoading) {
            1
        } else {
            imageUrlList.size
        }
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertViewVar = convertView
        var holder = ViewHolder()

        if (convertViewVar == null) {
            holder.picturesView = ProportionalImageView(context)
            //Converting the Row Layout to be used in Grid View
            convertViewVar = LayoutInflater.from(context).inflate(R.layout.image_row, parent, false)

            convertViewVar!!.tag = holder

        } else {
            holder = convertViewVar.tag as ViewHolder
        }

        if (imageUrlList.size != 0) {
            holder.picturesView = convertViewVar.findViewById(R.id.imageview) as ProportionalImageView
//            holder.titleTextView = convertView.findViewById(R.id.title_text_view) as TextView
            holder.dateTextView = convertViewVar.findViewById(R.id.date_text_view) as TextView
            holder.commentTextView = convertViewVar.findViewById(R.id.comment_text_view) as TextView
            if (position < imageUrlList.size) {
                val url = imageUrlList[position].downloadUrl
                // Only try to fetch the image and set it on the image view if it's a new image
                if (!(holder.urlSource ?: "").equals(url)) {
                    val progressBar = convertViewVar.findViewById(R.id.image_row_progress_bar) as ProgressBar
                    progressBar.visibility = View.VISIBLE

                    holder.urlSource = url
                    holder.picturesView?.setImageDrawable(context.getDrawable(R.drawable.bg_image_placeholder))
                    // This will wind up with pictures out of order if one async task finishes later than one that started later
                    // But it works well enough and its unlikely that the scenario will happen often
                    GetImageAsyncTask(context, holder.picturesView, progressBar, url).execute()
//                holder.titleTextView?.setText(imageUrlList[position].username)
                    holder.dateTextView?.setText(DateFormat.getDateInstance().format(imageUrlList[position].timestamp))
                    holder.commentTextView?.setText(
                            if (imageUrlList[position].comment != null) imageUrlList[position].comment else "")
                }
            } else {
                // Not sure if I should set the image here to blank/empty
//                holder.titleTextView?.setText("")
                holder.dateTextView?.setText("")
                holder.commentTextView?.setText("")
                holder.urlSource = null
            }
        } else {
            holder.picturesView = convertViewVar.findViewById(R.id.imageview) as ProportionalImageView
//            holder.titleTextView = convertView.findViewById(R.id.title_text_view) as TextView
            holder.dateTextView = convertViewVar.findViewById(R.id.date_text_view) as TextView
            holder.commentTextView = convertViewVar.findViewById(R.id.comment_text_view) as TextView

            holder.picturesView?.setImageDrawable(context.getDrawable(R.drawable.img_feed_unavailable))
//            holder.titleTextView?.setText("")
            holder.dateTextView?.setText("")
            holder.commentTextView?.setText("")
            holder.urlSource = null
        }

        return convertViewVar
    }
}

// View Holder pattern used for Smooth Scrolling. As View Holder pattern recycle the findViewById() object.
data class ViewHolder (
        var urlSource: String? = null,
        var picturesView: ProportionalImageView? = null,
        var titleTextView: TextView? = null,
        var dateTextView: TextView? = null,
        var commentTextView: TextView? = null
)
