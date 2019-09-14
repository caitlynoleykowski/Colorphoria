package com.oo0o0o.colorphoria.activities.loggedinactivities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.oo0o0o.colorphoria.R
import com.oo0o0o.colorphoria.activities.loggedoutactivites.LoginActivity
import com.oo0o0o.colorphoria.activities.loggedoutactivites.SignupActivity
import com.oo0o0o.colorphoria.backgroundtasks.GetImageAsyncTask
import com.oo0o0o.colorphoria.backgroundtasks.RestHelper
import com.oo0o0o.colorphoria.data.AppDatabase
import com.oo0o0o.colorphoria.data.MyMediaEntity
import com.oo0o0o.colorphoria.reusableuserinterface.SquareImageView
import com.oo0o0o.colorphoria.utils.Utils
import kotlinx.android.synthetic.main.account_fragment.*
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

/**
 * A simple [Fragment] subclass.
 * Use the [AccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class AccountFragment : Fragment() {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    val ARG_PARAM1 = "param1"
    val ARG_PARAM2 = "param2"

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var authListener: FirebaseAuth.AuthStateListener? = null
    private var auth: FirebaseAuth? = null

    // Google login vars (used for logout in this activity)
    private val googleApiClient: GoogleApiClient? = null


    private var adapter: MyImageAdapter? = null
    private var refreshingFeed = false

    private var firebaseAnalytics: FirebaseAnalytics? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.account_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Get firebase auth instance
        auth = FirebaseAuth.getInstance()

        // Get current user
        val user = FirebaseAuth.getInstance().currentUser
        usernameTextView.text = user?.email

        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val updatedUser = firebaseAuth.currentUser
            if (updatedUser == null) {
                // user auth state is changed - user is null
                // launch login activity
                startActivity(Intent(activity, LoginActivity::class.java))
                activity?.finish()
            }
        }

        progressBar.visibility = View.GONE

        settingsButton.setOnClickListener {
            // Toggle settings layout visibility
            settingsLayout.visibility = if (settingsLayout.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        changeEmailButton.setOnClickListener(View.OnClickListener {
                progressBar.setVisibility(View.VISIBLE)
                if (user != null && newEmailEditText.getText().toString().trim({ it <= ' ' }) != "") {
                    user.updateEmail(newEmailEditText.getText().toString().trim({ it <= ' ' }))
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    progressBar.setVisibility(View.GONE)
                                    Utils.showAlertDialog(
                                            activity,
                                            getString(R.string.change_email_success),
                                            getString(R.string.ok),
                                            { confirmDialog ->
                                                (activity as LoggedInActivity?)?.signOut()
                                                confirmDialog.dismiss()
                                            },
                                            false)
                                } else {
                                    progressBar.setVisibility(View.GONE)
                                    val failedMessage: String
                                    if (task.exception is FirebaseAuthUserCollisionException) {
                                        failedMessage = getString(R.string.change_email_failed_existing_user)
                                    } else if (task.exception is FirebaseAuthRecentLoginRequiredException) {
                                        failedMessage = getString(R.string.change_email_failed_recent_login)
                                    } else {
                                        failedMessage = getString(R.string.change_email_failed)
                                    }
                                    Utils.showAlertDialog(
                                            activity,
                                            failedMessage,
                                            getString(R.string.ok),
                                            { confirmDialog ->
                                                // Do nothing, just close the dialog
                                                confirmDialog.dismiss()
                                            },
                                            false)
                                }
                            }
                } else if (newEmailEditText.getText().toString().trim({ it <= ' ' }) == "") {
                    newEmailEditText.error = getString(R.string.empty_email_input)
                    progressBar.visibility = View.GONE
                }
        })

        changePasswordButton.setOnClickListener(View.OnClickListener {
                progressBar.visibility = View.VISIBLE
                if (user != null && newPasswordEditText.getText().toString().trim({ it <= ' ' }) != "") {
                    if (newPasswordEditText.text.toString().trim({ it <= ' ' }).length < 6) {
                        newPasswordEditText.error = getString(R.string.password_too_short)
                        progressBar.visibility = View.GONE
                    } else {
                        user.updatePassword(newPasswordEditText.getText().toString().trim({ it <= ' ' }))
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        progressBar.setVisibility(View.GONE)
                                        Utils.showAlertDialog(
                                                activity,
                                                getString(R.string.change_password_success),
                                                getString(R.string.ok),
                                                { confirmDialog ->
                                                    (activity as LoggedInActivity?)?.signOut()
                                                    confirmDialog.dismiss()
                                                },
                                                false)
                                    } else {
                                        progressBar.setVisibility(View.GONE)
                                        Utils.showAlertDialog(
                                                activity,
                                                getString(R.string.change_password_failed),
                                                getString(R.string.ok),
                                                { confirmDialog ->
                                                    // Do nothing, just close the dialog
                                                    confirmDialog.dismiss()
                                                },
                                                false)
                                    }
                                }
                    }
                } else if (newPasswordEditText.getText().toString().trim({ it <= ' ' }) == "") {
                    newPasswordEditText.setError(getString(R.string.empty_password_input))
                    progressBar.setVisibility(View.GONE)
                }
        })

        resetPasswordButton.setOnClickListener(View.OnClickListener {
            progressBar.visibility = View.VISIBLE
            auth?.currentUser?.email?.trim({ it <= ' ' })?.let {
                auth?.sendPasswordResetEmail(it)?.addOnCompleteListener(OnCompleteListener<Void> { task ->
                            if (task.isSuccessful) {
                                progressBar.setVisibility(View.GONE)
                                Utils.showAlertDialog(
                                        activity,
                                        getString(R.string.reset_password_success),
                                        getString(R.string.ok),
                                        { confirmDialog ->
                                            (activity as LoggedInActivity?)?.signOut()
                                            confirmDialog.dismiss()
                                        },
                                        false)
                            } else {
                                progressBar.setVisibility(View.GONE)
                                Utils.showAlertDialog(
                                        activity,
                                        getString(R.string.reset_password_failed),
                                        getString(R.string.ok),
                                        { confirmDialog ->
                                            // Do nothing, just close the dialog
                                            confirmDialog.dismiss()
                                        },
                                        false)
                            }
                        })
            } ?: run {
                progressBar.visibility = View.GONE
            }
        })

        deleteAccountButton.setOnClickListener(View.OnClickListener {
            Utils.showConfirmDialog(
                    activity,
                    getString(R.string.delete_account_confirmation),
                    getString(R.string.yes),
                    getString(R.string.cancel),
                    object : Utils.ConfirmDialogHandler {
                        override fun onPositiveButton(confirmDialog: Dialog) {
                            deleteUser(user)
                            confirmDialog.dismiss()
                        }

                        override fun onNegativeButton(confirmDialog: Dialog) {
                            // Do nothing, just close the dialog
                            confirmDialog.dismiss()
                        }
                    },
                    false)
        })

        signOutButton.setOnClickListener(View.OnClickListener {
            (activity as LoggedInActivity?)?.signOut()
        })






        // Handle images
        // Create the adapter to convert the array to views
        adapter = MyImageAdapter(activity!!.applicationContext)
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
        object : AsyncTask<String, String, List<MyMediaEntity>>() {
            override fun doInBackground(vararg strings: String): List<MyMediaEntity> {
                val db = Room.databaseBuilder(appcontext,
                        AppDatabase::class.java, "colorphoria-database")
                        .fallbackToDestructiveMigration()
                        .build()

                return db.myMediaDao().all
            }

            override fun onPostExecute(mediaEntities: List<MyMediaEntity>) {
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

    fun deleteUser(user: FirebaseUser?) {
        progressBar.visibility = View.VISIBLE
        user?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                (activity as LoggedInActivity?)?.googleSignOut() // Have to do this when user is deleted in case it was a google sign in
                progressBar.setVisibility(View.GONE)
                Utils.showAlertDialog(
                        activity,
                        getString(R.string.delete_account_success),
                        getString(R.string.ok),
                        { confirmDialog ->
                            // Go to signup activity
                            startActivity(Intent(activity, SignupActivity::class.java))
                            activity?.finish()
                            confirmDialog.dismiss()
                        },
                        false)
            } else {
                progressBar.visibility = View.GONE
                Utils.showAlertDialog(
                        activity,
                        getString(R.string.delete_account_failed),
                        getString(R.string.ok),
                        { confirmDialog ->
                            // Do nothing, just close the dialog
                            confirmDialog.dismiss()
                        },
                        false)
            }
        }
    }


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
                AccountFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
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
                    object : AsyncTask<String, String, List<MyMediaEntity>>() {
                        override fun doInBackground(vararg strings: String): List<MyMediaEntity> {
                            val db = Room.databaseBuilder(appContext,
                                    AppDatabase::class.java, "colorphoria-database").build()
                            db.myMediaDao().deleteAll()

                            val mediaEntities = ArrayList<MyMediaEntity>()

                            try {
                                val responseJson = JSONObject(response)
                                if (responseJson.has("mediaEntities")) {
                                    // TODO: Handle this better in case somehow it has "urls" but the array is empty

                                    val mediaEntitiesJson = responseJson.getJSONArray("mediaEntities")
                                    val gson = Gson()
                                    for (i in 0 until mediaEntitiesJson.length()) {
                                        val mediaEntity = gson.fromJson(mediaEntitiesJson.get(i).toString(), MyMediaEntity::class.java)
                                        mediaEntity.id = i.toLong()
                                        db.myMediaDao().insertAll(mediaEntity) // Change id from i
                                        mediaEntities.add(mediaEntity)
                                    }
                                }
                            } catch (e: JSONException) {
                                // TODO: Handle this
                                Log.i(this.javaClass.name, "Error parsing json.", e)
                            }

                            return mediaEntities
                        }

                        override fun onPostExecute(mediaEntities: List<MyMediaEntity>) {
                            super.onPostExecute(mediaEntities)
                            loadFeed(mediaEntities)

                        }
                    }.execute(response)
                }

                override fun onFailure(error: String) {
                    // AsyncTask needed for db access to fetch media urls
                    object : AsyncTask<String, String, List<MyMediaEntity>>() {
                        override fun doInBackground(vararg strings: String): List<MyMediaEntity> {
                            val db = Room.databaseBuilder(appContext,
                                    AppDatabase::class.java, "colorphoria-database").build()
                            return db.myMediaDao().all
                        }

                        override fun onPostExecute(mediaEntities: List<MyMediaEntity>) {
                            super.onPostExecute(mediaEntities)
                            // TODO: Figure out why in the world networkResponse is null and come back to this
                            // TODO: so that we don't have to just loadFeed as if there's no images for today
                            Utils.showCustomToast(activity, "Unable to refresh feed")
                            loadFeed(mediaEntities)
                            refreshingFeed = false
                            swipeRefreshLayout?.isRefreshing = false
                        }
                    }.execute()
                }
            }
            RestHelper.myImages(appContext, user, callbacks)
        }
    }

    private fun loadFeed(mediaEntities: List<MyMediaEntity>) {
        adapter?.isDoneLoading = true
        adapter?.clear() // Remove all images (image urls) from the adapter
        for (i in mediaEntities.indices) {
            adapter?.addItem(mediaEntities[i])
        }
        adapter?.notifyDataSetChanged() // Make list view pick up changes
        if (adapter?.count == 0) {
            noPostsInfoText.visibility = View.VISIBLE
        } else {
            noPostsInfoText.visibility = View.GONE
        }
        refreshingFeed = false
        swipeRefreshLayout?.isRefreshing = false
    }
}

// Image Adapter
// Adapter for Grid View
class MyImageAdapter(private val context: Context) : BaseAdapter() {
    private val imageUrlList: ArrayList<MyMediaEntity>
    var isDoneLoading = false

    init {
        this.imageUrlList = ArrayList()
    }

    fun addItem(mediaEntity: MyMediaEntity) {
        imageUrlList.add(mediaEntity)
    }

    fun addAll(mediaEntities: List<MyMediaEntity>) {
        imageUrlList.addAll(mediaEntities)
    }

    fun clear() {
        imageUrlList.clear()
    }

    override fun getCount(): Int {
        return imageUrlList.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertViewVar = convertView
        var holder = MyViewHolder()

        if (convertViewVar == null) {
            holder.picturesView = SquareImageView(context)
            //Converting the Row Layout to be used in Grid View
            convertViewVar = LayoutInflater.from(context).inflate(R.layout.account_image_item, parent, false)

            convertViewVar!!.tag = holder

        } else {
            holder = convertViewVar.tag as MyViewHolder
        }

        if (imageUrlList.size != 0) {

            holder.picturesView = convertViewVar.findViewById(R.id.imageview) as ImageView
            if (position < imageUrlList.size) {
                val url = imageUrlList[position].downloadUrl
                // Only try to fetch the image and set it on the image view if it's a new image
                if (!(holder.urlSource ?: "").equals(url)) {
                    val progressBar = convertViewVar.findViewById(R.id.image_row_progress_bar) as ProgressBar
//                  progressBar.visibility = View.VISIBLE

                    holder.urlSource = url
                    holder.picturesView?.setImageDrawable(context.getDrawable(R.drawable.bg_image_placeholder))
                    // This will wind up with pictures out of order if one async task finishes later than one that started later
                    // But it works well enough and its unlikely that the scenario will happen often
                    GetImageAsyncTask(context, holder.picturesView, progressBar, url).execute()
                }
            } else {
                // Not sure if I should set the image here to blank/empty
                holder.urlSource = null
            }
        } else {
            holder.picturesView = convertViewVar.findViewById(R.id.imageview) as ImageView

            holder.picturesView?.setImageDrawable(context.getDrawable(R.drawable.img_feed_unavailable))
            holder.urlSource = null
        }

        return convertViewVar
    }
}

// View Holder pattern used for Smooth Scrolling. As View Holder pattern recycle the findViewById() object.
data class MyViewHolder (
        var urlSource: String? = null,
        var picturesView: ImageView? = null
)
