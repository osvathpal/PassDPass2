package com.example.passdpass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.List;

public class ConnectShareList extends AppCompatActivity {

    private static final String TAG = "PassDPass";
    String ssid;
    String password=null;
    int type;
    String toBarcode;
    String toShare;
    ImageView qrImage;
    String wifiID;

    WifiConfiguration conf;
    WifiManager wifiManager;
    EditText qrSSID;
    EditText qrPassword;
    Button btnAddAndConnect;
    Button btnGenerateQR;
    Button btnShare;
    List<WifiConfiguration> listOfSavedWifis;

    FirebaseAuth firebaseAuth;
    //private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseFirestore db;
    private TextView email_display;
    List<WifiConfig> wifiList;
    int checkSaved = 0;
    String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_share_list);

        db = FirebaseFirestore.getInstance();

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String curentemail = user.getEmail();
        email_display = findViewById(R.id.emailID_UA);
        email_display.setText(curentemail);

        qrSSID = findViewById(R.id.edTxtSSID_list);
        qrPassword = findViewById(R.id.edTxtPassword_list);
        btnAddAndConnect = findViewById(R.id.add_and_connect_list);
        btnGenerateQR = findViewById(R.id.btnGenerateQR_list);
        btnShare = findViewById(R.id.btnShare_MN);
        qrImage = findViewById(R.id.QR_Image_list);

        ssid = (getIntent().getStringExtra("ssid"));
        qrSSID.setText(ssid);
        password = qrPassword.getText().toString().trim();

        conf = new WifiConfiguration();
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);

        verifyWifi(ssid);

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password = qrPassword.getText().toString().trim();
                if(password.length()> 7) {
                    toShare = "The Wifi: " + ssid + "\n" + " The Password: " + password + "";
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Sharing Wifi");
                    intent.putExtra(Intent.EXTRA_TEXT, toShare);
                    startActivity(Intent.createChooser(intent, "Share Wifi"));
                }
                else{
                Toast.makeText(ConnectShareList.this, "Please enter a valid password", Toast.LENGTH_LONG).show();
            }

            }
        });

        btnGenerateQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                password = qrPassword.getText().toString().trim();
                System.out.println("SSID: " + ssid + " Pass: "+ password );
                if(password.length()> 7){

                    toBarcode = "WIFI:T:WPA;S:"+ssid+";P:"+password+";;";
                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                    try {
                        BitMatrix bitMatrix = multiFormatWriter.encode(toBarcode, BarcodeFormat.QR_CODE, 500, 500);
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

                        qrImage.setImageBitmap(bitmap);

                    } catch (WriterException e) {
                        e.printStackTrace();
                    }

                }
                else{
                    Toast.makeText(ConnectShareList.this, "Please enter a valid password", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnAddAndConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password = qrPassword.getText().toString().trim();
                if(password.length()> 7){
                wifiID = ssid;
                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                CollectionReference dbWifis = db.collection("wifis");
                WifiConfig wifiConfig = new WifiConfig();
                wifiConfig.setSsid(ssid);
                wifiConfig.setPassword(password);
                wifiConfig.setUserId(userID);
                wifiConfig.setType(type);

                if (0 == checkSaved) {

                dbWifis.add(wifiConfig)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(ConnectShareList.this, "Wifi Config added to Firestore", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ConnectShareList.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                } else {
                    Toast.makeText(ConnectShareList.this, "Wifi already in Database. You can update in the My Saved Networks section.", Toast.LENGTH_LONG).show();
                }

                conf.SSID = String.format("\"%s\"",ssid);
                conf.preSharedKey = String.format("\"%s\"",password);


                wifiManager.addNetwork(conf);
                wifiManager.setWifiEnabled(true);


                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();

                for( WifiConfiguration i : list ) {
                    if(i.SSID != null && i.SSID.equals(ssid)) {
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(i.networkId, true);
                        wifiManager.reconnect();
                        break;
                    }
                }

                listOfSavedWifis = wifiManager.getConfiguredNetworks();

                for (WifiConfiguration tmp : listOfSavedWifis) {
                    System.out.println("Saved Networks: " + tmp.SSID);
                }

                startActivity(new Intent(ConnectShareList.this, MySavedNetworks.class));
                }
                else{
                    Toast.makeText(ConnectShareList.this, "Please enter a valid password", Toast.LENGTH_LONG).show();
                }
            }
        });



    }

    int verifyWifi(final String ssid){

        wifiList = new ArrayList<>();

        db.collection("wifis").whereEqualTo("userId", userID).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData()+ " => " + wifiList.size() );
                                WifiConfig wf = document.toObject(WifiConfig.class);
                                wifiList.add(wf);

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        int i=1;
                        for (WifiConfig wf : wifiList) {
                            String tempSSID = wf.getSsid();
                            if (tempSSID.equals(ssid)) {
                                checkSaved = 1;
                                Toast.makeText(ConnectShareList.this, "Wifi already in Database..." + i, Toast.LENGTH_LONG).show();
                                i++;
                            }
                        }

                    }
                });




        return checkSaved;
    }


}
