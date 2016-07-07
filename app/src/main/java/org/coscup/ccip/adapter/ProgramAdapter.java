package org.coscup.ccip.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.coscup.ccip.R;
import org.coscup.ccip.model.Program;

import java.text.SimpleDateFormat;
import java.util.List;

public class ProgramAdapter extends RecyclerView.Adapter<ViewHolder> {

    private Context mContext;
    private List<Program> mProgramList;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CardView card;
        public TextView subject, type, allowTimeRange, disableReason;

        public ViewHolder(View itemView) {
            super(itemView);
            card = (CardView) itemView.findViewById(R.id.card);
            subject = (TextView) itemView.findViewById(R.id.subject);
            type = (TextView) itemView.findViewById(R.id.type);
            allowTimeRange = (TextView) itemView.findViewById(R.id.allow_time_range);
            disableReason = (TextView) itemView.findViewById(R.id.disable_reason);
        }
    }

    public ProgramAdapter(Context context, List<Program> programList) {
        mContext = context;
        mProgramList = programList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_program, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final ViewHolder holder = ((ViewHolder) viewHolder);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss");

        final Program program = mProgramList.get(position);
        holder.subject.setText(program.getSubject());
    }

    @Override
    public int getItemCount() {
        return mProgramList.size();
    }
}
