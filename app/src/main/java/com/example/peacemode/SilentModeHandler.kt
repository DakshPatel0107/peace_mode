package com.example.peacemode

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SilentModeHandler : AppCompatActivity() {

    private lateinit var numberEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var savedNumberTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences

    private val phoneStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent?.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            val savedNumber = sharedPreferences.getString("saved_number", null)
            if (state == TelephonyManager.EXTRA_STATE_RINGING && incomingNumber == savedNumber) {
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_silent_mode_handler)

        numberEditText = findViewById(R.id.numberEditText)
        saveButton = findViewById(R.id.saveButton)
        savedNumberTextView = findViewById(R.id.savedNumberTextView)

        sharedPreferences = getSharedPreferences("SilentModePrefs", Context.MODE_PRIVATE)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()  // Handle the back button press
        }

        val savedNumber = sharedPreferences.getString("saved_number", null)
        if (savedNumber != null) {
            savedNumberTextView.text = "Saved Number: $savedNumber"
        }

        saveButton.setOnClickListener {
            val number = numberEditText.text.toString()
            sharedPreferences.edit().putString("saved_number", number).apply()
            savedNumberTextView.text = "Saved Number: $number"
            Toast.makeText(this, "Number saved!", Toast.LENGTH_SHORT).show()
        }

        registerReceiver(phoneStateReceiver, IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(phoneStateReceiver)
    }
}
