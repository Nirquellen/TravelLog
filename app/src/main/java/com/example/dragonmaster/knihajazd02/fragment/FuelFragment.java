package com.example.dragonmaster.knihajazd02.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dragonmaster.knihajazd02.R;
import com.example.dragonmaster.knihajazd02.adapter.FuelAdapter;
import com.example.dragonmaster.knihajazd02.adapter.RecyclerItemClickListener;
import com.example.dragonmaster.knihajazd02.model.Fuel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class FuelFragment extends Fragment implements FuelAdapter.OnPopUpMenuClickedListener {

    private Calendar mCalendar;
    private Realm mRealm;
    private SimpleDateFormat format = new SimpleDateFormat("d. MMM. yyyy", Locale.getDefault());
    private Unbinder mUnbinder;
    private Integer mId = null;
    private View mHighlighted = null;
    private FuelAdapter mFuelAdapter;

    @BindView(android.R.id.list) RecyclerView mList;
    @BindView(R.id.fuels_date) EditText mDate;
    @BindView(R.id.fuels_amount) EditText mAmount;
    //@BindView(R.id.fuels_save)

    public static FuelFragment newInstance() {
        return new FuelFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCalendar = Calendar.getInstance();
        mRealm = Realm.getDefaultInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fuels_layout, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        final RealmResults<Fuel> fuels = mRealm.where(Fuel.class).sort("date", Sort.DESCENDING).findAll();
        mFuelAdapter = new FuelAdapter(getActivity(), fuels, this);
        mList.setAdapter(mFuelAdapter);
        mList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mList.setHasFixedSize(true);
        return view;
    }

    @OnClick(R.id.fuels_date)
    public void onDateClick(View view) {
        new DatePickerDialog(getActivity(), dateListener,
                mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @OnClick(R.id.fuels_save)
    public void onSaveClick(View view) {
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        if(mDate.getText().toString().isEmpty() || mAmount.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), R.string.log_missing, Toast.LENGTH_SHORT).show();
        } else {
            Fuel mFuel = new Fuel();
            try {
                mFuel.date = format.parse(mDate.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (!mAmount.getText().equals(""))
                mFuel.amount = mAmount.getText().toString();
            else
                Toast.makeText(getActivity(), "@strings/amount_toast", Toast.LENGTH_SHORT).show();
            if (mId == null)
                mFuel.id = (int) (mRealm.where(Fuel.class).max("id")) + 1;
            else
                mFuel.id = mId;
            if (mFuel.date != null)
                saveResult(mFuel);
            else
                Toast.makeText(getActivity(), "@strings/date_toast", Toast.LENGTH_SHORT).show();
            mDate.setText("");
            mAmount.setText("");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, monthOfYear);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            mDate.setText(format.format(mCalendar.getTime()));
        }
    };

    private void saveResult(final Fuel fuel) {
        mId = null;
        if(mHighlighted != null){
            mHighlighted.setBackgroundResource(0);
            mHighlighted = null;
        }
        try {
            mRealm = Realm.getDefaultInstance();
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insertOrUpdate(fuel);
                }
            });
        } finally {
            if(mRealm != null) {
                mRealm.close();
            }
        }
    }

    @Override
    public void onEditClicked(View view, Fuel fuel) {
        editFuel(view, fuel);
    }

    @Override
    public void onDeleteClicked(Fuel fuel) {
        deleteFuel(fuel);
    }

    private void editFuel(View view, Fuel edit) {
        if(mHighlighted != null) {
            mHighlighted.setBackgroundResource(0);
        }
        mHighlighted = view;
        mHighlighted.setBackgroundResource(R.color.colorSecondaryLight);
        if(edit != null) {
            mAmount.setText(edit.amount);
            mId = edit.id;
            mDate.setText(format.format(edit.date));
        }
    }

    private void deleteFuel(final Fuel fuel) {
        try {
            mRealm = Realm.getDefaultInstance();
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    RealmResults<Fuel> result = realm.where(Fuel.class).equalTo("id", fuel.id).findAll();
                    result.deleteAllFromRealm();
                }
            });
        } finally {
            if(mRealm != null) {
                mRealm.close();
            }
        }
    }
}
