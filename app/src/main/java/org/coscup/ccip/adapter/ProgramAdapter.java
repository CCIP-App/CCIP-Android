package org.coscup.ccip.adapter;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.coscup.ccip.R;
import org.coscup.ccip.model.Program;
import org.coscup.ccip.model.Type;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class ProgramAdapter extends RecyclerView.Adapter<ViewHolder> {

    private Context mContext;
    private List<Program> mProgramList;
    private Map<String, String> roomMap;
    private Map<Integer, Type> typeMap;

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

    public ProgramAdapter(Context context, List<Program> programList, Map<String, String> roomMap, Map<Integer, Type> typeMap) {
        mContext = context;
        mProgramList = programList;
        this.roomMap = roomMap;
        this.typeMap = typeMap;
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
        holder.room.setText(program.getRoom());
        holder.type.setText(program.getType() == null ? "" : typeMap.get(program.getType()).getNamezh());
        holder.subject.setText(program.getSubject());
        holder.endTime.setText(program.getEndtime());

        if (program.getLang() != null && !program.getLang().equals("ZH")) {
            holder.lang.setText(program.getLang());
        }

        holder.card.setClickable(true);
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(mContext)
                        .setTitle(program.getSubject())
                        .setMessage(program.getAbstract())
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mProgramList.size();
    }
}
