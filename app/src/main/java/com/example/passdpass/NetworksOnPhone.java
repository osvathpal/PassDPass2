package com.example.passdpass;

import androidx.appcompat.app.AppCompatActivity;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NetworksOnPhone extends AppCompatActivity {


    private FirebaseFirestore db;
    private WifiManager wifiManager;
    private ListView listViewWifis;
    private List<WifiConfig> wifiList;
    private List<WifiConfiguration> savedWifisList;
    private Button addAllWifis;
    private WifiConfiguration conf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_networks_on_phone);
        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(WIFI_SERVICE);

        db = FirebaseFirestore.getInstance();

        listViewWifis = findViewById(R.id.wifisOnPhone);
        wifiList = new ArrayList<>();
        savedWifisList = new ArrayList<>();
        wifiManager.setWifiEnabled(true);
        savedWifisList = wifiManager.getConfiguredNetworks();
        addAllWifis = findViewById(R.id.addAllWifis);
        conf = new WifiConfiguration();

        Iterator<WifiConfiguration> it = savedWifisList.iterator();

        while (it.hasNext()) {
            WifiConfiguration tmp = it.next();
            WifiConfig wifiConfig = new WifiConfig();
            wifiConfig.setSsid(tmp.SSID);

            wifiList.add(wifiConfig);
            System.out.println("Available Networks: "+tmp.SSID);
        }

        WifiList adapter = new WifiList(NetworksOnPhone.this, wifiList);
        listViewWifis.setAdapter(adapter);

        addAllWifis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            getWifis();

            }
        });

    }

    void getWifis(){
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final WifiList adapter = new WifiList(NetworksOnPhone.this, wifiList);

        db.collection("wifis").whereEqualTo("userId", userID).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(!queryDocumentSnapshots.isEmpty()){
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for(DocumentSnapshot d: list){

                                WifiConfig wf = d.toObject(WifiConfig.class);
                                wf.setId(d.getId());
                                wf.setSsid(d.getString("ssid"));
                                wf.setPassword(d.getString("password"));

                                conf.SSID = String.format("\"%s\"", wf.getSsid());
                                conf.preSharedKey = String.format("\"%s\"", wf.getPassword());

                                wifiManager.addNetwork(conf);

                                wifiList.add(wf);

                                adapter.notifyDataSetChanged();

                                System.out.println("My saved network: " + wf.getSsid());
                            }

                        }
                        listViewWifis.setAdapter(adapter);
                    }
                });
    }

}
