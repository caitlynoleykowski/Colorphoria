package com.oo0o0o.colorphoria.activities.loggedoutactivites

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth
import com.oo0o0o.colorphoria.R
import com.oo0o0o.colorphoria.activities.loggedinactivities.main.BottomNavActivity

import kotlinx.android.synthetic.main.activity_starting.*

class StartingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.LoggedOutTheme)

        super.onCreate(savedInstanceState)

        //Get Firebase auth instance
        val auth = FirebaseAuth.getInstance()
        if (auth.getCurrentUser() != null) {
            startActivity(Intent(this@StartingActivity, BottomNavActivity::class.java))
            finish()
        }

        setContentView(R.layout.activity_starting)

        viewFlipper.isAutoStart = true
        viewFlipper.setFlipInterval(1200)
        viewFlipper.setInAnimation(this, R.anim.view_transition_in_left);
        viewFlipper.setOutAnimation(this, R.anim.view_transition_out_left);

        loginButton.setOnClickListener {
            // start the new activity
            val intent = Intent(this@StartingActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        signupButton.setOnClickListener {
            startActivity(Intent(this@StartingActivity, BirthdayActivity::class.java))
        }
    }

}
