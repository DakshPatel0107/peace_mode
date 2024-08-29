package com.example.peacemode

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var addTaskButton: Button
    private lateinit var taskListView: ListView
    private var selectedDate: Date? = null
    private lateinit var taskAdapter: ArrayAdapter<String>
    private val tasks = mutableListOf<String>()
    private lateinit var notificationHelper: NotificationHelper

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendaractivity)

        notificationHelper = NotificationHelper(this)


        calendarView = findViewById(R.id.calendarView)
        addTaskButton = findViewById(R.id.addButton)
        taskListView = findViewById(R.id.taskListView)

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()  // Handle the back button press
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.time
            Toast.makeText(this, "Date selected: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate)}", Toast.LENGTH_SHORT).show()
            loadTasks(selectedDate)
        }

        addTaskButton.setOnClickListener {
            selectedDate?.let {
                showAddTaskDialog(it)
            } ?: run {
                Toast.makeText(this, "Please select a date first", Toast.LENGTH_SHORT).show()
            }
        }

        taskAdapter = object : ArrayAdapter<String>(this, R.layout.task_item, R.id.taskTextView, tasks) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val deleteButton: Button = view.findViewById(R.id.deleteTaskButton)
                deleteButton.setOnClickListener {
                    val task = getItem(position)
                    tasks.remove(task)
                    task?.let { deleteTask(selectedDate, it) }
                    notifyDataSetChanged()
                }
                return view
            }
        }
        taskListView.adapter = taskAdapter
    }

    private fun showAddTaskDialog(date: Date) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Task")

        val input = EditText(this)
        input.hint = "Enter task description"
        builder.setView(input)

        builder.setPositiveButton("Save") { dialog, _ ->
            val task = input.text.toString()
            if (task.isNotEmpty()) {
                saveTask(date, task)
                setTaskAlert(this, date, task)
                requestNotificationPermission(task)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter a task description", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun saveTask(date: Date, task: String) {
        val sharedPreferences = getSharedPreferences("tasks", MODE_PRIVATE)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(date)

        // Load existing tasks for the date
        val currentTasks = sharedPreferences.getStringSet(formattedDate, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        currentTasks.add(task)

        // Save updated tasks to SharedPreferences
        sharedPreferences.edit().putStringSet(formattedDate, currentTasks).apply()

        // Debug log to check saved task
        Log.d("CalendarActivity", "Task saved for date $formattedDate: $task")

        // Update local list and notify adapter
        tasks.clear()
        tasks.addAll(currentTasks)
        taskAdapter.notifyDataSetChanged()

        Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show()
    }

    private fun deleteTask(date: Date?, task: String) {
        date?.let {
            val sharedPreferences = getSharedPreferences("tasks", MODE_PRIVATE)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(date)

            val currentTasks = sharedPreferences.getStringSet(formattedDate, mutableSetOf())?.toMutableSet()
            currentTasks?.remove(task)

            // Save updated tasks to SharedPreferences after deletion
            sharedPreferences.edit().putStringSet(formattedDate, currentTasks).apply()

            // Debug log to check deleted task
            Log.d("CalendarActivity", "Task deleted for date $formattedDate: $task")

            Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setTaskAlert(context: Context, date: Date, task: String) {
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("task", task)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            time = date
        }

        // Calculate the time difference between now and the task date
        val timeDifference = calendar.timeInMillis - System.currentTimeMillis()

        if (timeDifference > 0) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            Toast.makeText(context, "Alert set for task", Toast.LENGTH_SHORT).show()
        } else {
            // If the task date is in the past, handle accordingly (optional)
            Toast.makeText(context, "Task date has already passed", Toast.LENGTH_SHORT).show()
        }
    }


    private fun loadTasks(date: Date?) {
        date?.let {
            val sharedPreferences = getSharedPreferences("tasks", MODE_PRIVATE)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(date)

            val taskSet = sharedPreferences.getStringSet(formattedDate, mutableSetOf()) ?: mutableSetOf()
            tasks.clear()
            tasks.addAll(taskSet)
            taskAdapter.notifyDataSetChanged()

            // Debug log to check loaded tasks
            Log.d("CalendarActivity", "Loaded tasks for date $formattedDate: $tasks")
        }
    }

    private fun requestNotificationPermission(task: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NotificationHelper.REQUEST_CODE_POST_NOTIFICATIONS
            )
        } else {
            notificationHelper.showTaskNotification(task)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NotificationHelper.REQUEST_CODE_POST_NOTIFICATIONS) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
