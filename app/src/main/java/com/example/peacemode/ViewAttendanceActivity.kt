package com.example.peacemode

import android.graphics.Color
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ViewAttendanceActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var departmentSpinner: Spinner
    private lateinit var statusText: TextView
    private lateinit var databaseReference: DatabaseReference
    private val currentUserEmail: String? = FirebaseAuth.getInstance().currentUser?.email
    private var currentUserName: String? = null
    private val userAttendanceDates = mutableMapOf<String, Boolean>()
    private var selectedDepartmentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_attendance)

        calendarView = findViewById(R.id.calendar_view)
        departmentSpinner = findViewById(R.id.department_spinner)
        statusText = findViewById(R.id.status_text)
        databaseReference = FirebaseDatabase.getInstance().getReference("departments")

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()  // Handle the back button press
        }

        fetchCurrentUserName()
        fetchDepartments()

        departmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                selectedDepartmentId = departmentSpinner.getItemAtPosition(position) as String
                if (selectedDepartmentId != null) {
                    fetchAttendance(selectedDepartmentId!!)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle case when nothing is selected
            }
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time)

            val attendanceStatus = userAttendanceDates[selectedDate] ?: false
            statusText.text = "$selectedDate: ${if (attendanceStatus) "Present" else "Absent"}"
            statusText.setTextColor(if (attendanceStatus) Color.GREEN else Color.RED)
        }
    }

    private fun fetchCurrentUserName() {
        // Assuming you store the current user's name in the "users" node
        val usersReference = FirebaseDatabase.getInstance().getReference("users")
        currentUserEmail?.replace(".", ",")?.let { emailKey ->
            usersReference.child(emailKey).get().addOnSuccessListener { dataSnapshot ->
                currentUserName = dataSnapshot.child("name").getValue(String::class.java)
            }.addOnFailureListener {
                // Handle the error
            }
        }
    }

    private fun fetchDepartments() {
        databaseReference.get().addOnSuccessListener { dataSnapshot ->
            val departmentNames = mutableListOf<String>()
            val departmentIds = mutableListOf<String>()

            dataSnapshot.children.forEach { departmentSnapshot ->
                val department = departmentSnapshot.getValue(HostActivity.Department::class.java)
                val currentEmailKey = currentUserEmail?.replace(".", ",")

                // Check if the department's members contain the current user's email
                if (department?.members?.containsKey(currentEmailKey) == true) {
                    departmentNames.add(department.name ?: "Unknown")
                    departmentIds.add(departmentSnapshot.key!!)
                }
            }

            departmentSpinner.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                departmentNames
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }.addOnFailureListener {
            // Handle the error
        }
    }

    private fun fetchAttendance(departmentId: String) {
        databaseReference.child(departmentId).child("attendances")
            .get().addOnSuccessListener { dataSnapshot ->
                userAttendanceDates.clear()
                dataSnapshot.children.forEach { dateSnapshot ->
                    val date = dateSnapshot.key ?: ""
                    dateSnapshot.children.forEach { attendanceSnapshot ->
                        val memberName = attendanceSnapshot.key ?: ""
                        val isPresent = attendanceSnapshot.getValue(Boolean::class.java) ?: false
                        if (currentUserName == memberName) {
                            userAttendanceDates[date] = isPresent
                        }
                    }
                }
            }.addOnFailureListener {
                // Handle the error
            }
    }
}
