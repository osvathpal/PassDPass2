package com.example.passdpass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MySavedNetworks extends AppCompatActivity {


    private FirebaseFirestore db;
    ArrayList<String> arrayList = new ArrayList<>();
    ListView listViewWifis;
    List<WifiConfig> wifiList;
    String intentData = "";
    String intentData2 = "";
    int intentData3 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_saved_networks);

        db = FirebaseFirestore.getInstance();

        listViewWifis = findViewById(R.id.mySavedNetworkListView);
        wifiList = new ArrayList<>();

        final WifiList adapter = new WifiList(MySavedNetworks.this, wifiList);

        db.collection("wifis").whereEqualTo("userId", "gA7D6lO9Q2WDycEZsqSTpfnQH0i1").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(!queryDocumentSnapshots.isEmpty()){
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for(DocumentSnapshot d: list){

                                WifiConfig wf = d.toObject(WifiConfig.class);
                                wifiList.add(wf);
                                adapter.notifyDataSetChanged();

                                System.out.println("My saved network: " + wf.getSsid());
                            }

                        }
                    }
                });


        listViewWifis.setAdapter(adapter);

        listViewWifis.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiConfig wifiConfig = wifiList.get(position);

                Intent intent = new Intent(MySavedNetworks.this, ConnectShareQR.class);
                intentData = wifiConfig.getSsid();
                intentData2 = wifiConfig.getPassword();
                intentData3 = wifiConfig.getType();

                startActivity(new Intent(MySavedNetworks.this, ConnectShareQR.class).putExtra("wifiSSID", intentData).putExtra("wifiPassword",intentData2).putExtra("type",intentData3));

            }
        });




    }
}
