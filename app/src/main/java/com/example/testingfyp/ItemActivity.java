package com.example.testingfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

public class ItemActivity extends AppCompatActivity {

    private ImageButton imgBtnBack, imgBtnAddItem;
    private TextView tvContainerNameBanner;
    private RecyclerView rvItem;
    private Intent intentFromNav;
    private ItemAdapter itemAdapter;

    private String createdBy, fridgeKey, containerName, containerType, containerKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        intentFromNav = getIntent();
        createdBy = intentFromNav.getStringExtra("createdBy");
        fridgeKey = intentFromNav.getStringExtra("fridgeKey");
        containerName = intentFromNav.getStringExtra("containerName");
        containerType = intentFromNav.getStringExtra("containerType");
        containerKey = intentFromNav.getStringExtra("containerKey");

        tvContainerNameBanner = (TextView) findViewById(R.id.tvContainerNameBanner);
        tvContainerNameBanner.setText(containerName);

        imgBtnBack = (ImageButton) findViewById(R.id.imgBtnBack);
        imgBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        rvItem = (RecyclerView) findViewById(R.id.rvItem);
        rvItem.setLayoutManager(new GridLayoutManager(this, 3));
        FirebaseRecyclerOptions<Item> options = new FirebaseRecyclerOptions.Builder<Item>()
                .setQuery(FirebaseDatabase.getInstance().getReference()
                        .child("users").child(createdBy).child("fridges").child(fridgeKey)
                        .child(containerType).child(containerKey).child("items"), Item.class)
                .build();
        itemAdapter = new ItemAdapter(fridgeKey, containerType, containerKey, createdBy, options);
        rvItem.setAdapter(itemAdapter);

        imgBtnAddItem = (ImageButton) findViewById(R.id.imgBtnAddItem);
        imgBtnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ItemActivity.this, AddItemActivity.class);
                intent.putExtra("fridgeKey", fridgeKey);
                intent.putExtra("containerType",containerType);
                intent.putExtra("containerKey",containerKey);
                intent.putExtra("createdBy",createdBy);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        itemAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        itemAdapter.stopListening();
    }
}