package com.example.peacemode

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class HostActivity : AppCompatActivity() {

    private lateinit var departmentNameInput: EditText
    private lateinit var addDepartmentButton: Button
    private lateinit var departmentContainer: LinearLayout
    private lateinit var manageMembersButton: Button

    private lateinit var profileImageView: CircleImageView
    private lateinit var userNameTextView: TextView
    private lateinit var userDetailsTextView: TextView

    private lateinit var databaseReference: DatabaseReference
    private val currentUserEmail: String? = FirebaseAuth.getInstance().currentUser?.email
    private val currentUserId: String? = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)

        departmentNameInput = findViewById(R.id.department_name_input)
        addDepartmentButton = findViewById(R.id.add_department_button)
        departmentContainer = findViewById(R.id.department_container)
        manageMembersButton = findViewById(R.id.manage_members_button)

        profileImageView = findViewById(R.id.profile_image)
        userNameTextView = findViewById(R.id.user_name)
        userDetailsTextView = findViewById(R.id.user_details)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()  // Handle the back button press
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("departments")

        addDepartmentButton.setOnClickListener {
            addDepartment()
        }

        manageMembersButton.setOnClickListener {
            showManageMembersPopup()
        }

        fetchDepartments()

        currentUserId?.let {
            fetchUserProfile(it)
        }
    }

    private fun addDepartment() {
        val departmentName = departmentNameInput.text.toString().trim()
        if (departmentName.isEmpty()) {
            Toast.makeText(this, "Please enter a department name", Toast.LENGTH_SHORT).show()
            return
        }

        val departmentId = databaseReference.push().key ?: return
        val departmentData = Department(departmentName, currentUserEmail!!)

        databaseReference.child(departmentId).setValue(departmentData).addOnSuccessListener {
            Toast.makeText(this, "Department added", Toast.LENGTH_SHORT).show()
            fetchDepartments()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to add department", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showManageMembersPopup() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Manage Members")

        val input = EditText(this)
        input.hint = "Enter member emails separated by commas"
        builder.setView(input)

        builder.setPositiveButton("Add") { dialog, _ ->
            val membersEmails = input.text.toString().trim()
            if (membersEmails.isNotEmpty()) {
                val membersList = membersEmails.split(",").map { it.trim() }
                showDepartmentSelectionDialog { departmentId ->
                    addMembersToDepartment(departmentId, membersList)
                }
            } else {
                Toast.makeText(this, "Please enter valid emails", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNeutralButton("Remove") { dialog, _ ->
            val membersEmails = input.text.toString().trim()
            if (membersEmails.isNotEmpty()) {
                val membersList = membersEmails.split(",").map { it.trim() }
                showDepartmentSelectionDialog { departmentId ->
                    removeMembersFromDepartment(departmentId, membersList)
                }
            } else {
                Toast.makeText(this, "Please enter valid emails", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun showDepartmentSelectionDialog(onDepartmentSelected: (String) -> Unit) {
        val departmentIds = mutableListOf<String>()
        val departmentNames = mutableListOf<String>()

        databaseReference.get().addOnSuccessListener { dataSnapshot ->
            dataSnapshot.children.forEach { departmentSnapshot ->
                val department = departmentSnapshot.getValue(Department::class.java)
                if (department?.createdBy == currentUserEmail) {
                    departmentIds.add(departmentSnapshot.key!!)
                    if (department != null) {
                        department.name?.let { departmentNames.add(it) }
                    }
                }
            }

            val items = departmentNames.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("Select Department")
                .setItems(items) { _, which ->
                    val departmentId = departmentIds[which]
                    onDepartmentSelected(departmentId)
                }
                .show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load departments", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addMembersToDepartment(departmentId: String, membersList: List<String>) {
        val membersRef = databaseReference.child(departmentId).child("members")

        membersList.forEach { memberEmail ->
            // Check if the user exists in the database before adding
            val userEmail = memberEmail.replace(".", ",")
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userEmail)

            userRef.get().addOnSuccessListener { userSnapshot ->
                if (userSnapshot.exists()) {
                    // User exists, add to department
                    membersRef.child(userEmail).setValue(true)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Member $memberEmail added", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to add member $memberEmail", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // User does not exist
                    Toast.makeText(this, "User $memberEmail does not exist", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to check user existence", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeMembersFromDepartment(departmentId: String, membersList: List<String>) {
        val membersRef = databaseReference.child(departmentId).child("members")

        membersList.forEach { memberEmail ->
            val userEmail = memberEmail.replace(".", ",")
            membersRef.child(userEmail).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Member $memberEmail removed", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to remove member $memberEmail", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun fetchDepartments() {
        departmentContainer.removeAllViews() // Clear existing views
        databaseReference.get().addOnSuccessListener { dataSnapshot ->
            dataSnapshot.children.forEach { departmentSnapshot ->
                val department = departmentSnapshot.getValue(Department::class.java)
                if (department?.createdBy == currentUserEmail) {
                    val departmentView =
                        department?.let { it.name?.let { it1 ->
                            createDepartmentCard(departmentSnapshot.key!!,
                                it1
                            )
                        } }
                    departmentContainer.addView(departmentView)
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load departments", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createDepartmentCard(departmentId: String, departmentName: String): CardView {
        val cardView = CardView(this)

        // Set margins for the CardView to create distance between cards
        val cardLayoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            setMargins(16, 16, 16, 16) // Adjust the values as needed for top, left, right, bottom margins
        }
        cardView.layoutParams = cardLayoutParams
        cardView.radius = 8f
        cardView.setCardBackgroundColor(resources.getColor(R.color.white))

        // Use ConstraintLayout to position elements
        val constraintLayout = ConstraintLayout(this)
        val constraintLayoutParams = ConstraintLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        constraintLayout.layoutParams = constraintLayoutParams

        // Department Name TextView
        val textView = TextView(this)
        textView.text = departmentName
        textView.textSize = 18f
        textView.setPadding(16, 16, 0, 16) // Adjust padding as needed
        val textViewId = View.generateViewId()
        textView.id = textViewId
        val textViewParams = ConstraintLayout.LayoutParams(0, WRAP_CONTENT)
        textViewParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
        textViewParams.rightToLeft = View.generateViewId() // To be set with the delete button
        textViewParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        textViewParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        textViewParams.horizontalBias = 0f
        textView.layoutParams = textViewParams

        // Delete Button
        val deleteButton = Button(this)
        deleteButton.text = "Delete"
        val deleteButtonId = View.generateViewId()
        deleteButton.id = deleteButtonId
        val deleteButtonParams = ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        deleteButtonParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
        deleteButtonParams.leftToRight = textViewId
        deleteButtonParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        deleteButtonParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        deleteButtonParams.setMargins(16, 16, 0, 16)
        deleteButton.layoutParams = deleteButtonParams

        // Adding Views to ConstraintLayout
        constraintLayout.addView(textView)
        constraintLayout.addView(deleteButton)

        // Set onClick listener for the whole CardView
        cardView.setOnClickListener {
            showDepartmentDetails(departmentId)
        }

        // Set onClick listener for the delete button
        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(departmentId)
        }

        cardView.addView(constraintLayout)
        return cardView
    }



    private fun showDeleteConfirmationDialog(departmentId: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this department?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteDepartment(departmentId)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun deleteDepartment(departmentId: String) {
        databaseReference.child(departmentId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Department deleted", Toast.LENGTH_SHORT).show()
                fetchDepartments() // Refresh the department list
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete department", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDepartmentDetails(departmentId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Department Members")

        databaseReference.child(departmentId).child("members").get()
            .addOnSuccessListener { dataSnapshot ->
                val membersList =
                    dataSnapshot.children.joinToString("\n") { it.key?.replace(",", ".") ?: "" }
                builder.setMessage(if (membersList.isNotEmpty()) membersList else "No members found")
                builder.setPositiveButton("OK", null)
                builder.show()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load members", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUserProfile(userId: String) {
        val email = FirebaseAuth.getInstance().currentUser?.email
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(email?.replace(".", ",") ?: return)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    Log.d("HostActivity", "Fetched user data: $user")
                    userNameTextView.text = user.name
                    userDetailsTextView.text = user.profession
                    Picasso.get().load(user.profileImageUrl).into(profileImageView)
                } else {
                    Toast.makeText(this@HostActivity, "User data is null", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HostActivity, "Failed to load profile: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    data class Department(
        val name: String? = null,
        val createdBy: String? = null,
        val members: Map<String, Boolean>? = null // Adjust the type if needed
    )

    data class User(
        val name: String = "",
        val profession: String = "",
        val profileImageUrl: String = ""
    )
}
