package com.example.testingfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.FirebaseDatabase;

public class SearchFriendActivity extends AppCompatActivity {

    private TextInputEditText edt_searchUsername;
    private RecyclerView rvSearchFriendResult;
    private SearchView svSearchUsername;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);

        // search friends while user input username
        rvSearchFriendResult = (RecyclerView) findViewById(R.id.rvSearchFriendResult);
        rvSearchFriendResult.setLayoutManager(new LinearLayoutManager(this));
        svSearchUsername = (SearchView) findViewById(R.id.svSearchUsername);
        svSearchUsername.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchUser(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchUser(newText);
                return false;
            }
        });

        bottomNavigation();
    }

    private void searchUser(String query) {
        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                    .setQuery(FirebaseDatabase.getInstance().getReference().child("users")
                            .orderByChild("username").startAt(query).endAt(query + "~"), User.class)
                                .build();
        userAdapter = new UserAdapter(options);
        userAdapter.startListening();
        rvSearchFriendResult.setAdapter(userAdapter);


    }

    private void bottomNavigation() {

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_bottom);
        bottomNavigationView.setSelectedItemId(R.id.friendlist);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch(item.getItemId()){
                case R.id.home:
                    item.setChecked(true);
                    Intent intent1 = new Intent(SearchFriendActivity.this, HomeActivity.class);
                    startActivity(intent1);
                    overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                    finish();
                    break;
                case R.id.friendlist:
                    item.setCheckable(true);
                    break;
                case R.id.profile:
                    item.setChecked(true);
                    Intent intent3 = new Intent(SearchFriendActivity.this, ProfileActivity.class);
                    startActivity(intent3);
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    finish();
                    break;

            }
            return false;
        });

        BottomNavigationView friendListNavigationView = findViewById(R.id.friendlist_navbar);
        friendListNavigationView.setSelectedItemId(R.id.search_friend);
        friendListNavigationView.setOnItemSelectedListener(item -> {
            switch(item.getItemId()){
                case R.id.friend_list:
                    item.setChecked(true);
                    startActivity(new Intent(SearchFriendActivity.this, FriendListActivity.class));
                    //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                    break;
                case R.id.search_friend:
                    item.setChecked(true);
                    break;
                case R.id.friend_request:
                    item.setChecked(true);
                    startActivity(new Intent(SearchFriendActivity.this, FriendRequestActivity.class));
                    //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    break;
            }
            return false;
        });
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        userAdapter.startListening();
//    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        userAdapter.stopListening();
//    }
}