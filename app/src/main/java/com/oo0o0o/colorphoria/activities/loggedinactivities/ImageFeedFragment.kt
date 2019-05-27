package com.oo0o0o.colorphoria.activities.loggedinactivities

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson

import com.oo0o0o.colorphoria.R
import com.oo0o0o.colorphoria.backgroundtasks.GetImageAsyncTask
import com.oo0o0o.colorphoria.backgroundtasks.RestHelper
import com.oo0o0o.colorphoria.data.AppDatabase
import com.oo0o0o.colorphoria.data.MediaEntity
import com.oo0o0o.colorphoria.utils.Utils
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

        val appcontext = activity!!.applicationContext
        // Load the media urls from the database and display the images (if in db, should be cached)
        // Then, attempt to fetch the urls from the server
        object : AsyncTask<String, String, List<MediaEntity>>() {
            override fun doInBackground(vararg strings: String): List<MediaEntity> {
                val db = Room.databaseBuilder(appcontext,
                        AppDatabase::class.java, "colorphoria-database")
                        .fallbackToDestructiveMigration()
                        .build()

                return db.mediaDao().all
            }

            override fun onPostExecute(mediaEntities: List<MediaEntity>) {
                super.onPostExecute(mediaEntities)
                if (mediaEntities.size > 0) {
                    loadFeed(mediaEntities)
                }
                // Call to get the images
                getMyImages(false)
            }
        }.execute()

        firebaseAnalytics = FirebaseAnalytics.getInstance((activity as AppCompatActivity))
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, BottomNavActivity::class.java.name)
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

    }

    private fun getMyImages(showRefreshing: Boolean = true) {
        if (!refreshingFeed) {
            adapter?.isDoneLoading = false
            refreshingFeed = true
            swipeRefreshLayout.isRefreshing = showRefreshing

            val appContext = activity!!.applicationContext
            val user = FirebaseAuth.getInstance().currentUser
            val callbacks = object : RestHelper.HttpCallbacks {
                override fun onSuccess(response: String) {
                    // AsyncTask needed for db access to save media urls from response
                    object : AsyncTask<String, String, List<MediaEntity>>() {
                        override fun doInBackground(vararg strings: String): List<MediaEntity> {
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

                            return mediaEntities
                        }

                        override fun onPostExecute(mediaEntities: List<MediaEntity>) {
                            super.onPostExecute(mediaEntities)
                            loadFeed(mediaEntities)

                        }
                    }.execute(response)
                }

                override fun onFailure(error: String) {
                    // AsyncTask needed for db access to fetch media urls
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
                            swipeRefreshLayout.isRefreshing = false
                        }
                    }.execute()
                }
            }
            RestHelper.myImages(appContext, user, callbacks)
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
        swipeRefreshLayout.isRefreshing = false
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
        var convertView = convertView
        var holder = ViewHolder()

        if (convertView == null) {
            holder.picturesView = ImageView(context)
            //Converting the Row Layout to be used in Grid View
            convertView = LayoutInflater.from(context).inflate(R.layout.image_row, parent, false)

            convertView!!.tag = holder

        } else {
            holder = convertView.tag as ViewHolder
        }

        if (imageUrlList.size != 0) {
            val progressBar = convertView.findViewById(R.id.image_row_progress_bar) as ProgressBar
            progressBar.visibility = View.VISIBLE

            holder.picturesView = convertView.findViewById(R.id.imageview) as ImageView
            holder.titleTextView = convertView.findViewById(R.id.title_text_view) as TextView
            holder.dateTextView = convertView.findViewById(R.id.date_text_view) as TextView
            holder.commentTextView = convertView.findViewById(R.id.comment_text_view) as TextView
            if (position < imageUrlList.size) {
                holder.picturesView?.setImageDrawable(context.getDrawable(R.drawable.bg_image_placeholder))
                // This will wind up with pictures out of order if one async task finishes later than one that started later
                // But it works well enough and its unlikely that the scenario will happen often
                GetImageAsyncTask(context, holder.picturesView, progressBar, imageUrlList[position].downloadUrl).execute()
                holder.titleTextView?.setText(imageUrlList[position].username)
                holder.dateTextView?.setText(DateFormat.getDateInstance().format(imageUrlList[position].timestamp))
                holder.commentTextView?.setText(
                        if (imageUrlList[position].comment != null) imageUrlList[position].comment else "")
            } else {
                // Not sure if I should set the image here to blank/empty
                holder.titleTextView?.setText("")
                holder.dateTextView?.setText("")
                holder.commentTextView?.setText("")
            }
        } else {
            holder.picturesView = convertView.findViewById(R.id.imageview) as ImageView
            holder.titleTextView = convertView.findViewById(R.id.title_text_view) as TextView
            holder.dateTextView = convertView.findViewById(R.id.date_text_view) as TextView
            holder.commentTextView = convertView.findViewById(R.id.comment_text_view) as TextView

            holder.picturesView?.setImageDrawable(context.getDrawable(R.drawable.img_feed_unavailable))
            holder.titleTextView?.setText("")
            holder.dateTextView?.setText("")
            holder.commentTextView?.setText("")
        }

        return convertView
    }
}

// View Holder pattern used for Smooth Scrolling. As View Holder pattern recycle the findViewById() object.
data class ViewHolder (
    var picturesView: ImageView? = null,
    var titleTextView: TextView? = null,
    var dateTextView: TextView? = null,
    var commentTextView: TextView? = null
)
