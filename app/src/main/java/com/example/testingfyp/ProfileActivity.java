package com.example.testingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView civProfileImg;
    private TextView tvProfileUsername, tvProfileEmail, btnLogout;

    private FirebaseAuth mAuth;
    private DatabaseReference userProfileRef;

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        civProfileImg = (CircleImageView) findViewById(R.id.civProfileImg);
        tvProfileUsername = (TextView) findViewById(R.id.tvProfileUsername);
        tvProfileEmail = (TextView) findViewById(R.id.tvProfileEmail);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        //userProfileRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("user profile");
        userProfileRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        userProfileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // retrieve data from firebase and display on respective field
                String username = snapshot.child("username").getValue().toString();
                tvProfileUsername.setText(username);
                String email = snapshot.child("email").getValue().toString();
                tvProfileEmail.setText(email);
                Uri profileImgUri = Uri.parse(snapshot.child("profileImgUri").getValue().toString());
                Picasso.get().load(profileImgUri).into(civProfileImg);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout = (TextView) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToMainActivity();
            }
        });

        bottomNavigation();

    }

    private void sendToMainActivity() {
        mAuth.signOut();
        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
    }

    private void bottomNavigation() {

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_bottom);
        bottomNavigationView.setSelectedItemId(R.id.profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.home:
                    item.setChecked(true);
                    startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                    break;
                case R.id.shoppinglist:
                    item.setChecked(true);
                    startActivity(new Intent(ProfileActivity.this, MessageTesting.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                    break;
                case R.id.friendlist:
                    item.setChecked(true);
                    startActivity(new Intent(ProfileActivity.this, FriendListActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                    break;
                case R.id.profile:
                    item.setChecked(true);
                    break;
            }
            return false;
        });

    }

}