package com.example.peacemode

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.media.AudioManager
import android.os.Bundle
import android.os.Vibrator
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.peacemode.com.example.peacemode.LocationService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import java.util.UUID

@Suppress("UNCHECKED_CAST")
class SetLocationActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSpinner: Spinner
    private lateinit var setLocationButton: Button
    private lateinit var viewLocationButton: Button  // New button for viewing location
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private var currentMarker: PointAnnotation? = null
    private var selectedDepartmentId: String? = null
    private var userLocationMarker: PointAnnotation? = null

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private lateinit var databaseReference: DatabaseReference
    private val currentUserEmail: String? = FirebaseAuth.getInstance().currentUser?.email

    private lateinit var vibrator: Vibrator
    private lateinit var audioManager: AudioManager
    private var previousRingerMode: Int = AudioManager.RINGER_MODE_NORMAL
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_setup)

        mapView = findViewById(R.id.mapView)
        locationSpinner = findViewById(R.id.locationSpinner)
        setLocationButton = findViewById(R.id.setLocationButton)
        viewLocationButton = findViewById(R.id.viewLocationButton)  // Initialize the View Location button
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        databaseReference = FirebaseDatabase.getInstance().getReference("departments")

        val annotationApi = mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        requestNotificationPermissions()

        checkLocationPermission()
        setupMapClickListener()
        fetchDepartments()
        showDepartmentMarkers()

        setLocationButton.setOnClickListener {
            saveLocationForDepartment()
        }

        viewLocationButton.setOnClickListener {
            showLocationForSelectedDepartment()
        }

        val departmentButton = findViewById<Button>(R.id.departmentButton)
        departmentButton.setOnClickListener {
            fetchMemberDepartments()
        }
    }


    fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        startService(intent)
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getUserLocation()
        }
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        moveMapToLocation(it.latitude, it.longitude)
                        updateUserLocationMarker(it)
                        checkProximityToDepartment(it)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateUserLocationMarker(location: Location) {
        userLocationMarker?.let {
            pointAnnotationManager.delete(it)
        }

        val newSizeInPixels = (30 * resources.displayMetrics.density).toInt()
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.location_pin)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newSizeInPixels, newSizeInPixels, false)

        val point = Point.fromLngLat(location.longitude, location.latitude)
        val options = PointAnnotationOptions()
            .withPoint(point)
            .withIconImage(resizedBitmap)

        userLocationMarker = pointAnnotationManager.create(options)
    }

    private fun moveMapToLocation(latitude: Double, longitude: Double) {
        val cameraOptions = CameraOptions.Builder()
            .center(Point.fromLngLat(longitude, latitude))
            .zoom(14.0)
            .build()

        mapView.getMapboxMap().setCamera(cameraOptions)
    }

    private fun setupMapClickListener() {
        mapView.getMapboxMap().addOnMapClickListener { point ->
            updateMarkerPosition(point)
            true
        }
    }

    private fun updateMarkerPosition(point: Point) {
        currentMarker?.let {
            pointAnnotationManager.delete(it)
        }

        // Calculate the new size in pixels
        val newSizeInPixels = (30 * resources.displayMetrics.density).toInt()

        // Resize the bitmap
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.location_pin)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newSizeInPixels, newSizeInPixels, false)

        val options = PointAnnotationOptions()
            .withPoint(point)
            .withIconImage(resizedBitmap)

        currentMarker = pointAnnotationManager.create(options)
    }

    private fun fetchDepartments() {
        databaseReference.get().addOnSuccessListener { dataSnapshot ->
            val departmentNames = mutableListOf<String>()
            val departmentIds = mutableListOf<String>()

            dataSnapshot.children.forEach { departmentSnapshot ->
                val department = departmentSnapshot.getValue(HostActivity.Department::class.java)
                if (department?.createdBy == currentUserEmail) {
                    if (department != null) {
                        department.name?.let { departmentNames.add(it) }
                    }
                    departmentIds.add(departmentSnapshot.key!!)
                }
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departmentNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            locationSpinner.adapter = adapter

            locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedDepartmentId = departmentIds[position]
                    checkProximityToSelectedDepartment() // Check proximity on department change
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    selectedDepartmentId = null
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load departments", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveLocationForDepartment() {
        selectedDepartmentId?.let { departmentId ->
            currentMarker?.let { marker ->
                val location = marker.point
                databaseReference.child(departmentId).child("location").setValue(
                    mapOf(
                        "latitude" to location.latitude(),
                        "longitude" to location.longitude()
                    )
                ).addOnSuccessListener {
                    Toast.makeText(this, "Location saved for department", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to save location", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(this, "Please place a marker on the map", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLocationForSelectedDepartment() {
        selectedDepartmentId?.let { departmentId ->
            databaseReference.child(departmentId).child("location").get().addOnSuccessListener { dataSnapshot ->
                val latitude = dataSnapshot.child("latitude").value as? Double
                val longitude = dataSnapshot.child("longitude").value as? Double

                if (latitude != null && longitude != null) {
                    moveMapToLocation(latitude, longitude)
                    val point = Point.fromLngLat(longitude, latitude)
                    updateMarkerPosition(point)

                    // Check proximity to department location (even if location hasn't updated recently)
                    checkProximityToSelectedDepartment(latitude, longitude)
                } else {
                    Toast.makeText(this, "No location data found for this department", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to fetch location", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUserCurrentLocation(callback: (Location) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        updateUserLocationMarker(it)
                        callback(it)
                    } ?: run {
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to get current location", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    // Refactored checkProximityToDepartment function
    private fun checkProximityToSelectedDepartment(departmentLatitude: Double? = null, departmentLongitude: Double? = null) {
        selectedDepartmentId?.let { departmentId ->
            // If coordinates are not provided, fetch them from the database
            if (departmentLatitude == null || departmentLongitude == null) {
                databaseReference.child(departmentId).child("location").get().addOnSuccessListener { dataSnapshot ->
                    val latitude = dataSnapshot.child("latitude").value as? Double
                    val longitude = dataSnapshot.child("longitude").value as? Double

                    if (latitude != null && longitude != null) {
                        checkProximity(latitude, longitude) // Call the generic proximity check function
                    } else {
                        Toast.makeText(this, "No location data found for this department", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to fetch department location", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Coordinates are provided, directly call the generic proximity check function
                checkProximity(departmentLatitude, departmentLongitude)
            }
        }
    }

    // Generic proximity check function
    private fun checkProximity(departmentLatitude: Double, departmentLongitude: Double) {
        // Get the user's current location (if available)
        getUserCurrentLocation { userLocation ->
            val departmentLocation = Location("").apply {
                latitude = departmentLatitude
                longitude = departmentLongitude
            }

            val distance = userLocation.distanceTo(departmentLocation)
            if (distance < 100) { // Adjust the distance threshold as needed
                setPhoneToVibrateMode()
                sendNotificationToDepartmentMembers(selectedDepartmentId!!)
            } else {
                Toast.makeText(this, "You are not near the department location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestNotificationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun checkProximityToDepartment(location: Location) {
        databaseReference.get().addOnSuccessListener { dataSnapshot ->
            dataSnapshot.children.forEach { departmentSnapshot ->
                val departmentLocation = departmentSnapshot.child("location")
                val latitude = departmentLocation.child("latitude").value as? Double
                val longitude = departmentLocation.child("longitude").value as? Double

                if (latitude != null && longitude != null) {
                    val departmentLocationPoint = Location("").apply {
                        this.latitude = latitude
                        this.longitude = longitude
                    }

                    val distance = location.distanceTo(departmentLocationPoint)
                    if (distance < 100) { // Adjust the distance threshold as needed
                        setPhoneToVibrateMode()
                        sendNotificationToDepartmentMembers(departmentSnapshot.key!!)
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch department locations", Toast.LENGTH_SHORT).show()
        }
    }
    private fun setPhoneToVibrateMode() {
        previousRingerMode = audioManager.ringerMode  // Save the previous mode
        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
    }

    private fun sendNotificationToDepartmentMembers(departmentId: String) {
        databaseReference.child(departmentId).child("members").get().addOnSuccessListener { dataSnapshot ->
            val members = mutableListOf<String>()
            dataSnapshot.children.forEach { memberSnapshot ->
                members.add(memberSnapshot.key!!)
            }

            for (member in members) {
                sendNotification(member)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch department members", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendNotification(memberId: String) {
        val topic = "/topics/$memberId"
        val message = RemoteMessage.Builder(topic)
            .setMessageId(UUID.randomUUID().toString())
            .addData("title", "Department Location Alert")
            .addData("body", "You are near the department location. Your phone will vibrate.")
            .build()
        FirebaseMessaging.getInstance().send(message)
    }
    private fun vibrate() {
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(1000) // Vibrate for 1 second
        }
    }

    private fun fetchMemberDepartments() {
        val memberDepartments = mutableListOf<Pair<String, String>>()
        databaseReference.get().addOnSuccessListener { dataSnapshot ->
            dataSnapshot.children.forEach { departmentSnapshot ->
                val department = departmentSnapshot.getValue(HostActivity.Department::class.java)
                if (department?.members?.containsKey(currentUserEmail?.replace(".", ",")) == true) {
                    department.name?.let { departmentName ->
                        memberDepartments.add(Pair(departmentSnapshot.key!!, departmentName))
                    }
                }
            }
            showDepartmentSelectionDialog(memberDepartments)
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load member departments", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDepartmentSelectionDialog(memberDepartments: List<Pair<String, String>>) {
        if (memberDepartments.isEmpty()) {
            Toast.makeText(this, "You are not a member of any departments", Toast.LENGTH_SHORT).show()
            return
        }

        val departmentNames = memberDepartments.map { it.second }.toTypedArray()
        val departmentIds = memberDepartments.map { it.first }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select Department")
            .setItems(departmentNames) { _, which ->
                selectedDepartmentId = departmentIds[which]
                showLocationForSelectedDepartment()
            }
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with notification-related actions
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied, handle the case
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addDepartmentMarker(point: Point, departmentName: String) {
        val newSizeInPixels = (30 * resources.displayMetrics.density).toInt()
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.location_pin)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newSizeInPixels, newSizeInPixels, false)

        val options = PointAnnotationOptions()
            .withPoint(point)
            .withIconImage(resizedBitmap)

        val marker = pointAnnotationManager.create(options)

        // Set a click listener to show department name
        pointAnnotationManager.addClickListener { clickedMarker ->
            if (clickedMarker == marker) {
                Toast.makeText(this, departmentName, Toast.LENGTH_SHORT).show()
                true
            } else {
                false
            }
        }
    }

    private fun showDepartmentMarkers() {
        databaseReference.get().addOnSuccessListener { dataSnapshot ->
            dataSnapshot.children.forEach { departmentSnapshot ->
                val department = departmentSnapshot.getValue(HostActivity.Department::class.java)
                val location = departmentSnapshot.child("location")
                val latitude = location.child("latitude").value as? Double
                val longitude = location.child("longitude").value as? Double

                if (latitude != null && longitude != null && department != null) {
                    val point = Point.fromLngLat(longitude, latitude)
                    addDepartmentMarker(point, department.name ?: "Unnamed Department")
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load departments", Toast.LENGTH_SHORT).show()
        }
    }


    // Lifecycle methods
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    data class LocationData(
        var latitude: Double = 0.0,
        var longitude: Double = 0.0
    )
}
