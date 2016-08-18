package org.coscup.ccip.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.internal.bind.util.ISO8601Utils;

import org.coscup.ccip.R;
import org.coscup.ccip.model.Program;
import org.coscup.ccip.util.JsonUtil;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProgramDetailActivity extends TrackActivity {

    public static final String INTENT_EXTRA_PROGRAM = "program";
    private static final SimpleDateFormat SDF1 = new SimpleDateFormat("MM/dd HH:mm");
    private static final SimpleDateFormat SDF2 = new SimpleDateFormat("HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_detail);

        final Program program = JsonUtil.fromJson(getIntent().getStringExtra(INTENT_EXTRA_PROGRAM), Program.class);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (getResources().getConfiguration().locale.getLanguage().startsWith("zh")) {
            toolbar.setTitle(program.getRoomname());
        } else {
            toolbar.setTitle(program.getRoom());
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView speakername, subject, time, type, lang, speakerInfo, programAbstract;
        speakername = (TextView) findViewById(R.id.speakername);
        subject = (TextView) findViewById(R.id.subject);
        time = (TextView) findViewById(R.id.time);
        type = (TextView) findViewById(R.id.type);
        lang = (TextView) findViewById(R.id.lang);
        speakerInfo = (TextView) findViewById(R.id.speakerinfo);
        programAbstract = (TextView) findViewById(R.id.program_abstract);

        speakername.setText(program.getSpeakername());
        subject.setText(program.getSubject());

        try {
            StringBuffer timeString = new StringBuffer();
            Date startDate = ISO8601Utils.parse(program.getStarttime(), new ParsePosition(0));
            timeString.append(SDF1.format(startDate));
            timeString.append(" ~ ");
            Date endDate = ISO8601Utils.parse(program.getEndtime(), new ParsePosition(0));
            timeString.append(SDF2.format(endDate));

            timeString.append(", " + ((endDate.getTime() - startDate.getTime()) / 1000 / 60) + getResources().getString(R.string.min));

            time.setText(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (getResources().getConfiguration().locale.getLanguage().startsWith("zh")) {
            type.setText(program.getTypenamezh());
        } else {
            type.setText(program.getTypenameen());
        }
        lang.setText(program.getLang());
        speakerInfo.setText(program.getSpeakerintro());
        programAbstract.setText(program.getAbstract());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
