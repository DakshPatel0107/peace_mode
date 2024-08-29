package com.example.peacemode

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class LanguageRegionActivity : AppCompatActivity() {

    private lateinit var languageSpinner: Spinner
    private lateinit var regionSpinner: Spinner
    private lateinit var timeFormatSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_region)

        sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)

        languageSpinner = findViewById(R.id.language_spinner)
        regionSpinner = findViewById(R.id.region_spinner)
        timeFormatSpinner = findViewById(R.id.time_format_spinner)
        saveButton = findViewById(R.id.save_button)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()  // Handle the back button press
        }

        // Populate spinners
        setupSpinners()

        // Restore saved preferences
        restorePreferences()

        // Save preferences on button click
        saveButton.setOnClickListener {
            savePreferences()
        }
    }

    private fun setupSpinners() {
        val languages = arrayOf("English", "Spanish", "French", "German", "Chinese")
        val languageAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = languageAdapter

        val regions = arrayOf("US", "UK", "France", "Germany", "China")
        val regionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, regions)
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        regionSpinner.adapter = regionAdapter

        val timeFormats = arrayOf("12-hour", "24-hour")
        val timeFormatAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeFormats)
        timeFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeFormatSpinner.adapter = timeFormatAdapter
    }

    private fun restorePreferences() {
        val savedLanguage = sharedPreferences.getString("Language", "English")
        val savedRegion = sharedPreferences.getString("Region", "US")
        val savedTimeFormat = sharedPreferences.getString("TimeFormat", "12-hour")

        val languageIndex = (languageSpinner.adapter as ArrayAdapter<String>).getPosition(savedLanguage)
        val regionIndex = (regionSpinner.adapter as ArrayAdapter<String>).getPosition(savedRegion)
        val timeFormatIndex = (timeFormatSpinner.adapter as ArrayAdapter<String>).getPosition(savedTimeFormat)

        languageSpinner.setSelection(languageIndex)
        regionSpinner.setSelection(regionIndex)
        timeFormatSpinner.setSelection(timeFormatIndex)
    }

    private fun savePreferences() {
        val selectedLanguage = languageSpinner.selectedItem as String
        val selectedRegion = regionSpinner.selectedItem as String
        val selectedTimeFormat = timeFormatSpinner.selectedItem as String

        sharedPreferences.edit()
            .putString("Language", selectedLanguage)
            .putString("Region", selectedRegion)
            .putString("TimeFormat", selectedTimeFormat)
            .apply()

        // Apply changes
        applyLocale(selectedLanguage)
        applyRegionalSettings(selectedRegion, selectedTimeFormat)
    }

    private fun applyLocale(language: String) {
        val locale = when (language) {
            "English" -> Locale.ENGLISH
            "Spanish" -> Locale("es", "ES")
            "French" -> Locale.FRENCH
            "German" -> Locale.GERMAN
            "Chinese" -> Locale.CHINESE
            else -> Locale.ENGLISH
        }
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        recreate() // Refresh activity to apply locale change
    }

    private fun applyRegionalSettings(region: String, timeFormat: String) {
        // Example: Adjust date and time formatting
        val locale = when (region) {
            "US" -> Locale("en", "US")
            "UK" -> Locale("en", "GB")
            "France" -> Locale("fr", "FR")
            "Germany" -> Locale("de", "DE")
            "China" -> Locale("zh", "CN")
            else -> Locale.getDefault()
        }

        // Define date and time format patterns
        val timeFormatPattern = if (timeFormat == "24-hour") "HH:mm" else "hh:mm a"

        // Save the time format pattern
        sharedPreferences.edit()
            .putString("TimeFormat", timeFormatPattern)
            .apply()

        // Example usage
        val currentDate = java.util.Calendar.getInstance().time
        val formatter = java.text.SimpleDateFormat(timeFormatPattern, locale)
        val formattedDate = formatter.format(currentDate)

        // Log or display formattedDate to verify
        println("Formatted Date: $formattedDate")
    }

}
