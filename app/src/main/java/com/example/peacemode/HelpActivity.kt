package com.example.peacemode

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HelpActivity:  AppCompatActivity() {

    private lateinit var versionCheckButton: Button
    private lateinit var contactUsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.help_activity)

        versionCheckButton = findViewById(R.id.version_check)
        contactUsButton = findViewById(R.id.Contact_us)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()  // Handle the back button press
        }

        versionCheckButton.setOnClickListener {
            checkForUpdates()
        }

        contactUsButton.setOnClickListener {
            showContactInfo()
        }
    }

    private fun checkForUpdates() {
        // TO DO: Implement logic to check for updates
        // For example, you can make a API call to your server to check for updates
        // or use a library like Google Play Core to check for updates

        // For demonstration purposes, let's assume we have a new version available
        val newVersionAvailable = true

        if (newVersionAvailable) {
            Toast.makeText(this, "New version available! Please update.", Toast.LENGTH_SHORT).show()
            // You can also open the Google Play Store or your app's update page here
        } else {
            Toast.makeText(this, "You're up to date!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showContactInfo() {
        val contactInfo = "Contact us at dakshpatel0107@gmail.com (Email)"
        Toast.makeText(this, contactInfo, Toast.LENGTH_LONG).show()
    }
}