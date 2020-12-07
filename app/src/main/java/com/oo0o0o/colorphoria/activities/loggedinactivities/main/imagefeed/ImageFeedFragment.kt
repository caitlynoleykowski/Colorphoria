package com.oo0o0o.colorphoria.activities.loggedinactivities.main.imagefeed

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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.room.Room
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes

import com.oo0o0o.colorphoria.R
import com.oo0o0o.colorphoria.activities.loggedinactivities.main.BottomNavActivity
import com.oo0o0o.colorphoria.backgroundtasks.GetImageAsyncTask
import com.oo0o0o.colorphoria.backgroundtasks.RestHelperKotlinRx
import com.oo0o0o.colorphoria.model.AppDatabase
import com.oo0o0o.colorphoria.model.MediaEntity
import com.oo0o0o.colorphoria.reusableuserinterface.ProportionalImageView
import com.oo0o0o.colorphoria.utils.Utils
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
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

    private var disposables = CompositeDisposable()

    private lateinit var viewModel: ImageFeedViewModel

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

        viewModel = ViewModelProviders.of(this, ImageFeedViewModelFactory(activity!!.applicationContext)).get(ImageFeedViewModel::class.java)

        adapter = ImageAdapter(activity!!.applicationContext)
        imageListView.adapter = adapter

        swipeRefreshLayout.setColorSchemeColors(
                ResourcesCompat.getColor(resources, R.color.colorSecondary, activity?.theme)
        )

        firebaseAnalytics = FirebaseAnalytics.getInstance((activity as AppCompatActivity))
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, BottomNavActivity::class.java.name)
        firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

        viewModel.mediaEntities.observe(this, Observer<List<MediaEntity>> { mediaEntities ->
            loadFeed(mediaEntities)
        })

        viewModel.fetchComplete.observe(this, Observer<Boolean> {
        })
    }

    override fun onResume() {
        super.onResume()

        val refreshes = swipeRefreshLayout.refreshes()
                .toFlowable(BackpressureStrategy.BUFFER)
                .doOnNext {
                    swipeRefreshLayout.isRefreshing = true
                }

        val inputs = ImageFeedViewModel.Inputs(
                refreshes
        )
        viewModel.bindUI(inputs)
    }

    override fun onPause() {
        super.onPause()

        viewModel.unsubscribe()
        disposables.clear()
    }

    private fun loadFeed(mediaEntities: List<MediaEntity>) {
        adapter?.isDoneLoading = true
        adapter?.clear() // Remove all images (image urls) from the adapter
        for (i in mediaEntities.indices) {
            adapter?.addItem(mediaEntities[i])
        }
        adapter?.notifyDataSetChanged() // Make list view pick up changes
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


