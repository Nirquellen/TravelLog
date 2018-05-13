package com.example.dragonmaster.knihajazd02.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.dragonmaster.knihajazd02.R;
import com.example.dragonmaster.knihajazd02.adapter.TabsPagerAdapter;
import com.example.dragonmaster.knihajazd02.model.Fuel;
import com.example.dragonmaster.knihajazd02.model.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity{

    private static final int REQUEST_WRITE_PERMISSION = 42;
    private SharedPreferences mSharedPreferences;
    private RealmResults<Log> logs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(new TabsPagerAdapter(getSupportFragmentManager()));

        TabLayout mTabLayout = findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.export:
                if (checkPreferences())
                    export();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void export() {
        int km = counting();
        if(km != 0) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage(getResources().getString(R.string.km_left, km))
                    .setCancelable(true).setNeutralButton(R.string.okay,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }).show();
        } else {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            try {
                                writePermissionCheck();
                            } catch (DocumentException e) {
                                e.printStackTrace();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.cancel();
                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.pdf_export_question).setPositiveButton(R.string.yes, dialogClickListener)
                    .setNegativeButton(R.string.no, dialogClickListener).show();
        }
    }

    private void writePermissionCheck() throws FileNotFoundException, DocumentException {
        // runtime permissions start with the Marshmallow ->
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Lower than Marshmallow -> permissions were granted during the install process
            makePdf();
        } else {
            // Let's check whethere we already have the permission
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted -> save the data
                makePdf();
            } else {
                // Android helper method to tell us if it's useful to show a hint
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Show some alert explaining why it is important to grant the permission
                    showWritePermissionRationale(this);
                } else {
                    // Just straight to the point
                    requestPermissions(
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_PERMISSION);
                }
            }
        }
    }

    private void showWritePermissionRationale(final Context context) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(R.string.write_permission_title);
        alertBuilder.setMessage(R.string.write_permission_text);
        alertBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // this way you can get to the screen to set the permissions manually
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", getPackageName(), null));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    private Boolean checkPreferences() {
        if(mSharedPreferences.getString("consumption", null).isEmpty() || mSharedPreferences.getString("transfer", null).isEmpty() ||
                mSharedPreferences.getString("licence_plate", null).isEmpty()) {
            Toast.makeText(this, R.string.preference_missing, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private int counting() {
        float consumption = Float.valueOf(mSharedPreferences.getString("consumption", null));
        android.util.Log.d("Main Activity", String.valueOf(consumption));
        float km = 0;
        float fuel = 0;
        Realm mRealm = Realm.getDefaultInstance();
        logs = mRealm.where(Log.class).findAll();
        for(Log log : logs) {
            km += Float.valueOf(log.distance.replaceAll("[^0-9.]", ""));
        }
        RealmResults<Fuel> fuels = mRealm.where(Fuel.class).findAll();
        for(Fuel f : fuels) {
            fuel += Float.valueOf(f.amount);
        }
        return (int)(fuel/consumption*100 - km);
    }

    private void makePdf() throws DocumentException, FileNotFoundException {
        Transfer mTransfer = new Transfer();
        mTransfer.value = Float.valueOf(mSharedPreferences.getString("transfer", null));

        Document document = new Document(PageSize.A4.rotate());
        File rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        rootDir.mkdirs();
        File file = new File(rootDir, "kniha_jazd.pdf");
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        float[] columnWidths = {1.5f, 1, 1, 2.5f, 2.5f, 1.5f, 1.5f, 1.2f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);

        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        PdfPCell cell = new PdfPCell(new Phrase("Kniha jázd"));
        cell.setColspan(3);
        cell.setFixedHeight(34);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(new BaseColor(0, 191, 165));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        table.addCell("");
        table.addCell("");
        table.addCell("");
        table.addCell("");
        table.getDefaultCell().setBackgroundColor(new BaseColor(255, 179, 0));
        table.addCell(mSharedPreferences.getString("licence_plate", null));

        table.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
        table.addCell("");
        table.addCell("");
        table.addCell("");
        table.addCell("Najazdené km celkom");
        cell = new PdfPCell();
        cell.setCellEvent(new TransferEvent(mTransfer));
        cell.setBackgroundColor(new BaseColor(255, 179, 0));
        table.addCell(cell);
        table.addCell("");
        table.addCell("");
        table.addCell("");

        table.getDefaultCell().setBackgroundColor(new BaseColor(255, 229, 76));
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setBorder(Rectangle.BOX);
        table.addCell("Dátum");
        table.addCell("Čas");
        table.addCell("Účel");
        table.addCell("Od");
        table.addCell("Do");
        table.addCell("Stav v štarte");
        table.addCell("Stav v cieli");
        table.addCell("Najazdené km");
        table.setHeaderRows(3);

        table.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setFixedHeight(15);
        SimpleDateFormat formatDate = new SimpleDateFormat("d. MMM. yyyy", Locale.getDefault());
        SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        for(Log log : logs) {
            table.addCell(formatDate.format(log.date));
            table.addCell(formatTime.format(log.date));
            table.addCell("Servis");
            table.addCell(log.start);
            table.addCell(log.end);
            cell = new PdfPCell();
            cell.setCellEvent(new TransferEvent(mTransfer));
            table.addCell(cell);
            cell = new PdfPCell();
            cell.setCellEvent(new TransferEvent(mTransfer, Float.valueOf(log.distance)));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(log.distance));
            cell.setHorizontalAlignment(Rectangle.ALIGN_RIGHT);
            table.addCell(cell);
        }

        if(document.add(table)) {
            Toast.makeText(this, R.string.pdf_done, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.pdf_not_done, Toast.LENGTH_SHORT).show();
        }
        document.close();
    }

    class Transfer {
        float value = 0;
    }

    public class TransferEvent implements PdfPCellEvent {
        float distance;
        Transfer transfer;

        private TransferEvent(Transfer trans) {
            transfer = trans;
            distance = 0;
        }

        private TransferEvent(Transfer trans, float distance) {
            transfer = trans;
            this.distance = distance;
        }

        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            transfer.value += distance;
            PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, new Phrase(String.valueOf(transfer.value)), position.getLeft() + position.getWidth()/2, position.getBottom() + 2, 0);
        }
    }
}