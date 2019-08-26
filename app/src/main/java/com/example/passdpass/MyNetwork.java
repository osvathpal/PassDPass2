package com.example.passdpass;

import androidx.appcompat.app.AppCompatActivity;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MyNetwork extends AppCompatActivity {

    String ssid;
    String password;
    int type;
    String toBarcode;
    String toShare;
    ImageView qrImage;
    String wifiID;

    WifiConfiguration conf;
    WifiManager wifiManager;
    TextView qrSSID;
    EditText qrPassword;
    Button btnUpdateConnect;
    Button btnShare;
    Button btnGenerateQR;
    Button btnDelete;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_network);


        qrSSID = findViewById(R.id.edTxtSSID_MN);
        qrPassword = findViewById(R.id.edTxtPassword_MN);

        btnUpdateConnect =findViewById(R.id.update_and_connect_MN);
        btnShare = findViewById(R.id.btnShare_MN);
        btnGenerateQR = findViewById(R.id.btnGenerateQR_MN);
        btnDelete = findViewById(R.id.btnDelete);

        ssid = (getIntent().getStringExtra("wifiSSID"));
        password =(getIntent().getStringExtra("wifiPassword"));
        type = getIntent().getIntExtra("type", 0);
        qrSSID.setText(ssid);
        qrPassword.setText(password);



    }
}
