package app.opass.ccip.ui.sessiondetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import app.opass.ccip.R
import app.opass.ccip.model.Speaker
import coil.load

class SpeakerFragment : Fragment() {
    companion object {
        fun newInstance(speaker: Speaker): Fragment {
            val speakerFragment = SpeakerFragment()
            speakerFragment.speaker = speaker
            return speakerFragment
        }
    }

    private var speaker: Speaker? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.item_speaker_image, container, false)
        val speakerImageView = view.findViewById<ImageView>(R.id.speaker_image)
        speakerImageView.load(speaker?.avatar)

        return view
    }
}
