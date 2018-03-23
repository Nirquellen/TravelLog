package com.example.dragonmaster.knihajazd02;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Dragon Master on 23.3.2018.
 */

public class LogJournal extends RecyclerView.Adapter<LogJournal.ViewHolder>  {

    private Context mContext;
    private List<Log> mLogs;

    public LogJournal(@NonNull List<Log> logs) {
        mLogs = logs;
    }

    public void addLog(@NonNull Log log) {
        mLogs.add(log);
        Collections.sort(mLogs);
        notifyDataSetChanged();
    }

    @Override
    public LogJournal.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.log_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log log = mLogs.get(position);
        holder.mDate.setText(log.date);
        holder.mStart.setText(log.start);
        holder.mEnd.setText(log.end);
        holder.mDist.setText(log.distance);
    }

    @Override
    public int getItemCount() {
        return mLogs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.logDate)
        TextView mDate;
        @BindView(R.id.logFrom)
        TextView mStart;
        @BindView(R.id.logTo)
        TextView mEnd;
        @BindView(R.id.distance)
        TextView mDist;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
