package com.example.testingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvFridgeList;
    private ImageButton btnAddFridge;
    private FridgeAdapter fridgeAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference fridgeRef;
    private String currentUserId;

    private List<String> fridgeKeys = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = (FirebaseAuth) FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        rvFridgeList = (RecyclerView) findViewById(R.id.rvFridgeList);
        rvFridgeList.setLayoutManager(new LinearLayoutManager(this));

        btnAddFridge = (ImageButton) findViewById(R.id.btnAddFridge);

        FirebaseRecyclerOptions<Fridge> options =
            new FirebaseRecyclerOptions.Builder<Fridge>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("fridges"),Fridge.class)
                .build();

        fridgeAdapter = new FridgeAdapter(options);
        rvFridgeList.setAdapter(fridgeAdapter);

        btnAddFridge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, AddFridgeActivity.class));
            }
        });

        fridgeRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("fridges");

        fridgeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> fridges = new ArrayList<String>();
                List<String> expiredItemCounts = new ArrayList<String>();
                for(DataSnapshot fridge: snapshot.getChildren()){
                    String fridgeName = fridge.child("fridgeName").getValue().toString();
                    long expiredItemCount = fridge.child("expiredItems").getChildrenCount();
                    if (expiredItemCount>0){
                        fridges.add(fridgeName);
                        expiredItemCounts.add(String.valueOf(expiredItemCount));
                    }
                }
                if (!expiredItemCounts.isEmpty()){
                    sendNotification(fridges, expiredItemCounts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        bottomNavigation();
    }

    public void sendNotification(List<String> fridges, List<String> expiredItemCounts){

        NotificationChannel channel = new NotificationChannel(
                "Expired Item Notification",
                "Expired Item Notification",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(HomeActivity.this, "Expired Item Notification");
        builder.setContentTitle("Vridgy");
        builder.setContentText("Expired Item Notification");
        builder.setSmallIcon(R.drawable.apple);
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        for (int i=0; i<fridges.size(); i++){
            style.addLine(fridges.get(i) + " is having " + expiredItemCounts.get(i) + " expired items.");
        }
        builder.setStyle(style);
        builder.setAutoCancel(true);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(HomeActivity.this);
        managerCompat.notify(1, builder.build());
    }

    private void bottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_bottom);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.home:
                    item.setChecked(true);
                    break;
                case R.id.friendlist:
                    item.setChecked(true);
                    startActivity(new Intent(HomeActivity.this, FriendListActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    break;
                case R.id.profile:
                    item.setChecked(true);
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    break;
            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        fridgeAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        fridgeAdapter.stopListening();
    }
}