package si.uni_lj.fri.pbd.dragonhack

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.io.IOException

data class MarkerData(val title: String, val id: Int, var numLikes: Int, var numDislikes: Int, val audioFile: File)

class MarkerListAdapter(private val context: Context, private val markers: List<MarkerData>) : BaseAdapter() {

    override fun getCount(): Int {
        return markers.size
    }

    override fun getItem(position: Int): Any {
        return markers[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_marker, null)
            viewHolder = ViewHolder()
            viewHolder.markerTitle = view.findViewById(R.id.titleTextView)
            viewHolder.markerLikes = view.findViewById(R.id.likesTextView)
            viewHolder.markerDislikes = view.findViewById(R.id.dislikesTextView)
            viewHolder.likeButton = view.findViewById(R.id.likeButton)
            viewHolder.playButton = view.findViewById(R.id.playButton)
            viewHolder.dislikeButton = view.findViewById(R.id.dislikeButton)
            view.tag = viewHolder

        } else {
            view = convertView
            viewHolder = convertView.tag as ViewHolder
        }

        val marker = markers[position]
        viewHolder.markerTitle.text = marker.title
        viewHolder.markerLikes.text = "Likes: ${marker.numLikes}"
        viewHolder.markerDislikes.text = "Dislikes: ${marker.numDislikes}"
        val id = marker.id
        //get entry_id
        viewHolder.likeButton.setOnClickListener {
            marker.numLikes++
            viewHolder.markerLikes.text = "Likes: ${marker.numLikes}"
            // Send like to the server
            val url = "http://212.101.137.122:8000/audios/${marker.id}/like_dislike"
            sendLikeDislike(url, true)
        }

        viewHolder.dislikeButton.setOnClickListener {
            marker.numDislikes++
            viewHolder.markerDislikes.text = "Dislikes: ${marker.numDislikes}"
            // Send dislike to the server
            val url = "http://212.101.137.122:8000/audios/${marker.id}/like_dislike"
            sendLikeDislike(url, false)
        }

        viewHolder.playButton.setOnClickListener {
            // Play audio of this marker
            playAudio(marker.audioFile)
        }

        return view
    }

    private fun sendLikeDislike(url: String, likeValue: Boolean) {
        val client = OkHttpClient()
        val urlWithParameter = "$url?like=${likeValue.toString()}"

        val request = Request.Builder()
            .url(urlWithParameter)
            .addHeader("accept", "application/json")
            .post(RequestBody.create(null, ""))
            .build()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code $response")
                } else {
                    // Handle the response as needed
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    private fun playAudio(audioFile: File) {
        // Play audio file
        val mediaPlayer = android.media.MediaPlayer()
        mediaPlayer.setDataSource(audioFile.absolutePath)
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    private class ViewHolder {
        lateinit var playButton: Button
        lateinit var markerTitle: TextView
        lateinit var markerLikes: TextView
        lateinit var markerDislikes: TextView
        lateinit var likeButton: Button
        lateinit var dislikeButton: Button
    }
}
