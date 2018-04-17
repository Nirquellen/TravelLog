package com.example.dragonmaster.knihajazd02.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.dragonmaster.knihajazd02.R;
import com.example.dragonmaster.knihajazd02.model.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Dragon Master on 23.3.2018.
 */

public class LogJournal extends RealmRecyclerViewAdapter<Log, LogJournal.ViewHolder> {

    private static final String TAG = "LogJournal";
    private Context mContext;

    public LogJournal(Context context, @NonNull OrderedRealmCollection<Log> logs) {
        super(logs, true);
        mContext = context;
    }

    @NonNull
    @Override
    public LogJournal.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.log_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log log = getItem(position);
        SimpleDateFormat format = new SimpleDateFormat("MMM", Locale.getDefault());
        holder.mMonth.setText(format.format(log.date));
        format = new SimpleDateFormat("d", Locale.getDefault());
        holder.mDay.setText(format.format(log.date));
        format = new SimpleDateFormat("HH", Locale.getDefault());
        holder.mHours.setText(format.format(log.date));
        format = new SimpleDateFormat("mm", Locale.getDefault());
        holder.mMinutes.setText(format.format(log.date));
        holder.mStart.setText(log.start.replaceAll(",.*", ""));
        holder.mEnd.setText(log.end.replaceAll(",.*", ""));
        holder.mDist.setText(log.distance);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.month)
        TextView mMonth;
        @BindView(R.id.day)
        TextView mDay;
        @BindView(R.id.logFrom)
        TextView mStart;
        @BindView(R.id.logTo)
        TextView mEnd;
        @BindView(R.id.distance)
        TextView mDist;
        @BindView(R.id.hours)
        TextView mHours;
        @BindView(R.id.minutes)
        TextView mMinutes;
        //@BindView(R.id.log_menu)
        //Button mMenu;


        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            //mMenu.setOnClickListener(this);
        }
    }
}
