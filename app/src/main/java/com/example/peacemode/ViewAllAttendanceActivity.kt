package com.example.peacemode

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale

class ViewAllAttendanceActivity : AppCompatActivity() {

    private lateinit var datePicker: DatePicker
    private lateinit var attendanceListLayout: LinearLayout
    private lateinit var databaseReference: DatabaseReference
    private val currentUserEmail: String? = FirebaseAuth.getInstance().currentUser?.email

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_attendance)

        datePicker = findViewById(R.id.date_picker)
        attendanceListLayout = findViewById(R.id.attendance_list_layout)
        databaseReference = FirebaseDatabase.getInstance().getReference("departments")

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()  // Handle the back button press
        }


        datePicker.setOnDateChangedListener { _, year, month, dayOfMonth ->
            val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                GregorianCalendar(year, month, dayOfMonth).time
            )
            val selectedDepartmentId = intent.getStringExtra("departmentId")
            selectedDepartmentId?.let {
                fetchAttendanceRecordsForDate(it, selectedDate)
            }
        }
    }

    private fun fetchAttendanceRecordsForDate(departmentId: String, date: String) {
        attendanceListLayout.removeAllViews()
        databaseReference.child(departmentId).child("attendances").child(date)
            .get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    dataSnapshot.children.forEach { attendanceSnapshot ->
                        val memberEmail = attendanceSnapshot.key?.replace(",", ".") ?: ""
                        val isPresent = attendanceSnapshot.getValue(Boolean::class.java) ?: false

                        val attendanceTextView = TextView(this)
                        attendanceTextView.text = "${getNameFromEmail(memberEmail)}: ${if (isPresent) "Present" else "Absent"}"
                        attendanceTextView.setTextColor(if (isPresent) Color.GREEN else Color.RED)
                        attendanceListLayout.addView(attendanceTextView)
                    }
                } else {
                    val noRecordsTextView = TextView(this)
                    noRecordsTextView.text = "No attendance records for $date"
                    attendanceListLayout.addView(noRecordsTextView)
                }
            }
    }

    private fun getNameFromEmail(email: String): String {
        // Implement logic to get the user's name from their email.
        // This can involve fetching the user information from Firebase.
        return email // Replace with actual implementation if needed
    }
}
