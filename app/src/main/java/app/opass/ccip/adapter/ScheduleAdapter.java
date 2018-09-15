package app.opass.ccip.adapter;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.internal.bind.util.ISO8601Utils;

import app.opass.ccip.R;
import app.opass.ccip.model.Submission;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import app.opass.ccip.model.Submission;

public class ScheduleAdapter extends RecyclerView.Adapter<ViewHolder> {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm");

    private Context mContext;
    private List<List<Submission>> mSubmissionSlotList;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView startTimeText;
        private RecyclerView submissionView;

        public ViewHolder(View itemView) {
            super(itemView);
            startTimeText = (TextView) itemView.findViewById(R.id.start_time);
            submissionView = (RecyclerView) itemView.findViewById(R.id.programs);
        }
    }

    public ScheduleAdapter(Context context, List<List<Submission>> submissionSlotList) {
        mContext = context;
        mSubmissionSlotList = submissionSlotList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final ViewHolder holder = ((ViewHolder) viewHolder);

        holder.submissionView.setLayoutManager(new LinearLayoutManager(mContext));
        holder.submissionView.setItemAnimator(new DefaultItemAnimator());

        final List<Submission> submissions = mSubmissionSlotList.get(position);
        try {
            Date date = ISO8601Utils.parse(submissions.get(0).getStart(), new ParsePosition(0));
            holder.startTimeText.setText(SDF.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.submissionView.setAdapter(new SubmissionAdapter(mContext, submissions));
    }

    @Override
    public int getItemCount() {
        return mSubmissionSlotList.size();
    }

    public void update(List<List<Submission>> submissionSlotList) {
        mSubmissionSlotList = submissionSlotList;
        notifyDataSetChanged();
    }
}
