package com.example.peacemode

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LocationDetailsActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_details)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()  // Handle the back button press
        }

        // Remove the map-related code
    }


    // Add a method to check if the user is in a specific area
    private fun isUserInSilenceArea(latitude: Double, longitude: Double): Boolean {
        // Define the silence area's coordinates (e.g., a geofence)
        val silenceAreaLatitude = 37.7749
        val silenceAreaLongitude = -122.4194
        val silenceAreaRadius = 100 // meters

        // Calculate the distance between the user's location and the silence area
        val distance = distanceBetweenTwoPoints(latitude, longitude, silenceAreaLatitude, silenceAreaLongitude)

        // Check if the user is within the silence area
        return distance <= silenceAreaRadius
    }

    private fun distanceBetweenTwoPoints(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    // Add a method to silence the phone
    private fun silencePhone() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        Toast.makeText(this, "Silence mode enabled", Toast.LENGTH_SHORT).show()
    }

    // Add a method to restore the phone's original ringer mode
    private fun restoreRingerMode() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        Toast.makeText(this, "Silence mode disabled", Toast.LENGTH_SHORT).show()
    }

}