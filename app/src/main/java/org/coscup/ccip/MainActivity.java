package org.coscup.ccip;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Uri uri = getIntent().getData();
        if (uri != null) {
            TextView hello = (TextView) findViewById(R.id.hello);
            String token = uri.getQueryParameter("token");

            hello.setText(token);
        }

    }
}
