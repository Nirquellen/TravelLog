package com.example.dragonmaster.knihajazd02.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.dragonmaster.knihajazd02.R;
import com.example.dragonmaster.knihajazd02.model.Fuel;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class FuelAdapter extends RealmRecyclerViewAdapter<Fuel, FuelAdapter.ViewHolder> {

    private static final String TAG = "FuelAdapter";
    private Context mContext;
    private FuelAdapter.OnPopUpMenuClickedListener mListener;

    public FuelAdapter(Context context, @NonNull OrderedRealmCollection<Fuel> fuels, OnPopUpMenuClickedListener fragment) {
        super(fuels, true);
        mContext = context;
        mListener = fragment;
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

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        @BindView(R.id.fuel_month)
        TextView mMonth;
        @BindView(R.id.fuel_day)
        TextView mDay;
        @BindView(R.id.fuel_amount)
        TextView mAmount;
        @BindView(R.id.fuel_menu)
        ImageButton mMenu;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mMenu.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        // Kdyz uz tady mas nabindovany ten Butterknife, klidne jsi ho mohla pouzit i pro onClick eventy (@OnClick)
        @Override
        public void onClick(final View view) {
            android.util.Log.d(TAG, "onClick: happens");
            PopupMenu popup = new PopupMenu(mContext, mMenu);
            popup.inflate(R.menu.options_menu);
            popup.show();
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.item1:
                            mListener.onEditClicked(itemView, getItem(getAdapterPosition()));
                            break;
                        case R.id.item2:
                            mListener.onDeleteClicked(getItem(getAdapterPosition()));
                            break;
                    }
                    return false;
                }
            });
        }

        @Override
        public boolean onLongClick(View view) {
            mListener.onEditClicked(view, getItem(getAdapterPosition()));
            return false;
        }
    }

    public interface OnPopUpMenuClickedListener {
        void onEditClicked(View v, Fuel fuel);
        void onDeleteClicked(Fuel fuel);
    }
}
