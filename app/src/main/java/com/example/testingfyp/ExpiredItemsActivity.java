package com.example.testingfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class ExpiredItemsActivity extends AppCompatActivity {

    private RecyclerView rvExpiredItems;
    private TextView tvAllExpiredItemsBanner;

    private FirebaseAuth mAuth;
    private String currentUserId;

    private Intent intentFromFridgeActivity;
    private ExpiredItemsAdapter expiredItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expired_items);

        intentFromFridgeActivity = getIntent();
        String fridgeKey = intentFromFridgeActivity.getStringExtra("fridgeKey");
        String createdBy = intentFromFridgeActivity.getStringExtra("createdBy");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        rvExpiredItems = (RecyclerView) findViewById(R.id.rvExpiredItems);
        rvExpiredItems.setLayoutManager(new LinearLayoutManager(this));

        // set up the recycler options for the expired items list
        FirebaseRecyclerOptions<Item> options =
                new FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("users").child(createdBy)
                                .child("fridges").child(fridgeKey).child("expiredItems").orderByChild("days"),Item.class).build();
        expiredItemAdapter = new ExpiredItemsAdapter(fridgeKey,createdBy, options);
        rvExpiredItems.setAdapter(expiredItemAdapter);

        tvAllExpiredItemsBanner = (TextView) findViewById(R.id.tvAllExpiredItemsBanner);
        // while click on the banner, close current activity and return back to previous activity
        tvAllExpiredItemsBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        expiredItemAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        expiredItemAdapter.stopListening();
    }
}