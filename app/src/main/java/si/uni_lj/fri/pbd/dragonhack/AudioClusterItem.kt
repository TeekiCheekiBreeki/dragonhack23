package si.uni_lj.fri.pbd.dragonhack
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import java.io.File

class AudioClusterItem(
    private val position: LatLng,
    private val title: String,
    private val audioFile: File
) : ClusterItem {
    override fun getPosition(): LatLng {
        return position
    }

    override fun getTitle(): String {
        return title
    }

    override fun getSnippet(): String? {
        return null
    }

    fun getAudioFile(): File {
        return audioFile
    }
}
