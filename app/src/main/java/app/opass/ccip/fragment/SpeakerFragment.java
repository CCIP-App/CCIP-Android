package app.opass.ccip.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import app.opass.ccip.R;
import app.opass.ccip.model.Speaker;
import com.squareup.picasso.Picasso;

public class SpeakerFragment extends Fragment {
    private Speaker speaker;

    public static Fragment newInstance(Speaker speaker) {
        SpeakerFragment speakerFragment = new SpeakerFragment();
        speakerFragment.speaker = speaker;
        return speakerFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.item_speaker_image, container, false);
        ImageView speakerImageView = view.findViewById(R.id.speaker_image);
        Picasso.get().load(speaker.getAvatar()).into(speakerImageView);

        return view;
    }
}
