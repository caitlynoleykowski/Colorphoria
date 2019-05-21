package com.oo0o0o.colorphoria.activities.loggedinactivities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.oo0o0o.colorphoria.R
import com.oo0o0o.colorphoria.activities.loggedoutactivites.LoginActivity
import com.oo0o0o.colorphoria.activities.loggedoutactivites.SignupActivity
import com.oo0o0o.colorphoria.utils.Utils
import kotlinx.android.synthetic.main.account_fragment.*

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

        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                // user auth state is changed - user is null
                // launch login activity
                startActivity(Intent(activity, LoginActivity::class.java))
                activity?.finish()
            }
        }

        progressBar.visibility = View.GONE

        changeEmailButton.setOnClickListener(View.OnClickListener {
            if (newEmailEditText.getVisibility() == View.GONE) {
                hideInputs()
                newEmailEditText.setVisibility(View.VISIBLE)
            } else {
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
            }
        })

        changePasswordButton.setOnClickListener(View.OnClickListener {
            if (newPasswordEditText.visibility == View.GONE) {
                hideInputs()
                newPasswordEditText.visibility = View.VISIBLE
            } else {
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
            }
        })

        resetPasswordButton.setOnClickListener(View.OnClickListener {
            hideInputs()
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
            hideInputs()
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
            hideInputs()
            (activity as LoggedInActivity?)?.signOut()
        })

    }


    private fun hideInputs() {
        // Hide the inputs until the user selects the option that requires them
        newEmailEditText.visibility = View.GONE
        newPasswordEditText.visibility = View.GONE
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
}
