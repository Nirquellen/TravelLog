package com.example.dragonmaster.knihajazd02.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.dragonmaster.knihajazd02.R;
import com.example.dragonmaster.knihajazd02.adapter.LogJournal;
import com.example.dragonmaster.knihajazd02.adapter.PlaceArrayAdapter;
import com.example.dragonmaster.knihajazd02.api.APIClient;
import com.example.dragonmaster.knihajazd02.api.ResultDistanceMatrix;
import com.example.dragonmaster.knihajazd02.model.Log;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

/**
 * Created by Dragon Master on 29.3.2018.
 */

public class TravelFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LogJournal.OnPopUpMenuClickedListener {

    private static final String LOG_TAG = "TravelFragment";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;

    private APIClient mClient;
    private Calendar mCalendar;
    private LogJournal mLogJournal;
    private String mDistance;
    private Integer mId = null;
    private View mHighlighted = null;
    private Realm mRealm;
    private SimpleDateFormat format = new SimpleDateFormat("d. MMM. yyyy HH:mm", Locale.getDefault());
    private Unbinder mUnbinder;

    @BindView(R.id.result) EditText result;
    @BindView(R.id.date) EditText date;

    @BindView(R.id.layout_focus) RelativeLayout mLayout;
    @BindView(R.id.logs_wrapper) LinearLayout mLogsWrapper;
    @BindView(android.R.id.list) RecyclerView mList;

    @BindView(R.id.startPoint) AutoCompleteTextView mStartPoint;
    @BindView(R.id.endPoint) AutoCompleteTextView mEndPoint;

    public static TravelFragment newInstance() {
        return new TravelFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCalendar = Calendar.getInstance();
        mClient = new APIClient();
        mRealm = Realm.getDefaultInstance();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(getActivity(), GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.travel_layout, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        final RealmResults<Log> logs = mRealm.where(Log.class).sort("date", Sort.DESCENDING).findAll();
        mLogJournal = new LogJournal(getActivity(), logs, this);
        mList.setAdapter(mLogJournal);
        mList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mList.setHasFixedSize(true);

        mStartPoint.setThreshold(3);
        mEndPoint.setThreshold(3);
        mStartPoint.setOnItemClickListener(mAutoCompleteClickListener);
        mEndPoint.setOnItemClickListener(mAutoCompleteClickListener);
        AutocompleteFilter mFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();
        mPlaceArrayAdapter = new PlaceArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, mFilter);
        mStartPoint.setAdapter(mPlaceArrayAdapter);
        mEndPoint.setAdapter(mPlaceArrayAdapter);

        return view;
    }

    @OnClick(R.id.date)
    public void onDateClick(View view) {
        new DatePickerDialog(getActivity(), dateListener,
                mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @OnClick(R.id.save)
    public void onSaveButtonClick(View view) {
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        Log mLog = new Log();
        if(date.getText().toString().isEmpty() || mStartPoint.getText().toString().isEmpty() ||
                mEndPoint.getText().toString().isEmpty() || result.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), R.string.log_missing, Toast.LENGTH_SHORT).show();
        } else {
            try {
                mLog.date = format.parse(date.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mLog.start = mStartPoint.getText().toString();
            mLog.end = mEndPoint.getText().toString();
            mLog.distance = result.getText().toString();
            if (mId == null)
                mLog.id = (mRealm.where(Log.class).count() > 0) ? (mRealm.where(Log.class).max("id")).intValue() + 1 : 1;
            else
                mLog.id = mId;
            if (mLog.date != null)
                saveResult(mLog);
            else
                Toast.makeText(getActivity(), "@strings/date_toast", Toast.LENGTH_SHORT).show();
            mStartPoint.setText("");
            mEndPoint.setText("");
            date.setText("");
            result.setText("");
        }
    }

    private AdapterView.OnItemClickListener mAutoCompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            android.util.Log.i(LOG_TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            android.util.Log.i(LOG_TAG, "Fetching details for ID: " + item.placeId);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                android.util.Log.e(LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
            }
            if(!TextUtils.isEmpty(mStartPoint.getText()) && !TextUtils.isEmpty(mEndPoint.getText())) {
                mLayout.requestFocus();
                fetchDistance();
            }
            places.release();
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        android.util.Log.i(LOG_TAG, "Google Places API connected.");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        android.util.Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(getActivity(),"Google Places API connection failed with error code:" + connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        android.util.Log.e(LOG_TAG, "Google Places API connection suspended.");
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
            new TimePickerDialog(getActivity(), timeListener, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true).show();
            //date.setText(format.format(mCalendar.getTime()));
        }
    };

    TimePickerDialog.OnTimeSetListener timeListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
            mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mCalendar.set(Calendar.MINUTE, minute);
            date.setText(format.format(mCalendar.getTime()));
        }
    };

    private void fetchDistance() {

        String orig = mStartPoint.getText().toString();
        String dest = mEndPoint.getText().toString();
        Map<String, String> params = new HashMap<>();
        params.put("origins", orig);
        params.put("destinations", dest);
        params.put("key", APIClient.GOOGLE_PLACE_API_KEY);

        Call<ResultDistanceMatrix> call = mClient.getInterface().getDistance(params);

        call.enqueue(new Callback<ResultDistanceMatrix>() {
            @Override
            public void onResponse(@NonNull Call<ResultDistanceMatrix> call, @NonNull Response<ResultDistanceMatrix> response) {

                ResultDistanceMatrix resultDistance = response.body();
                if (resultDistance != null && "OK".equalsIgnoreCase(resultDistance.status)) {
                    mEndPoint.setText(resultDistance.destination.get(0).replaceAll("\\d","").trim());
                    mStartPoint.setText(resultDistance.origin.get(0).replaceAll("\\d","").trim());
                    ResultDistanceMatrix.InfoDistanceMatrix infoDistanceMatrix = resultDistance.rows.get(0);
                    ResultDistanceMatrix.InfoDistanceMatrix.DistanceElement distanceElement = infoDistanceMatrix.elements.get(0);
                    if ("OK".equalsIgnoreCase(distanceElement.status)) {
                        ResultDistanceMatrix.InfoDistanceMatrix.ValueItem itemDistance = distanceElement.distance;
                        mDistance = String.valueOf(itemDistance.text);
                        result.setText(mDistance.replaceAll("[^0-9.]", ""));
                    } else {
                        result.setText(distanceElement.status);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResultDistanceMatrix> call, @NonNull Throwable t) {
                call.cancel();
            }
        });
    }

    private void saveResult(final Log log) {
        mId = null;
        if(mHighlighted != null){
            mHighlighted.setBackgroundResource(0);
            mHighlighted = null;
        }
        try {
            mRealm = Realm.getDefaultInstance();
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    realm.insertOrUpdate(log);
                }
            });
        } finally {
            if(mRealm != null) {
                mRealm.close();
            }
        }
    }

    @Override
    public void onEditClicked(View view, Log log) {
        editLog(view, log);
    }

    @Override
    public void onDeleteClicked(Log log) {
        deleteLog(log);
    }

    private void editLog(View view, Log edit) {
        if(mHighlighted != null) {
            mHighlighted.setBackgroundResource(0);
        }
        mHighlighted = view;
        mHighlighted.setBackgroundResource(R.color.colorSecondaryLight);
        if(edit != null) {
            mStartPoint.setText(edit.start);
            mEndPoint.setText(edit.end);
            result.setText(edit.distance);
            mId = edit.id;
            date.setText(format.format(edit.date));
        }
    }

    private void deleteLog(final Log log) {
        try {
            mRealm = Realm.getDefaultInstance();
            mRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    RealmResults<Log> result = realm.where(Log.class).equalTo("id", log.id).findAll();
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
