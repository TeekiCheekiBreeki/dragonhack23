package si.uni_lj.fri.pbd.dragonhack

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import si.uni_lj.fri.pbd.dragonhack.databinding.ActivityMapsBinding
import android.util.Log

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var isLoggedIn: Boolean = false
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationPermissionCode = 1

    private lateinit var currentLocation: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //check for permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //when permission granted
            //do nothing
        } else {
            //when permission not granted
            //request permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }

        //check for storage permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //when permission granted
            //do nothing
        } else {
            //when permission not granted
            //request permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), locationPermissionCode)
        }

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_nearby -> {
                    openNearby()
                }
                R.id.action_profile -> {
                    if (isLoggedIn){
                        openProfile()
                    } else{
                        loginRedirect()
                    }

                }
                R.id.action_settings -> {
                    // Change to SettingsActivity
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)

                }
                R.id.add_new_graffiti -> {
                    // Handle "Add new graffiti" click
                    addMarkerAtCurrentLocation()
                    // Open new window for adding new graffiti(recording sound)
                    val intent = Intent(this, AddNewGraffitiActivity::class.java).apply {
                        putExtra("latitude", currentLocation.getLatitude())
                        putExtra("longitude", currentLocation.getLongitude())
                    }

                    startActivity(intent)

                }
            }
            true
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }
    private fun addMarkerAtCurrentLocation() {
        val currentLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        mMap.addMarker(
            MarkerOptions()
                .position(currentLatLng)
                .title("New Graffiti") // you can set the title of your marker
        )
    }

    private fun openProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun loginRedirect() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLocation = location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            onMapReady(mMap)
        }
    }

    private fun openProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun openNearby() {
        val intent = Intent(this, NearbyActivity::class.java)
        val lat = currentLocation.latitude
        val lon = currentLocation.longitude
        Log.d("CURRENT LATITUDE:", "$lat")
        Log.d("CURRENT LONGITUDE:", "$lon")
        intent.putExtra("lat", lat)
        intent.putExtra("lon", lon)
        startActivity(intent)
    }

    private fun loginRedirect() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

}


