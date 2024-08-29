package com.example.peacemode

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val firebase: DatabaseReference = FirebaseDatabase.getInstance().getReference()

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            // User is not authenticated, redirect to SignupActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {

            // Delayed task to start the new activity after 5 seconds
            Handler().postDelayed({
                // Start the new activity
                val intent = Intent(this, first_page::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_up, R.anim.slide_up)
                // Finish the current activity (optional)
                finish()
            }, 2000) // 2000 milliseconds = 2 seconds
        }
    }
}