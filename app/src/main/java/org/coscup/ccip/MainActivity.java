package org.coscup.ccip;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.coscup.ccip.model.Attendee;
import org.coscup.ccip.network.CCIPClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Uri uri = getIntent().getData();
        if (uri != null) {
            final TextView hello = (TextView) findViewById(R.id.hello);
            final String token = uri.getQueryParameter("token");

            hello.setText(token);
            Call<Attendee> attendee = CCIPClient.get().status(token);
            attendee.enqueue(new Callback<Attendee>() {
                @Override
                public void onResponse(Call<Attendee> call, Response<Attendee> response) {
                    if (response.isSuccessful()) {
                        Attendee attendee = response.body();
                        hello.setText(attendee.getUserId());
                    }
                }

                @Override
                public void onFailure(Call<Attendee> call, Throwable t) {
                }
            });
        }

    }
}
