package com.example.locationtracking

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.locationtracking.databinding.ActivityMainBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
    }

    private lateinit var locationService: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationService = Intent(this, LocationService::class.java)

        binding.apply {
            startTrackingBtn.setOnClickListener {
                checkPermissions()
            }
            stopTrackingBtn.setOnClickListener {
                stopService(locationService)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(locationService)
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    @Subscribe
    fun receiveLocationEvent(locationEvent: LocationEvent) {
        binding.latitude.text = "Latitude: ${locationEvent.latitude}"
        binding.longitude.text = "Longitude: ${locationEvent.longitude}"
    }

    private fun checkPermissions() {
        val fineLocationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocationPermission = android.Manifest.permission.ACCESS_COARSE_LOCATION
        val permissionsToRequest = mutableListOf<String>()

        if (!PermissionUtils.isPermissionGranted(this, fineLocationPermission)) {
            permissionsToRequest.add(fineLocationPermission)
        }
        if (!PermissionUtils.isPermissionGranted(this, coarseLocationPermission)) {
            permissionsToRequest.add(coarseLocationPermission)
        }

        if (permissionsToRequest.isNotEmpty()) {
            PermissionUtils.requestPermission(
                this,
                permissionsToRequest.toTypedArray()[0],
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            startService(locationService)
        }
    }
}