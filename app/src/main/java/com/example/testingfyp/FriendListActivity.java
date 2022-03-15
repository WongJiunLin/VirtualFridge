package com.example.testingfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FriendListActivity extends AppCompatActivity {

    private RecyclerView rvFriendList;

    private FirebaseAuth mAuth;
    private String currentUserID;

    private FriendListAdapter friendListAdapter;
    private DatabaseReference friendsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getUid();

        rvFriendList = (RecyclerView) findViewById(R.id.rvFriendList);
        rvFriendList.setLayoutManager(new LinearLayoutManager(this));
        displayFriendList();


        bottomNavigation();
    }

    private void displayFriendList() {
        FirebaseRecyclerOptions<Friend> options =
                new FirebaseRecyclerOptions.Builder<Friend>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserID), Friend.class)
                        .build();
        friendListAdapter = new FriendListAdapter(options);
        friendListAdapter.startListening();
        rvFriendList.setAdapter(friendListAdapter);
    }


    private void bottomNavigation() {

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_bottom);
        bottomNavigationView.setSelectedItemId(R.id.friendlist);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch(item.getItemId()){
                case R.id.home:
                    item.setChecked(true);
                    Intent intent1 = new Intent(FriendListActivity.this, HomeActivity.class);
                    startActivity(intent1);
                    overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                    finish();
                    break;
                case R.id.friendlist:
                    item.setCheckable(true);
                    break;
                case R.id.profile:
                    item.setChecked(true);
                    Intent intent3 = new Intent(FriendListActivity.this, ProfileActivity.class);
                    startActivity(intent3);
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    finish();
                    break;

            }
            return false;
        });

        BottomNavigationView friendListNavigationView = findViewById(R.id.friendlist_navbar);
        friendListNavigationView.setSelectedItemId(R.id.friend_list);
        friendListNavigationView.setOnItemSelectedListener(item -> {
            switch(item.getItemId()){
                case R.id.friend_list:
                    item.setChecked(true);
                    break;
                case R.id.search_friend:
                    item.setChecked(true);
                    startActivity(new Intent(FriendListActivity.this, SearchFriendActivity.class));
                    finish();
                    break;
                case R.id.friend_request:
                    item.setChecked(true);
                    startActivity(new Intent(FriendListActivity.this, FriendRequestActivity.class));
                    finish();
                    break;
            }
            return false;
        });
    }

}