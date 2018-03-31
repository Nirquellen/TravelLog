package com.example.dragonmaster.knihajazd02.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dragonmaster.knihajazd02.R;
import com.example.dragonmaster.knihajazd02.model.Log;

import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Dragon Master on 23.3.2018.
 */

public class LogJournal extends RealmRecyclerViewAdapter<Log, LogJournal.ViewHolder>  {

    private Context mContext;

    public LogJournal(Context context, @NonNull OrderedRealmCollection<Log> logs) {
        super(logs, true);
        mContext = context;
    }

    @Override
    public LogJournal.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.log_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log log = getItem(position);
        SimpleDateFormat format = new SimpleDateFormat("d. MMM. yyyy");
        holder.mDate.setText(format.format(log.date));
        holder.mStart.setText(log.start);
        holder.mEnd.setText(log.end);
        holder.mDist.setText(log.distance);
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


        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
