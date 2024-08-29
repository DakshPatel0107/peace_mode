package com.example.peacemode

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log

class BroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "SilenceBroadcastReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Broadcast received")
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
        Log.d(TAG, "Phone set to vibrate mode")
    }

}

//open class BroadcastReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context?, intent: Intent?) {
//        val task = intent?.getStringExtra("task")
//        Toast.makeText(context, "Task Alert: $task", Toast.LENGTH_LONG).show()
//    }
