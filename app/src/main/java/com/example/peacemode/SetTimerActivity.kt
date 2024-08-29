package com.example.peacemode

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class SetTimerActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_ALARM_PERMISSION = 1
        const val TAG = "SetTimerActivity"
    }

    private lateinit var timerAdapter: TimerAdapter
    private val timerList = mutableListOf<Timer>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settimeractivity)

        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        val setTimerButton = findViewById<Button>(R.id.setTimerButton)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        timerAdapter = TimerAdapter(timerList, this::onSwitchChanged, this::onDeleteClicked)
        recyclerView.adapter = timerAdapter

        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()  // Handle the back button press
        }

        val savedTimers = getSavedTimerSettings()
        if (savedTimers != null) {
            timerList.addAll(savedTimers)
            timerAdapter.notifyDataSetChanged()
        }

        setTimerButton.setOnClickListener {
            if (hasAlarmPermission()) {
                val newTimer = Timer(timePicker.hour, timePicker.minute, true)
                timerList.add(newTimer)
                setTimer(newTimer)
                timerAdapter.notifyDataSetChanged()
                saveTimersToSharedPreferences()
            } else {
                requestAlarmPermission()
            }
        }
    }

    private fun hasAlarmPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.SCHEDULE_EXACT_ALARM
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.USE_EXACT_ALARM
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAlarmPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.SCHEDULE_EXACT_ALARM,
                Manifest.permission.USE_EXACT_ALARM
            ),
            REQUEST_ALARM_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ALARM_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) ||
                    (grantResults.isNotEmpty() && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted
                } else {
                    // Permission denied, handle accordingly
                    Toast.makeText(
                        this,
                        "Permission denied to set exact alarms",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setTimer(timer: Timer) {
        if (!timer.isEnabled) {
            return
        }
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timer.hour)
            set(Calendar.MINUTE, timer.minute)
            set(Calendar.SECOND, 0)
        }

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, BroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        Toast.makeText(this, "Phone will be set to vibrate at ${timer.hour}:${timer.minute}", Toast.LENGTH_SHORT).show()
    }

    private fun getSavedTimerSettings(): List<Timer>? {
        val sharedPreferences = getSharedPreferences("timer_preferences", MODE_PRIVATE)
        val timersString = sharedPreferences.getString("timers", "") ?: ""

        return if (timersString.isNotEmpty()) {
            timersString.split(",").map { Timer.fromString(it) }
        } else {
            null
        }
    }

    private fun saveTimersToSharedPreferences() {
        val sharedPreferences = getSharedPreferences("timer_preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val timersString = timerList.joinToString(",") { it.toString() }

        editor.putString("timers", timersString)
        editor.apply()
    }

    private fun onSwitchChanged(timer: Timer, isChecked: Boolean) {
        timer.isEnabled = isChecked
        if (isChecked) {
            setTimer(timer)
        } else {
            // Disable the vibrate mode for the timer
            // This requires some additional logic to cancel the alarm
            cancelTimer(timer)
        }
        saveTimersToSharedPreferences()
    }

    private fun cancelTimer(timer: Timer) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, BroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Toast.makeText(this, "Timer for ${timer.hour}:${timer.minute} cancelled", Toast.LENGTH_SHORT).show()
    }

    private fun onDeleteClicked(timer: Timer) {
        timerList.remove(timer)
        timerAdapter.notifyDataSetChanged()
        cancelTimer(timer)
        saveTimersToSharedPreferences()
    }
}
