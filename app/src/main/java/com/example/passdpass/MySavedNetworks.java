package com.example.passdpass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

    public class MySavedNetworks extends AppCompatActivity {

    private List<WifiConfig> wifiList;
    private String intentData = "";
    private String intentData2 = "";
    private int intentData3 = 0;
    private String intentData4 = "";
    private String intentData5 = "";


        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_saved_networks);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String curentemail = user.getEmail();
        TextView email_display = findViewById(R.id.emailID_UA);
        email_display.setText(curentemail);
        Button btnLogOut = findViewById(R.id.btnLogOut);
            Button wifisOnPhone = findViewById(R.id.wifisOnPhone);


        ListView listViewWifis = findViewById(R.id.mySavedNetworkListView);
        wifiList = new ArrayList<>();

        final WifiList adapter = new WifiList(MySavedNetworks.this, wifiList);

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MySavedNetworks.this, ActivityLogin.class);
                startActivity(intent);

            }
        });



        wifisOnPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MySavedNetworks.this, NetworksOnPhone.class));
            }
        });


        db.collection("wifis").whereEqualTo("userId", userID).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(!queryDocumentSnapshots.isEmpty()){
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for(DocumentSnapshot d: list){

                                WifiConfig wf = d.toObject(WifiConfig.class);
                                wf.setId(d.getId());
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


                intentData = wifiConfig.getSsid();
                intentData2 = wifiConfig.getPassword();
                intentData3 = wifiConfig.getType();
                intentData4 = wifiConfig.getUserId();

                intentData5 = wifiConfig.getId();

                startActivity(new Intent(MySavedNetworks.this, MyNetwork.class).putExtra("wifiSSID", intentData).putExtra("wifiPassword",intentData2).putExtra("type",intentData3).putExtra("userId", intentData4).putExtra("wifiID", intentData5));

            }
        });




    }
}
