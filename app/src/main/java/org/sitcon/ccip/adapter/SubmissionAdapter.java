package org.sitcon.ccip.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.internal.bind.util.ISO8601Utils;

import java.util.Collections;

import org.sitcon.ccip.activity.SubmissionDetailActivity;
import org.sitcon.ccip.R;
import org.sitcon.ccip.model.Submission;
import org.sitcon.ccip.util.AlarmUtil;
import org.sitcon.ccip.util.JsonUtil;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.sitcon.ccip.util.PreferenceUtil;


public class SubmissionAdapter extends RecyclerView.Adapter<ViewHolder> {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm");
    private static final String FORMAT_ENDTIME = "~ %s, %d%s";

    private Context mContext;
    private List<Submission> mSubmissionList;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CardView card;
        public TextView subject, type, room, endTime, lang;
        public ImageView star;

        public ViewHolder(View itemView) {
            super(itemView);
            card = (CardView) itemView.findViewById(R.id.card);
            subject = (TextView) itemView.findViewById(R.id.subject);
            type = (TextView) itemView.findViewById(R.id.type);
            room = (TextView) itemView.findViewById(R.id.room);
            endTime = (TextView) itemView.findViewById(R.id.end_time);
            lang = (TextView) itemView.findViewById(R.id.lang);
            star = (ImageView) itemView.findViewById(R.id.star);
        }
    }

    public SubmissionAdapter(Context context, List<Submission> submissionList) {
        mContext = context;
        mSubmissionList = submissionList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_submission, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final ViewHolder holder = ((ViewHolder) viewHolder);

        final Submission submission = mSubmissionList.get(position);

        holder.room.setText(submission.getRoom());

        holder.subject.setText(submission.getSubject());

        try {
            Date startDate = ISO8601Utils.parse(submission.getStart(), new ParsePosition(0));
            Date endDate = ISO8601Utils.parse(submission.getEnd(), new ParsePosition(0));
            holder.endTime.setText(String.format(FORMAT_ENDTIME, SDF.format(endDate),
                    ((endDate.getTime() - startDate.getTime()) / 1000 / 60),
                    mContext.getResources().getString(R.string.min)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            holder.type.setText(Submission.getTypeString(submission.getType()));
        } catch (Resources.NotFoundException e) {
            holder.type.setText("");
            e.printStackTrace();
        }

        if (!submission.getSummary().isEmpty()) {
            toggleStar(holder.star, isSubmissionStar(mContext, submission));

            holder.star.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateStarSubmissions(mContext, submission);
                    toggleStar(holder.star, isSubmissionStar(mContext, submission));
                }
            });

            holder.card.setClickable(true);
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(mContext, SubmissionDetailActivity.class);
                    intent.putExtra(SubmissionDetailActivity.INTENT_EXTRA_PROGRAM, JsonUtil.toJson(submission));
                    mContext.startActivity(intent);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mSubmissionList.size();
    }

    private boolean isSubmissionStar(Context context, Submission submission) {
        List<Submission> submissions = PreferenceUtil.loadStars(context);
        return submissions != null && submissions.contains(submission);
    }

    private void updateStarSubmissions(Context context, Submission submission) {
        List<Submission> submissions = PreferenceUtil.loadStars(context);
        if (submissions != null) {
            if (submissions.contains(submission)) {
                submissions.remove(submission);
                AlarmUtil.cancelSubmissionAlarm(context, submission);
            } else {
                submissions.add(submission);
                AlarmUtil.setSubmissionAlarm(context, submission);
            }
        } else {
            submissions = Collections.singletonList(submission);
        }
        PreferenceUtil.saveStars(context, submissions);
    }

    private void toggleStar(ImageView star, boolean isStar) {
        if (isStar) {
            star.setImageResource(R.drawable.ic_bookmark_black_24dp);
            star.setColorFilter(mContext.getResources().getColor(R.color.colorAccent));
        } else {
            star.setImageResource(R.drawable.ic_bookmark_border_black_24dp);
            star.setColorFilter(mContext.getResources().getColor(R.color.colorGray));
        }
    }
}
