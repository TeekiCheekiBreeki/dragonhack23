package si.uni_lj.fri.pbd.dragonhack
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import java.io.File

data class MarkerData(val id: String, var numLikes: Int, var numDislikes: Int, val audioFile: File)


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
        viewHolder.markerTitle.text = marker.id
        viewHolder.markerLikes.text = "Likes: ${marker.numLikes}"
        viewHolder.markerDislikes.text = "Dislikes: ${marker.numDislikes}"

        viewHolder.likeButton.setOnClickListener {
            marker.numLikes++
            viewHolder.markerLikes.text = "Likes: ${marker.numLikes}"
        }

        viewHolder.dislikeButton.setOnClickListener {
            marker.numDislikes++
            viewHolder.markerDislikes.text = "Dislikes: ${marker.numDislikes}"
        }

        viewHolder.playButton.setOnClickListener {
            // play audio of this marker
            playAudio(marker.audioFile)


        }

        return view
    }
    fun playAudio(audioFile: File) {
        // play audio file
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
