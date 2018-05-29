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
import android.support.annotation.NonNull;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
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
        if(logs.isEmpty())
            Toast.makeText(this, R.string.empty_database, Toast.LENGTH_SHORT).show();
        else {
            if (km != 0) {
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
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                try {
                                    writePermissionCheck();
                                } catch (DocumentException e) {
                                    Toast.makeText(getBaseContext(), "Document exception", Toast.LENGTH_SHORT).show();
                                } catch (FileNotFoundException e) {
                                    Toast.makeText(getBaseContext(), "File Not Found exception", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                makePdf();
            } catch (DocumentException | FileNotFoundException e) {
                e.printStackTrace();
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
        if(mSharedPreferences.getString("consumption", "").isEmpty() || mSharedPreferences.getString("transfer", "0").isEmpty() ||
                mSharedPreferences.getString("licence_plate", "").isEmpty()) {
            Toast.makeText(this, R.string.preference_missing, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private int counting() {
        float consumption = Float.valueOf(mSharedPreferences.getString("consumption", ""));
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
        mTransfer.value = Float.valueOf(mSharedPreferences.getString("transfer", ""));

        Document document = new Document(PageSize.A4.rotate());
        File rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        rootDir.mkdirs();
        File file = new File(rootDir, "kniha_jazd.pdf");
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        float[] columnWidths = {1.5f, 1, 1, 2.5f, 2.5f, 1.5f, 1.5f, 1.2f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        BaseFont courier = null;
        try {
            courier = BaseFont.createFont(BaseFont.COURIER, BaseFont.CP1250, BaseFont.EMBEDDED);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Font font = new Font(courier);
        android.util.Log.d("FONT", "makePdf: "+font.getBaseFont().getEncoding());

        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        PdfPCell cell = new PdfPCell(new Phrase("Kniha jázd", font));
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
        cell = new PdfPCell(new Phrase(mSharedPreferences.getString("licence_plate", ""), font));
        table.addCell(cell);

        table.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
        table.addCell("");
        table.addCell("");
        table.addCell("");
        cell = new PdfPCell(new Phrase("Najazdené km celkom", font));
        table.addCell(cell);
        cell = new PdfPCell();
        cell.setCellEvent(new TransferEvent(mTransfer, font));
        cell.setBackgroundColor(new BaseColor(255, 179, 0));
        table.addCell(cell);
        table.addCell("");
        table.addCell("");
        table.addCell("");

        cell = new PdfPCell(new Phrase("Dátum", font));
        cell.setBackgroundColor(new BaseColor(255, 229, 76));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Čas", font));
        cell.setBackgroundColor(new BaseColor(255, 229, 76));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Účel", font));
        cell.setBackgroundColor(new BaseColor(255, 229, 76));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Od", font));
        cell.setBackgroundColor(new BaseColor(255, 229, 76));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Do", font));
        cell.setBackgroundColor(new BaseColor(255, 229, 76));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Stav v štarte", font));
        cell.setBackgroundColor(new BaseColor(255, 229, 76));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Stav v cieli", font));
        cell.setBackgroundColor(new BaseColor(255, 229, 76));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Najazdené km", font));
        cell.setBackgroundColor(new BaseColor(255, 229, 76));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
        table.setHeaderRows(3);

        table.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setFixedHeight(15);
        SimpleDateFormat formatDate = new SimpleDateFormat("d. MMM yyyy", Locale.getDefault());
        SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        for(Log log : logs) {
            cell = new PdfPCell(new Phrase(formatDate.format(log.date), font));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(formatTime.format(log.date), font));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("Servis", font));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(log.start, font));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(log.end, font));
            table.addCell(cell);
            cell = new PdfPCell();
            cell.setCellEvent(new TransferEvent(mTransfer, font));
            table.addCell(cell);
            cell = new PdfPCell();
            cell.setCellEvent(new TransferEvent(mTransfer, Float.valueOf(log.distance), font));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(log.distance, font));
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
        Font f;

        private TransferEvent(Transfer trans, Font font) {
            transfer = trans;
            distance = 0;
            f = font;
        }

        private TransferEvent(Transfer trans, float distance, Font font) {
            transfer = trans;
            this.distance = distance;
            f = font;
        }

        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            transfer.value += distance;
            PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, new Phrase(String.valueOf(transfer.value), f), position.getLeft() + position.getWidth()/2, position.getBottom() + 2, 0);
        }
    }
}