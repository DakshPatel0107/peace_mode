package com.example.peacemode.com.example.peacemode

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.media.AudioManager
import android.os.IBinder
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LocationService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var departmentLatitude: Double = 0.0
    private var departmentLongitude: Double = 0.0

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    checkLocation(location)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val departmentId = intent.getStringExtra("departmentId")
        if (departmentId != null) {
            fetchDepartmentLocation(departmentId)
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        return START_STICKY
    }

    private fun fetchDepartmentLocation(departmentId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("departments")
        databaseReference.child(departmentId).child("location").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                departmentLatitude = snapshot.child("latitude").getValue(Double::class.java) ?: 0.0
                departmentLongitude = snapshot.child("longitude").getValue(Double::class.java) ?: 0.0
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun checkLocation(location: Location) {
        val departmentLocation = Location("").apply {
            latitude = departmentLatitude
            longitude = departmentLongitude
        }
        val distance = location.distanceTo(departmentLocation)
        if (distance < 100) {
            setPhoneToVibrateMode()
        }
    }

    private fun setPhoneToVibrateMode() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}