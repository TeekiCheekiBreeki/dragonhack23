package si.uni_lj.fri.pbd.dragonhack

import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import java.io. File
import java.io.IOException
import java.util.Timer

class AddNewGraffitiActivity : AppCompatActivity() {
    var mediaPlayer: MediaPlayer? = null
    var recorder: MediaRecorder? = null
    private val url = "http://212.101.137.122:8000/uploadfile/"

    private var isRecording = false
    private var countDownTimer: CountDownTimer? = null
    private val maxRecordTimeInMilliseconds = 20 * 1000L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_graffiti)

        val recordBtn = findViewById<Button>(R.id.btn_record)
        val playBtn = findViewById<ImageButton>(R.id.btn_play)
        val saveBtn = findViewById<ImageButton>(R.id.btn_save)
        val path = "${externalCacheDir?.absolutePath}/recording.wav"
        val timerTextView: TextView = findViewById(R.id.textView)

        //disable buttons
        recordBtn.isEnabled = true
        playBtn.isEnabled = false
        saveBtn.isEnabled = false
        //change color of play button to grey

        val gifImageView: GifImageView = findViewById(R.id.gifImageView)
        val gifDrawable = gifImageView.drawable as GifDrawable

        gifDrawable.stop()


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
            if (!isRecording) {
                // Start recording

                // Delete existing recording if present
                val oldFile = File(path)
                if (oldFile.exists()) {
                    oldFile.delete()
                }

                gifDrawable.start()
                recordBtn.text = "Stop"
                recordBtn.setBackgroundResource(R.drawable.circle_button_red)
                recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
                recorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                recorder?.setOutputFile(path)
                recorder?.prepare()
                recorder?.start()
                isRecording = true

                // Start countdown
                countDownTimer = object : CountDownTimer(maxRecordTimeInMilliseconds, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        // You can update a UI element here to display remaining time
                        timerTextView.text = (millisUntilFinished / 1000).toString()
                    }

                    override fun onFinish() {
                        stopRecording()
                    }
                }
                countDownTimer?.start()
            } else {
                // Stop recording
                timerTextView.text = "20"
                recordBtn.text = "Retry"
                stopRecording()
            }
        }






        playBtn.setOnClickListener {
            if (java.io.File(path).exists()) {
                mediaPlayer = MediaPlayer()
                mediaPlayer?.setDataSource(path)
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                recordBtn.isEnabled = true
            } else {
                // File does not exist, show an appropriate message or handle the scenario
                Log.e("Playback", "File not found")
            }
        }






        saveBtn.setOnClickListener() {
            //save recording
            val file = java.io.File(path)
            val titleEditText = EditText(this) // Create an EditText for AlertDialog
            val latitude = intent.getDoubleExtra("latitude", 0.0)
            val longitude = intent.getDoubleExtra("longitude", 0.0)

            // Creating AlertDialog
            AlertDialog.Builder(this)
                .setTitle("Name your Graffiti")
                .setMessage("Please enter a name for your graffiti:")
                .setView(titleEditText)
                .setPositiveButton("Save") { _, _ ->
                    val title = titleEditText.text.toString() // Get the text entered by the user

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

                    val intent = Intent(this, MapsActivity::class.java)
                    startActivity(intent)
                    GlobalScope.launch(Dispatchers.IO) {
                        try {
                            val response = client.newCall(request).execute()
                            if (!response.isSuccessful) {
                                throw IOException("Unexpected code $response")
                            } else {
                                val responseBody = response.body?.string()
                                if (responseBody != null) {
                                    Log.i("response", responseBody)
                                }
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
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
            val playBtn = findViewById<ImageButton>(R.id.btn_play)

            recordBtn.isEnabled = true
            playBtn.isEnabled = true
        }
    }

    private fun stopRecording() {
        val recordBtn = findViewById<Button>(R.id.btn_record)
        val playBtn = findViewById<ImageButton>(R.id.btn_play)
        val saveBtn = findViewById<ImageButton>(R.id.btn_save)
        val gifImageView: GifImageView = findViewById(R.id.gifImageView)
        val gifDrawable = gifImageView.drawable as GifDrawable

        recordBtn.setBackgroundResource(R.drawable.circle_button_blue)

        if (isRecording) {
            gifDrawable.stop()
            recordBtn.text = "Retry"
            try {
                recorder?.stop()
                recorder?.reset()
            } catch (e: RuntimeException) {
                // This may occur when stop() is called immediately after start()
                // Ignoring it as we are handling it
                e.printStackTrace()
            }
            playBtn.isEnabled = true
            saveBtn.isEnabled = true
            isRecording = false
            countDownTimer?.cancel()
        }
    }
}