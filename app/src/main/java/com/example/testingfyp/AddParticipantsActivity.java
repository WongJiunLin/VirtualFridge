package com.example.testingfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class AddParticipantsActivity extends AppCompatActivity {

    private ImageButton imgBtnBack;
    private RecyclerView rvFriendList;
    private String currentUserID, fridgeKey;

    private AddParticipantsAdapter addParticipantsAdapter;
    private Intent intentFromFridge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_participants);

        intentFromFridge = getIntent();
        fridgeKey = intentFromFridge.getStringExtra("fridgeKey");

        currentUserID = FirebaseAuth.getInstance().getUid();

        rvFriendList = (RecyclerView) findViewById(R.id.rvFriendList);
        rvFriendList.setLayoutManager(new LinearLayoutManager(this));
        displayFriendList();

        // while click on the back button then close participants adding activity
        imgBtnBack = (ImageButton) findViewById(R.id.imgBtnBack);
        imgBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void displayFriendList() {
        FirebaseRecyclerOptions<Participant> options =
                new FirebaseRecyclerOptions.Builder<Participant>()
                    .setQuery(FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserID), Participant.class)
                    .build();
        addParticipantsAdapter = new AddParticipantsAdapter(fridgeKey, options);
        addParticipantsAdapter.startListening();
        rvFriendList.setAdapter(addParticipantsAdapter);
    }
}