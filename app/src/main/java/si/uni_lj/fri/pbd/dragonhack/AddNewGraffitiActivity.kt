package si.uni_lj.fri.pbd.dragonhack

import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io. File
import java.io.IOException

class AddNewGraffitiActivity : AppCompatActivity() {
    var mediaPlayer: MediaPlayer? = null
    var recorder: MediaRecorder? = null
    private val url = "http://212.101.137.122:8000/uploadfile/"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_graffiti)

        val recordBtn = findViewById<Button>(R.id.btn_record)
        val playBtn = findViewById<Button>(R.id.btn_play)
        val stopBtn = findViewById<Button>(R.id.btn_stp)
        val saveBtn = findViewById<Button>(R.id.btn_save)
        val retryBtn = findViewById<Button>(R.id.btn_retry)
        val path = "${externalCacheDir?.absolutePath}/recording.wav"

        //disable buttons
        recordBtn.isEnabled = false
        playBtn.isEnabled = false
        stopBtn.isEnabled = false
        retryBtn.isEnabled = false
        saveBtn.isEnabled = false
        stopBtn.visibility = View.INVISIBLE


        //path

        recorder = MediaRecorder()

        //check for record audio permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            //when permission granted
            //do nothing
            recordBtn.isEnabled = true
        } else {
            //when permission not granted
            //request permission



            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 1)
        }

        recordBtn.setOnClickListener {
            //start recording
            recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder?.setOutputFile(path)
            recorder?.prepare()
            recorder?.start()
            stopBtn.isEnabled = true
            recordBtn.isEnabled = false
            recordBtn.visibility = View.INVISIBLE
            stopBtn.visibility = View.VISIBLE

        }

        stopBtn.setOnClickListener {
            //stop recording
            recorder?.stop()
            recorder?.reset()
            recorder?.release()
            recorder = null
            playBtn.isEnabled = true
            stopBtn.isEnabled = false
            retryBtn.isEnabled = true
            recordBtn.isEnabled = false
            saveBtn.isEnabled = true
            recordBtn.visibility = View.VISIBLE
            stopBtn.visibility = View.INVISIBLE
        }

        playBtn.setOnClickListener {
            //play recording
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(path)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            recordBtn.isEnabled = true
        }

        retryBtn.setOnClickListener() {
            val file = File(path)
            file.delete()

            // Release the old MediaRecorder if it's not null
            recorder?.release()

            // Create a new MediaRecorder
            recorder = MediaRecorder()

            recordBtn.isEnabled = true
            saveBtn.isEnabled = false
            playBtn.isEnabled = false
            retryBtn.isEnabled = false
        }




        saveBtn.setOnClickListener() {
            //save recording
            val file = java.io.File(path)
            val titleEditText = findViewById<EditText>(R.id.et_title) // Get the EditText
            val title = titleEditText.text.toString() // Get the text entered by the user
            val latitude = intent.getDoubleExtra("latitude", 0.0)
            val longitude = intent.getDoubleExtra("longitude", 0.0)

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    file.name,
                    file.asRequestBody("audio/wav".toMediaTypeOrNull())
                )
                .build()

            val client = OkHttpClient()
            val urlRequest = "$url?title=$title&latitude=$latitude&longitude=$longitude"
            val request = Request.Builder()
                .url(urlRequest)
                .addHeader("accept", "application/json")
                .post(requestBody)
                .build()

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    } else {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            Log.i("response", responseBody)
                            val resultIntent = Intent().apply {
                                putExtra("newGraffitiTitle", title)
                                putExtra("newGraffitiLatitude", latitude)
                                putExtra("newGraffitiLongitude", longitude)
                                putExtra("newGraffitiAudioPath", path)
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish()

                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            //enable buttons
            val recordBtn = findViewById<Button>(R.id.btn_record)
            val playBtn = findViewById<Button>(R.id.btn_play)
            val stopBtn = findViewById<Button>(R.id.btn_stp)

            recordBtn.isEnabled = true
            playBtn.isEnabled = true
            stopBtn.isEnabled = true
        }
    }


}