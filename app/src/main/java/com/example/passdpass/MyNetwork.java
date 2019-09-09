package com.example.passdpass;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyNetwork extends AppCompatActivity {

    private  String ssid;
    private  String password;
    private  String passwordUpdate;
    private  int type;
    private  String toBarcode;
    private  String toShare;
    private  ImageView qrImage;
    private  String wifiID;
    private  WifiConfiguration conf;
    private  WifiManager wifiManager;
    private  EditText qrPassword;
    private  Bitmap bitmap;
    private  List<WifiConfig> wifiList;
    private  FirebaseFirestore db;

    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_network);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String curentemail = user.getEmail();
        TextView email_display = findViewById(R.id.emailID_UA);
        email_display.setText(curentemail);


        db = FirebaseFirestore.getInstance();

        TextView qrSSID = findViewById(R.id.edTxtSSID_MN);
        qrPassword = findViewById(R.id.edTxtPassword_MN);
        qrImage = findViewById(R.id.QR_Image_MN);

        Button btnUpdateConnect = findViewById(R.id.update_and_connect_MN);
        Button btnShare = findViewById(R.id.btnShare_MN);
        Button btnGenerateQR = findViewById(R.id.btnGenerateQR_MN);
        Button btnDelete = findViewById(R.id.btnDelete);

        ssid = (getIntent().getStringExtra("wifiSSID"));
        password =(getIntent().getStringExtra("wifiPassword"));
        type = getIntent().getIntExtra("type", 0);
        wifiID = getIntent().getStringExtra("wifiID");



        qrSSID.setText(ssid);
        qrPassword.setText(password);

        conf = new WifiConfiguration();
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);



        btnUpdateConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                wifiList = new ArrayList<>();
                passwordUpdate = qrPassword.getText().toString().trim();
                WifiConfig wifiConfig = new WifiConfig();
                wifiConfig.setSsid(ssid);
                wifiConfig.setPassword(passwordUpdate);
                wifiConfig.setUserId(userID);
                wifiConfig.setType(type);
                wifiConfig.setId(wifiID);

                /*--------update ----------------*/
                db.collection("wifis").document(wifiConfig.getId())
                        .update(
                                "password",passwordUpdate
                        )
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MyNetwork.this, "Wifi Config updated in Database", Toast.LENGTH_LONG).show();
                            }
                        });

                /*--- ADD local ---*/

                conf.SSID = String.format("\"%s\"", ssid);
                conf.preSharedKey = String.format("\"%s\"", password);


                wifiManager.addNetwork(conf);
                wifiManager.setWifiEnabled(true);


                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();

                for (WifiConfiguration i : list) {
                    if (i.SSID != null && i.SSID.equals(ssid)) {
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(i.networkId, true);
                        wifiManager.reconnect();
                        break;
                    }
                }
                startActivity(new Intent(MyNetwork.this, MySavedNetworks.class));



            }
        });

        btnGenerateQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                passwordUpdate = qrPassword.getText().toString().trim();
                System.out.println("SSID: " + ssid + " Pass: "+ password );
                if(password.length()> 7){

                    toBarcode = "WIFI:T:WPA;S:"+ssid+";P:"+password+";;";
                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                    try {
                        BitMatrix bitMatrix = multiFormatWriter.encode(toBarcode, BarcodeFormat.QR_CODE, 500, 500);
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        bitmap = barcodeEncoder.createBitmap(bitMatrix);

                        qrImage.setImageBitmap(bitmap);


                    } catch (WriterException e) {
                        e.printStackTrace();
                    }

                }
                else{
                    Toast.makeText(MyNetwork.this, "Please enter a valid password", Toast.LENGTH_LONG).show();
                }

            }
        });


        qrImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {


                String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/PassDPass";
                File dir = new File(file_path);
                if(!dir.exists())
                    dir.mkdirs();
                    File file = new File(dir, "Wifi_QR_"+ssid+".jpg");
                FileOutputStream fOut = null;

                try {
                    fOut = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                    fOut.flush();
                    fOut.close();
                }
                 catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Toast.makeText(MyNetwork.this, "QR Code saved to Phone", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                file_path = Environment.getExternalStorageDirectory()+
                        "/PassDPass/Wifi_QR_"+ssid+".jpg";

                File imageFileToShare = new File(file_path);
                Uri uri = Uri.fromFile(imageFileToShare);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(intent, "Share QR Code via"));
                return false;
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordUpdate = qrPassword.getText().toString().trim();
                toShare = "The Wifi: " + ssid + "\n" + " The Password: " + passwordUpdate + "";
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Sharing Wifi");
                intent.putExtra(Intent.EXTRA_TEXT, toShare);
                startActivity(Intent.createChooser(intent, "Share Wifi"));


            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MyNetwork.this);
                builder.setTitle("Are you sure you want to delete this wifi " + ssid + " ?");
                builder.setMessage(" Deleteion is permanent!");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WifiConfig wifiConfig = new WifiConfig();
                        wifiConfig.setId(wifiID);
                        db.collection("wifis").document(wifiConfig.getId())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MyNetwork.this, "Wifi "+ ssid + " deleted from Database", Toast.LENGTH_LONG).show();
                                    }
                                });
                        startActivity(new Intent(MyNetwork.this, MySavedNetworks.class));
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog ad = builder.create();
                ad.show();
            }
        });
    }
}
