package com.example.peacemode

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class first_page : AppCompatActivity() {

    companion object {
        const val REQUEST_ALARM_PERMISSION = 1
    }

    private lateinit var taskListView: ListView
    private lateinit var profileImageView: CircleImageView
    private lateinit var userNameTextView: TextView
    private lateinit var userDetailsTextView: TextView

    private lateinit var databaseReference: DatabaseReference
    private val currentUserEmail: String? = FirebaseAuth.getInstance().currentUser?.email

    @SuppressLint("MissingInflatedId", "ObjectAnimatorBinding")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.first_page)

        taskListView = findViewById(R.id.taskListView)
        profileImageView = findViewById(R.id.profile_image)
        userNameTextView = findViewById(R.id.user_name)
        userDetailsTextView = findViewById(R.id.user_details)

        databaseReference = FirebaseDatabase.getInstance().getReference("users")

        // Fetch and display user profile details
        currentUserEmail?.let { email ->
            fetchUserProfile(email)
        }

        // Other setup code
        setupUI()
    }

    private fun setupUI() {
        val settingImageView: ImageView = findViewById(R.id.setting)
        settingImageView.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            this@first_page.overridePendingTransition(
                R.anim.animate_card_enter,
                R.anim.animate_card_exit
            )
        }

        val appLogo = findViewById<ImageView>(R.id.app_logo)
        appLogo.setOnClickListener {
            appLogo.animate().apply {
                duration = 1000
                rotationYBy(360f)
            }.start()
        }

        val popupButton: FloatingActionButton = findViewById(R.id.popup_Button)
        popupButton.setOnClickListener { view ->
            showPopupMenu(view)
        }

        val calendarImageView: ImageView = findViewById(R.id.calender)
        calendarImageView.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
            this@first_page.overridePendingTransition(
                R.anim.animate_card_enter,
                R.anim.animate_card_exit
            )
        }

        val attendanceImageView: ImageView = findViewById(R.id.attendance)
        attendanceImageView.setOnClickListener {
            val intent = Intent(this, AttendanceActivity::class.java)
            startActivity(intent)
            this@first_page.overridePendingTransition(
                R.anim.animate_card_enter,
                R.anim.animate_card_exit
            )
        }

        val timerImageView: ImageView = findViewById(R.id.timer)
        timerImageView.setOnClickListener {
            val intent = Intent(this, SetTimerActivity::class.java)
            startActivity(intent)
            this@first_page.overridePendingTransition(
                R.anim.animate_card_enter,
                R.anim.animate_card_exit
            )
        }

        val sosImageView: ImageView = findViewById(R.id.sos)
        sosImageView.setOnClickListener {
            val intent = Intent(this, SilentModeHandler::class.java)
            startActivity(intent)
            this@first_page.overridePendingTransition(
                R.anim.animate_card_enter,
                R.anim.animate_card_exit
            )
        }

        val locationImageView: ImageView = findViewById(R.id.location)
        locationImageView.setOnClickListener {
            val intent = Intent(this, LocationDetailsActivity::class.java)
            startActivity(intent)
            this@first_page.overridePendingTransition(
                R.anim.animate_card_enter,
                R.anim.animate_card_exit
            )
        }

        val menuHostImageView: ImageView = findViewById(R.id.menu_host)
        menuHostImageView.setOnClickListener {
            val intent = Intent(this, HostActivity::class.java)
            startActivity(intent)
            this@first_page.overridePendingTransition(
                R.anim.animate_card_enter,
                R.anim.animate_card_exit
            )
        }

        loadTasks()

        if (!hasAlarmPermission()) {
            requestAlarmPermission()
        }
    }

    private fun fetchUserProfile(email: String) {
        val userRef = databaseReference.child(email.replace(".", ","))
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    userNameTextView.text = user.name
                    userDetailsTextView.text = user.profession
                    Picasso.get().load(user.profileImageUrl).into(profileImageView)
                } else {
                    Toast.makeText(this@first_page, "User data is null", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@first_page, "Failed to load profile: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun hasAlarmPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.USE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAlarmPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM, Manifest.permission.USE_EXACT_ALARM),
            REQUEST_ALARM_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ALARM_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) ||
                    (grantResults.isNotEmpty() && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted
                } else {
                    // Permission denied, handle accordingly
                }
                return
            }
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.arrow_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_ringer -> {
                    setRingerMode(AudioManager.RINGER_MODE_NORMAL)
                    true
                }
                R.id.menu_vibrate -> {
                    setRingerMode(AudioManager.RINGER_MODE_VIBRATE)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun setRingerMode(mode: Int) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = mode
    }

    private fun loadTasks() {
        val sharedPreferences = getSharedPreferences("tasks", MODE_PRIVATE)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val tasks = sharedPreferences.getStringSet(currentDate, mutableSetOf()) ?: mutableSetOf()
        if (tasks.isNotEmpty()) {
            val taskArray = tasks.toTypedArray()
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, taskArray)
            taskListView.adapter = adapter

            Log.d("FirstPage", "Tasks loaded for date $currentDate: ${tasks.joinToString()}")
        } else {
            Log.d("FirstPage", "No tasks for date $currentDate")
            Toast.makeText(this, "No tasks for today", Toast.LENGTH_SHORT).show()
        }
    }
}
