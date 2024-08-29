package com.example.peacemode

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceActivity : AppCompatActivity() {

    private lateinit var departmentSpinner: Spinner
    private lateinit var attendanceList: LinearLayout
    private lateinit var saveButton: Button
    private lateinit var viewAttendanceButton: Button
    private lateinit var viewAllAttendanceButton: Button
    private lateinit var dateTextView: TextView
    private lateinit var databaseReference: DatabaseReference
    private val currentUserEmail: String? = FirebaseAuth.getInstance().currentUser?.email

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        departmentSpinner = findViewById(R.id.department_spinner)
        attendanceList = findViewById(R.id.attendance_list)
        saveButton = findViewById(R.id.save_button)
        viewAttendanceButton = findViewById(R.id.view_attendance)
        viewAllAttendanceButton = findViewById(R.id.view_all_attendance_button)
        dateTextView = findViewById(R.id.date_text_view)

        databaseReference = FirebaseDatabase.getInstance().getReference("departments")

        fetchDepartments()
        updateDateTextView()

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()  // Handle the back button press
        }

        saveButton.setOnClickListener {
            saveAttendance()
        }

        viewAttendanceButton.setOnClickListener {
            navigateToReport()
        }

        viewAllAttendanceButton.setOnClickListener {
            navigateToAllAttendance()
        }
    }

    private fun updateDateTextView() {
        val currentDate = getCurrentDate()
        dateTextView.text = "Mark attendance for date: $currentDate"
    }

    private fun navigateToReport() {
        val intent = Intent(this, ViewAttendanceActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToAllAttendance() {
        val selectedDepartmentId = departmentSpinner.selectedItem.toString()
        val intent = Intent(this, ViewAllAttendanceActivity::class.java)
        intent.putExtra("departmentId", selectedDepartmentId)
        startActivity(intent)
    }

    private fun fetchDepartments() {
        databaseReference.get().addOnSuccessListener { dataSnapshot ->
            val departmentNames = mutableListOf<String>()
            val departmentIds = mutableListOf<String>()

            dataSnapshot.children.forEach { departmentSnapshot ->
                val department = departmentSnapshot.getValue(HostActivity.Department::class.java)
                if (department?.createdBy == currentUserEmail) {
                    department?.name?.let { departmentNames.add(it) }
                    departmentIds.add(departmentSnapshot.key!!)
                }
            }

            departmentSpinner.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                departmentNames
            )

            departmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedDepartmentId = departmentIds[position]
                    fetchMembers(selectedDepartmentId)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    private fun fetchMembers(departmentId: String) {
        databaseReference.child(departmentId).child("members").get().addOnSuccessListener { dataSnapshot ->
            attendanceList.removeAllViews()

            val userNames = mutableMapOf<String, String>() // To store email and corresponding name

            // Fetch user names
            val usersRef = FirebaseDatabase.getInstance().getReference("users")
            dataSnapshot.children.forEach { memberSnapshot ->
                val memberEmail = memberSnapshot.key?.replace(",", ".") ?: ""
                // Store email to look up later
                userNames[memberEmail] = ""
            }

            // Fetch names for all emails
            userNames.keys.forEach { email ->
                val userRef = usersRef.child(email.replace(".", ","))
                userRef.get().addOnSuccessListener { userSnapshot ->
                    val user = userSnapshot.getValue(User::class.java)
                    user?.name?.let { name ->
                        userNames[email] = name
                        // Update UI only if all names are fetched
                        if (userNames.size == dataSnapshot.childrenCount.toInt()) {
                            updateUIWithUserNames(userNames)
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to fetch user details", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUIWithUserNames(userNames: Map<String, String>) {
        attendanceList.removeAllViews()
        userNames.forEach { (email, name) ->
            val checkBox = CheckBox(this)
            checkBox.text = name
            attendanceList.addView(checkBox)
        }
    }

    private fun saveAttendance() {
        val departmentId = departmentSpinner.selectedItem.toString()
        val attendanceRef = databaseReference.child(departmentId).child("attendances")

        // Get the current date
        val currentDate = getCurrentDate()

        // Iterate through all CheckBoxes to save their state
        for (i in 0 until attendanceList.childCount) {
            val view = attendanceList.getChildAt(i)
            if (view is CheckBox) {
                val userName = view.text.toString()
                val userEmail = getEmailFromName(userName)
                val isPresent = view.isChecked
                attendanceRef.child(currentDate).child(userEmail).setValue(isPresent)
            }
        }
        Toast.makeText(this, "Attendance saved", Toast.LENGTH_SHORT).show()
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun getEmailFromName(userName: String): String {
        // This function should convert userName back to email.
        // If you have a map of names to emails, use it here.
        // Otherwise, you might need to fetch the email based on the name from Firebase.
        return userName // Replace with actual implementation if needed
    }

}
