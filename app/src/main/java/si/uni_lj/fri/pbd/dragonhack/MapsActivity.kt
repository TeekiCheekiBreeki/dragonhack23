package si.uni_lj.fri.pbd.dragonhack

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import si.uni_lj.fri.pbd.dragonhack.databinding.ActivityMapsBinding
import android.util.Log
import android.view.LayoutInflater
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.model.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var isLoggedIn: Boolean = false
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationPermissionCode = 1

    private lateinit var currentLocation: Location

    private val markerAudioMap = HashMap<Marker, File>()
    val markerClusters = mutableListOf<MarkerCluster>()

    private val clusterMarkerMap = HashMap<Marker, MarkerCluster>()

    private val markerOptionsAudioMap = HashMap<MarkerOptions, File>()

    private val standaloneMarkers = mutableListOf<MarkerOptions>()

    private val markerLikesMap = HashMap<MarkerOptions, Int>()
    private val markerDislikesMap = HashMap<MarkerOptions, Int>()





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //check for permissions
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //when permission granted
            //do nothing
        } else {
            //when permission not granted
            //request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }

        //check for storage permissions
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //when permission granted
            //do nothing
        } else {
            //when permission not granted
            //request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                locationPermissionCode
            )
        }


        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_nearby -> {
                    openNearby()
                }
                R.id.action_profile -> {
                    if (isLoggedIn) {
                        openProfile()
                    } else {
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
                    //addMarkerAtCurrentLocation()
                    // Open new window for adding new graffiti(recording sound)
                    val intent = Intent(this, AddNewGraffitiActivity::class.java).apply {
                        putExtra("latitude", currentLocation.getLatitude())
                        putExtra("longitude", currentLocation.getLongitude())
                    }


                    startActivityForResult(intent, ADD_GRAFFITI_REQUEST_CODE)





                }
            }
            true
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    /*private fun addMarkerAtCurrentLocation() {
        val currentLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        mMap.addMarker(
            MarkerOptions()
                .position(currentLatLng)
                .title("New Graffiti") // you can set the title of your marker
        )
    }*/

    fun openProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    fun loginRedirect() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this, R.raw.custom_map_style));


        mMap.setOnMarkerClickListener { marker ->
            val audioFile = markerAudioMap[marker]
            val cluster = clusterMarkerMap[marker]

            if (audioFile != null) {
                // It's an audio marker
                playAudio(audioFile)
                marker.showInfoWindow()

                val markerOptions = MarkerOptions()
                    .position(marker.position)
                    .title(marker.title)
                    .snippet(marker.snippet)
                showLikeDislikeButtons(markerOptions)
            } else if (cluster != null) {
                // It's a cluster marker
                displayMarkerList(cluster)
            }

            true
        }


        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLocation = location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    // GET all the data from the server
                    fetchDataFromServer()
                }

            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )

        }


    }


    private fun showMarkersDialog(markers: List<MarkerData>) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_marker, null)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
        val listView = dialogView.findViewById<ListView>(R.id.dialog_listview)

        // Set the title of the dialog
        dialogTitle.text = "Markers in Cluster"

        // Create and set the adapter for the ListView
        val markerListAdapter = MarkerListAdapter(this, markers)
        listView.adapter = markerListAdapter

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Markers in cluster")
        builder.setView(dialogView)

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }
    private fun displayMarkerList(cluster: MarkerCluster) {
        val markerDataList = cluster.markers.mapNotNull { markerOptions ->
            val id = markerOptions.title
            val likes = markerLikesMap[markerOptions] ?: 0
            val dislikes = markerDislikesMap[markerOptions] ?: 0
            val audioFile = markerOptionsAudioMap[markerOptions]
            if (id != null && audioFile != null) {
                MarkerData(id, likes, dislikes, audioFile)
            } else {
                null
            }
        }

        // Show the dialog with the markers in the cluster
        showMarkersDialog(markerDataList)
    }






    private fun showLikeDislikeButtons(marker: MarkerOptions) {
        val likes = markerLikesMap[marker] ?: 0
        val dislikes = markerDislikesMap[marker] ?: 0
        Log.d("LIKES", "Marker ${marker.title} has $likes likes and $dislikes dislikes")
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

    private fun fetchDataFromServer() {
        val url = "http://212.101.137.122:8000/audios/nearby/?latitude=${currentLocation.latitude}&longitude=${currentLocation.longitude}"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("GET Request", "Failed to get response: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val handler = Handler(Looper.getMainLooper())
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    try {
                        val jsonObject = JSONObject(responseBody)
                        val jsonArray = jsonObject.getJSONArray("audios_nearby")
                        var requestCounter = jsonArray.length()

                        val markerDataList = mutableListOf<MarkerData>()


                        for (i in 0 until jsonArray.length()) {
                            val audioObject = jsonArray.getJSONObject(i)
                            val latitude = audioObject.getJSONObject("location")
                                .getJSONArray("coordinates").getDouble(1)
                            val longitude = audioObject.getJSONObject("location")
                                .getJSONArray("coordinates").getDouble(0)
                            val audioUrl = audioObject.getString("filename")
                            val title = audioObject.getString("title")
                            val entryId = audioObject.getString("entry_id")
                            val numLikes = audioObject.getInt("num_likes")
                            val numDislikes = audioObject.getInt("num_dislikes")

                            getAudioFile(entryId) { audioFile ->
                                if (audioFile != null) {
                                    val locationLatLng = LatLng(latitude, longitude)
                                    val markerOptions =
                                        MarkerOptions().position(locationLatLng)
                                            .title(title)
                                            .snippet("Likes: $numLikes, Dislikes: $numDislikes")
                                    markerOptionsAudioMap[markerOptions] = audioFile
                                    markerLikesMap[markerOptions] = numLikes
                                    markerDislikesMap[markerOptions] = numDislikes
                                    // Create a MarkerData object and add it to the list
                                    val markerData = MarkerData(entryId, numLikes, numDislikes, audioFile)
                                    markerDataList.add(markerData)
                                    requestCounter--
                                    var addedToCluster = false

                                    // Check each cluster if the new marker can be added.
                                    for (cluster in markerClusters) {
                                        for (existingMarker in cluster.markers) {
                                            val distance = calculateDistance(
                                                existingMarker.position,
                                                locationLatLng
                                            )
                                            Log.d(
                                                "ADDING TO CLUSTER",
                                                "title: ${existingMarker.title}, distance: $distance"
                                            )
                                            if (distance <= 200.0) {
                                                cluster.markers.add(markerOptions)
                                                standaloneMarkers.remove(markerOptions)  // Add this line
                                                addedToCluster = true
                                                break
                                            }
                                        }

                                    }

                                    // Check standalone markers if a new cluster can be created.
                                    if (!addedToCluster) {
                                        var markerForNewCluster: MarkerOptions? = null
                                        for (existingMarker in standaloneMarkers) {
                                            val distance = calculateDistance(
                                                existingMarker.position,
                                                locationLatLng
                                            )
                                            if (distance <= 200.0) {
                                                markerForNewCluster = existingMarker
                                                break
                                            }
                                        }

                                        if (markerForNewCluster != null) {
                                            standaloneMarkers.remove(markerForNewCluster)
                                            val newCluster = MarkerCluster(
                                                locationLatLng,
                                                mutableListOf(markerForNewCluster, markerOptions)
                                            )
                                            markerClusters.add(newCluster)
                                        } else {
                                            standaloneMarkers.add(markerOptions)
                                        }
                                    }
                                }
                                if(requestCounter == 0){
                                    handler.post {
                                        for (cluster in markerClusters) {
                                            val clusterMarker = mMap.addMarker(
                                                MarkerOptions()
                                                    .position(cluster.center)
                                                    .title("Cluster")
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                            )
                                            if (clusterMarker != null) {
                                                cluster.markerInstances.add(clusterMarker)
                                                clusterMarkerMap[clusterMarker] = cluster
                                            }
                                        }

                                        for (marker in standaloneMarkers) {
                                            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                            val standaloneMarker = mMap.addMarker(marker)
                                            if (standaloneMarker != null) {
                                                markerAudioMap[standaloneMarker] = markerOptionsAudioMap[marker]!!
                                            }
                                        }
                                    }
                                }
                            }


                                }


                    } catch (e: JSONException) {
                        Log.d("GET Request", "Failed to parse JSON: ${e.message}")
                    }
                }
            }
        })
    }

    private fun clusterMarkers(markers: List<MarkerOptions>, maxDistance: Double): Pair<List<MarkerOptions>, List<MarkerCluster>> {
        val standaloneMarkers = mutableListOf<MarkerOptions>()
        val clusters = mutableListOf<MarkerCluster>()

        for (marker in markers) {
            var nearestCluster: MarkerCluster? = null
            var nearestDistance = Double.MAX_VALUE

            for (cluster in clusters) {
                val distance = calculateDistance(cluster.center, marker.position)
                if (distance < nearestDistance) {
                    nearestDistance = distance
                    nearestCluster = cluster
                }
            }

            if (nearestCluster != null && nearestDistance <= maxDistance) {
                nearestCluster.markers.add(marker)
                // Update the cluster's center
                nearestCluster.center = LatLng(
                    (nearestCluster.center.latitude * (nearestCluster.markers.size - 1) + marker.position.latitude) / nearestCluster.markers.size,
                    (nearestCluster.center.longitude * (nearestCluster.markers.size - 1) + marker.position.longitude) / nearestCluster.markers.size
                )
            } else {
                standaloneMarkers.add(marker)
            }
        }

        return Pair(standaloneMarkers, clusters)
    }



                        private fun calculateDistance(location1: LatLng, location2: LatLng): Double {
        val earthRadius = 6371000.0 // Earth's radius in meters
        val lat1 = Math.toRadians(location1.latitude)
        val lon1 = Math.toRadians(location1.longitude)
        val lat2 = Math.toRadians(location2.latitude)
        val lon2 = Math.toRadians(location2.longitude)

        val deltaLat = lat2 - lat1
        val deltaLon = lon2 - lon1

        val a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c
    }


    private fun getAudioFile(entryId: String, callback: (File?) -> Unit) {
        val url = "http://212.101.137.122:8000/audios/$entryId"
        val client = OkHttpClient()

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Download", "Failed to download file", e)
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val file = File(getExternalFilesDir(null), "$entryId.mp3")

                response.body?.byteStream()?.use { inputStream ->
                    file.outputStream().use { fileOut ->
                        inputStream.copyTo(fileOut)
                    }
                }

                callback(file)
            }
        })
    }


    private fun playAudio(audioFile: File) {
        if (audioFile != null) {
            // Create a new media player and set the listeners
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(audioFile.absolutePath)
            mediaPlayer.prepare()
            mediaPlayer.start()

        }
    }
    companion object {
        const val ADD_GRAFFITI_REQUEST_CODE = 101
    }

}
data class MarkerCluster(
    var center: LatLng,
    val markers: MutableList<MarkerOptions>,
    val markerInstances: MutableList<Marker> = mutableListOf()
)












