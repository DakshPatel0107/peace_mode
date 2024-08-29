package com.example.peacemode

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var shareAppButton: TextView
    private lateinit var logoutButton: TextView
    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        auth = FirebaseAuth.getInstance()

        shareAppButton = findViewById(R.id.share_app_button)
        logoutButton = findViewById(R.id.logout_button)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()  // Handle the back button press
        }

        val userProfileButton = findViewById<TextView>(R.id.user_profile_text)
        userProfileButton.setOnClickListener {
            startActivity(Intent(this, profileactivity::class.java))
        }

        val helpButton = findViewById<TextView>(R.id.info_help)
        helpButton.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        val rateButton = findViewById<TextView>(R.id.rate_us)
        rateButton.setOnClickListener {
            startActivity(Intent(this, ratinactivity::class.java))
        }

        val themeDisplayButton = findViewById<TextView>(R.id.theme_display_text)
        themeDisplayButton.setOnClickListener {
            startActivity(Intent(this, DisplayActivity::class.java))
        }

        val languageRegionButton = findViewById<TextView>(R.id.language_region_text)
        languageRegionButton.setOnClickListener {
            startActivity(Intent(this, LanguageRegionActivity::class.java))
        }

        shareAppButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Hey! I'm using this awesome app ${getString(R.string.app_name)} and I think you should try it too! https://play.google.com/store/apps/details?id=${applicationContext.packageName}"
            )
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
