package com.example.peacemode

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ratinactivity : AppCompatActivity() {

    private lateinit var ratingBar: RatingBar
    private lateinit var feedbackEditText: EditText
    private lateinit var sendFeedbackButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rate_us)

        ratingBar = findViewById(R.id.rating_bar)
        feedbackEditText = findViewById(R.id.feedback_edit_text)
        sendFeedbackButton = findViewById(R.id.send_feedback_button)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()  // Handle the back button press
        }

        ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            // update the rating display
            val ratingText = "Rating: $rating/5"
            Toast.makeText(this, ratingText, Toast.LENGTH_SHORT).show()
        }

        sendFeedbackButton.setOnClickListener {
            val feedback = feedbackEditText.text.toString()
            if (feedback.isNotEmpty()) {
                // send the feedback to your server or email
                Toast.makeText(this, "Feedback sent! Thank you for your feedback.", Toast.LENGTH_SHORT).show()
                // clear the feedback edit text
                feedbackEditText.text.clear()
            } else {
                Toast.makeText(this, "Please enter your feedback.", Toast.LENGTH_SHORT).show()
            }
        }

    }
}