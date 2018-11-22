package app.opass.ccip.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import app.opass.ccip.R
import app.opass.ccip.model.Speaker
import com.squareup.picasso.Picasso

class SpeakerFragment : Fragment() {
    private var speaker: Speaker? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.item_speaker_image, container, false)
        val speakerImageView = view.findViewById<ImageView>(R.id.speaker_image)
        Picasso.get().load(speaker!!.avatar).into(speakerImageView)

        return view
    }

    companion object {

        fun newInstance(speaker: Speaker): Fragment {
            val speakerFragment = SpeakerFragment()
            speakerFragment.speaker = speaker
            return speakerFragment
        }
    }
}
