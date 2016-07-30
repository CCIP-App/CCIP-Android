package org.coscup.ccip.adapter;

import android.content.Context;
import android.content.Intent;
import android.drm.ProcessedData;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.internal.bind.util.ISO8601Utils;

import org.coscup.ccip.ProgramDetailActivity;
import org.coscup.ccip.R;
import org.coscup.ccip.model.Program;
import org.coscup.ccip.model.Type;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ProgramAdapter extends RecyclerView.Adapter<ViewHolder> {

    private Context mContext;
    private List<Program> mProgramList;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CardView card;
        public TextView subject, type, room, endTime, lang;

        public ViewHolder(View itemView) {
            super(itemView);
            card = (CardView) itemView.findViewById(R.id.card);
            subject = (TextView) itemView.findViewById(R.id.subject);
            type = (TextView) itemView.findViewById(R.id.type);
            room = (TextView) itemView.findViewById(R.id.room);
            endTime = (TextView) itemView.findViewById(R.id.end_time);
            lang = (TextView) itemView.findViewById(R.id.lang);
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
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        final Program program = mProgramList.get(position);

        holder.room.setText(program.getRoom());

        if (mContext.getResources().getConfiguration().locale.getLanguage().startsWith("zh")) {
            holder.type.setText(program.getTypenamezh());
        } else {
            holder.type.setText(program.getTypenameen());
        }

        holder.subject.setText(program.getSubject());

        try {
            Date startDate = ISO8601Utils.parse(program.getStarttime(), new ParsePosition(0));
            Date endDate = ISO8601Utils.parse(program.getEndtime(), new ParsePosition(0));
            holder.endTime.setText("~ " + sdf.format(endDate) + ", " +
                    ((endDate.getTime() - startDate.getTime()) / 1000 / 60) + mContext.getResources().getString(R.string.min));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (program.getLang() != null && !program.getLang().equals("ZH")) {
            holder.lang.setText(program.getLang());
        }

        holder.card.setClickable(true);
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Gson gson = new Gson();
                Intent intent = new Intent();
                intent.setClass(mContext, ProgramDetailActivity.class);
                intent.putExtra(ProgramDetailActivity.INTENT_EXTRA_PROGRAM, gson.toJson(program));
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mProgramList.size();
    }
}
