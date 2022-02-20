package com.example.testingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class FriendRequestActivity extends AppCompatActivity {

    private RecyclerView rvFriendRequest;

    private FirebaseAuth mAuth;
    private String currentUserID;

    private FriendRequestAdapter friendRequestAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getUid();

        rvFriendRequest = findViewById(R.id.rvFriendRequest);
        rvFriendRequest.setLayoutManager(new LinearLayoutManager(this));
        displayFriendRequests();

        bottomNavigation();
    }

    private void displayFriendRequests() {
        FirebaseRecyclerOptions<FriendRequest> options =
                new FirebaseRecyclerOptions.Builder<FriendRequest>()
                    .setQuery(FirebaseDatabase.getInstance().getReference().child("friend requests").child(currentUserID)
                            .orderByChild("request_type").startAt("received").endAt("received"+"~"), FriendRequest.class)
                                .build();
        friendRequestAdapter = new FriendRequestAdapter(options);
        friendRequestAdapter.startListening();
        rvFriendRequest.setAdapter(friendRequestAdapter);
    }

    private void bottomNavigation() {

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_bottom);
        bottomNavigationView.setSelectedItemId(R.id.friendlist);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    item.setChecked(true);
                    Intent intent1 = new Intent(FriendRequestActivity.this, HomeActivity.class);
                    startActivity(intent1);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                    break;
                case R.id.shoppinglist:
                    item.setChecked(true);
                    Intent intent2 = new Intent(FriendRequestActivity.this, HomeActivity.class);
                    startActivity(intent2);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                    break;
                case R.id.friendlist:
                    item.setCheckable(true);
                    break;
                case R.id.profile:
                    item.setChecked(true);
                    Intent intent3 = new Intent(FriendRequestActivity.this, ProfileActivity.class);
                    startActivity(intent3);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    break;

            }
            return false;
        });

        BottomNavigationView friendListNavigationView = findViewById(R.id.friendlist_navbar);
        friendListNavigationView.setSelectedItemId(R.id.friend_request);
        friendListNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.friend_list:
                    item.setChecked(true);
                    startActivity(new Intent(FriendRequestActivity.this, FriendListActivity.class));
                    finish();
                    break;
                case R.id.search_friend:
                    item.setChecked(true);
                    startActivity(new Intent(FriendRequestActivity.this, SearchFriendActivity.class));
                    finish();
                    break;
                case R.id.friend_request:
                    item.setChecked(true);
                    break;
            }
            return false;
        });
    }
}