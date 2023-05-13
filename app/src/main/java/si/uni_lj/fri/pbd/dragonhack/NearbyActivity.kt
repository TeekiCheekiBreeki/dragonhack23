package si.uni_lj.fri.pbd.dragonhack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import okhttp3.*
import java.io.IOException


class NearbyActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby)
        val lon = intent.getDoubleExtra("lon", 0.0)
        val lat = intent.getDoubleExtra("lat", 0.0)
        Log.d("lat:", lat.toString())
        Log.d("lon:", lon.toString())

        // Create an OkHttp client
        val client = OkHttpClient()

        // Create a request with the endpoint you want to hit
        val request = Request.Builder()
            .url("http://212.101.137.122:8000/audios/nearby/?latitude=${lat}&longitude=${lon}")
            .build()

        // Make the network request asynchronously
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("GET Request", "Failed to get response: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    Log.d("GET Request", responseBody)
                }
            }
        })
    }
}