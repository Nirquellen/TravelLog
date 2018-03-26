package com.example.dragonmaster.knihajazd02.activity;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.dragonmaster.knihajazd02.R;
import com.example.dragonmaster.knihajazd02.api.ResultDistanceMatrix;
import com.example.dragonmaster.knihajazd02.adapter.LogJournal;
import com.example.dragonmaster.knihajazd02.api.APIClient;
import com.example.dragonmaster.knihajazd02.api.ApiInterface;
import com.example.dragonmaster.knihajazd02.model.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private final static int ALL_PERMISSIONS_RESULT = 101;
    ApiInterface apiService;
    private Calendar mCalendar;
    private Context mContext = this;
    private LogJournal mLogJournal;
    private String mDistance;
    private Realm mRealm;
    private SimpleDateFormat format = new SimpleDateFormat("d. MMM. yyyy");

    @BindView(R.id.startPoint) EditText start;
    @BindView(R.id.endPoint) EditText end;
    @BindView(R.id.result) EditText result;
    @BindView(R.id.date) EditText date;
    @BindView(R.id.save) Button save;
    @BindView(R.id.logs_wrapper) LinearLayout mLogsWrapper;
    @BindView(android.R.id.list) RecyclerView mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mCalendar = Calendar.getInstance();
        mRealm = Realm.getDefaultInstance();

        permissionsToRequest = findUnAskedPermissions(permissions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }
        apiService = APIClient.getClient().create(ApiInterface.class);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(mContext, dateListener,
                        mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLogsWrapper.getVisibility() == View.GONE) {
                    mLogsWrapper.setVisibility(View.VISIBLE);
                }
                Log mLog = new Log();
                try {
                    mLog.date = format.parse(date.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                mLog.start = start.getText().toString().replaceAll("\\d","").trim();
                mLog.end = end.getText().toString().replaceAll("\\d","").trim();
                mLog.distance = mDistance;
                mLog.id = mLogJournal.getItemCount();
                saveResult(mLog);
            }
        });

        RealmResults<Log> logs = mRealm.where(Log.class).sort("date", Sort.DESCENDING).findAll();
        mLogJournal = new LogJournal(mContext, logs);
        mList.setAdapter(mLogJournal);
        mList.setLayoutManager(new LinearLayoutManager(mContext));
        mList.setHasFixedSize(true);
    }

    @OnClick(R.id.button)
    public void onClick(View view) {
        if (this.getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        fetchDistance();
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<>();
        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }
        return result;
    }

    DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, monthOfYear);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            date.setText(format.format(mCalendar.getTime()));
        }
    };

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }
                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }
                }
                break;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void fetchDistance() {

        String orig = start.getText().toString();
        String dest = end.getText().toString();
        Map<String, String> params = new HashMap<>();
        params.put("origins", orig);
        params.put("destinations", dest);
        params.put("key", APIClient.GOOGLE_PLACE_API_KEY);

        Call<ResultDistanceMatrix> call = apiService.getDistance(params);

        call.enqueue(new Callback<ResultDistanceMatrix>() {
            @Override
            public void onResponse(Call<ResultDistanceMatrix> call, Response<ResultDistanceMatrix> response) {

                ResultDistanceMatrix resultDistance = response.body();
                if ("OK".equalsIgnoreCase(resultDistance.status)) {
                    end.setText(resultDistance.destination.get(0));
                    start.setText(resultDistance.origin.get(0));
                    ResultDistanceMatrix.InfoDistanceMatrix infoDistanceMatrix = resultDistance.rows.get(0);
                    ResultDistanceMatrix.InfoDistanceMatrix.DistanceElement distanceElement = infoDistanceMatrix.elements.get(0);
                    if ("OK".equalsIgnoreCase(distanceElement.status)) {
                        //ResultDistanceMatrix.InfoDistanceMatrix.ValueItem itemDuration = distanceElement.duration;
                        ResultDistanceMatrix.InfoDistanceMatrix.ValueItem itemDistance = distanceElement.distance;
                        String totalDistance = String.valueOf(itemDistance.text);
                        //String totalDuration = String.valueOf(itemDuration.text);
                        mDistance = totalDistance;
                        result.setText(mDistance);
                    } else {
                        result.setText(distanceElement.status);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResultDistanceMatrix> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void saveResult(final Log log) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insertOrUpdate(log);
                }
            });
        } finally {
            if(realm != null) {
                realm.close();
            }
        }
    }

    /*@Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putString("from", start.getText().toString());
        outState.putString("to", end.getText().toString());
        outState.putString("result", result.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        start.setText(savedInstanceState.getString("from"));
        end.setText(savedInstanceState.getString("to"));
        result.setText(savedInstanceState.getString("result"));
    }*/
}