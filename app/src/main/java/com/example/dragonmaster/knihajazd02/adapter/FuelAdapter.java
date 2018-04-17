package com.example.dragonmaster.knihajazd02.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.dragonmaster.knihajazd02.R;
import com.example.dragonmaster.knihajazd02.model.Fuel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class FuelAdapter extends RealmRecyclerViewAdapter<Fuel, FuelAdapter.ViewHolder> {

    private Context mContext;

    public FuelAdapter(Context context, @NonNull OrderedRealmCollection<Fuel> fuels) {
        super(fuels, true);
        mContext = context;
    }
    @Override
    public FuelAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new FuelAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fuel_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Fuel fuel = getItem(position);
        SimpleDateFormat format = new SimpleDateFormat("MMM", Locale.getDefault());
        holder.mMonth.setText(format.format(fuel.date));
        format = new SimpleDateFormat("d", Locale.getDefault());
        holder.mDay.setText(format.format(fuel.date));
        holder.mAmount.setText(fuel.amount);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.fuel_month)
        TextView mMonth;
        @BindView(R.id.fuel_day)
        TextView mDay;
        @BindView(R.id.fuel_amount)
        TextView mAmount;


        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
