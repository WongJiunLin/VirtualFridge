package com.example.testingfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvFridgeList;
    private FloatingActionButton btnAddFridge;
    private FridgeAdapter fridgeAdapter;

    private FirebaseAuth mAuth;
    private String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = (FirebaseAuth) FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        rvFridgeList = (RecyclerView) findViewById(R.id.rvFridgeList);
        rvFridgeList.setLayoutManager(new LinearLayoutManager(this));

        btnAddFridge = (FloatingActionButton) findViewById(R.id.btnAddFridge);

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