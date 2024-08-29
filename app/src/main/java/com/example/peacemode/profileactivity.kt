package com.example.peacemode

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.UUID

class profileactivity : AppCompatActivity() {
    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var professionEditText: EditText
    private lateinit var dobEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var actionButton: Button

    private var selectedPhotoUri: Uri? = null
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profileactivity)

        profileImageView = findViewById(R.id.profile_image)
        nameEditText = findViewById(R.id.user_name)
        professionEditText = findViewById(R.id.user_details)
        dobEditText = findViewById(R.id.dob)
        phoneEditText = findViewById(R.id.phone_number)
        emailEditText = findViewById(R.id.email)
        actionButton = findViewById(R.id.action_button)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()  // Handle the back button press
        }


        databaseReference = FirebaseDatabase.getInstance().getReference("users")

        profileImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        actionButton.setOnClickListener {
            if (actionButton.text == "Edit") {
                enableEditing(true)
                actionButton.text = "Save"
            } else {
                saveProfile()
                enableEditing(false)
                actionButton.text = "Edit"
            }
        }

        enableEditing(false)
        fetchUserProfile()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            profileImageView.setImageBitmap(bitmap)
        }
    }

    private fun enableEditing(enable: Boolean) {
        nameEditText.isEnabled = enable
        professionEditText.isEnabled = enable
        dobEditText.isEnabled = enable
        phoneEditText.isEnabled = enable
        emailEditText.isEnabled = enable
    }

    private fun saveProfile() {
        val name = nameEditText.text.toString()
        val profession = professionEditText.text.toString()
        val dob = dobEditText.text.toString()
        val phone = phoneEditText.text.toString()
        val email = emailEditText.text.toString()

        Log.d("ProfileActivity", "Saving profile with name: $name, profession: $profession, dob: $dob, phone: $phone, email: $email")

        if (selectedPhotoUri == null) {
            Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show()
            return
        }

        uploadImageToFirebaseStorage(name, profession, dob, phone, email)
    }

    private fun uploadImageToFirebaseStorage(name: String, profession: String, dob: String, phone: String, email: String) {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("ProfileActivity", "Image uploaded successfully, URL: $uri")
                    saveUserToFirebaseDatabase(uri.toString(), name, profession, dob, phone, email)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload image: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String, name: String, profession: String, dob: String, phone: String, email: String) {
        val user = User(name, profession, dob, phone, email, profileImageUrl)
        Log.d("ProfileActivity", "Saving user to database: $user")
        databaseReference.child(email.replace(".", ",")).setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save profile: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUserProfile() {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: return
        val userRef = databaseReference.child(email.replace(".", ","))

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    Log.d("ProfileActivity", "Fetched user data: $user")
                    nameEditText.setText(user.name)
                    professionEditText.setText(user.profession)
                    dobEditText.setText(user.dob)
                    phoneEditText.setText(user.phone)
                    emailEditText.setText(user.email)
                    Picasso.get().load(user.profileImageUrl).into(profileImageView)
                } else {
                    Log.e("ProfileActivity", "User data is null")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@profileactivity, "Failed to load profile: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

data class User(
    val name: String = "",
    val profession: String = "",
    val dob: String = "",
    val phone: String = "",
    val email: String = "",
    val profileImageUrl: String = ""
)
