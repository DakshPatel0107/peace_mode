package com.example.peacemode

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class DisplayActivity : AppCompatActivity() {

    private lateinit var themeRadioGroup: RadioGroup
    private lateinit var fontSizeSeekBar: SeekBar
    private lateinit var sampleText: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.display_activity)

        sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)

        themeRadioGroup = findViewById(R.id.theme_radio_group)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()  // Handle the back button press
        }

        val lightModeRadioButton = findViewById<RadioButton>(R.id.light_mode_radio_button)
        val darkModeRadioButton = findViewById<RadioButton>(R.id.dark_mode_radio_button)

        // Set the radio button state based on the saved theme preference
        val isDarkMode = sharedPreferences.getBoolean("isDarkMode", false)
        if (isDarkMode) {
            darkModeRadioButton.isChecked = true
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            lightModeRadioButton.isChecked = true
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.light_mode_radio_button -> {
                    sharedPreferences.edit().putBoolean("isDarkMode", false).apply()
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                R.id.dark_mode_radio_button -> {
                    sharedPreferences.edit().putBoolean("isDarkMode", true).apply()
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        }


    }
}
