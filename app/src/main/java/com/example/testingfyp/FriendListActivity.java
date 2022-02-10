package com.example.testingfyp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FriendListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        bottomNavigation();
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
                case R.id.shoppinglist:
                    item.setChecked(true);
                    Intent intent2 = new Intent(FriendListActivity.this, HomeActivity.class);
                    startActivity(intent2);
                    overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                    finish();
                case R.id.friendlist:
                    item.setCheckable(true);
                    break;
                case R.id.profile:
                    item.setChecked(true);
                    Intent intent3 = new Intent(FriendListActivity.this, ProfileActivity.class);
                    startActivity(intent3);
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    finish();

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
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                case R.id.friend_request:
                    item.setChecked(true);
                    startActivity(new Intent(FriendListActivity.this, SearchFriendActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
            }
            return false;
        });
    }

}