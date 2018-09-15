package app.opass.ccip.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import app.opass.ccip.R;
import app.opass.ccip.model.Speaker;

import app.opass.ccip.model.Speaker;

public class SpeakerFragment extends Fragment {
    private Speaker speaker;
    private ImageView speakerImageView;

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
        speakerImageView = (ImageView) view.findViewById(R.id.speaker_image);
        Picasso.get().load(speaker.getAvatar()).into(speakerImageView);

        return view;
    }
}
