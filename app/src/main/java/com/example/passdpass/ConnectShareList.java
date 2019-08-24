package com.example.passdpass;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.List;

public class ConnectShareList extends AppCompatActivity {
    String ssid;
    String password;
    int type;
    String toBarcode;
    ImageView qrImage;
    String wifiID;

    WifiConfiguration conf;
    WifiManager wifiManager;
    EditText qrSSID;
    EditText qrPassword;
    Button btnAddAndConnect;
    List<WifiConfiguration> listOfSavedWifis;

    FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_share_list);

        db = FirebaseFirestore.getInstance();

        qrSSID = findViewById(R.id.edTxtSSID_list);
        qrPassword = findViewById(R.id.edTxtPassword_list);
        btnAddAndConnect = findViewById(R.id.add_and_connect_list);
        qrImage = findViewById(R.id.QR_Image_list);

        ssid = (getIntent().getStringExtra("ssid"));

        qrSSID.setText(ssid);

        conf = new WifiConfiguration();
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);

        password = qrPassword.getText().toString().trim();

        btnAddAndConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(password !=  null){
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
            }
        });



    }
}
