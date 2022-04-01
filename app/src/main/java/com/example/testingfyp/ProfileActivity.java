package com.example.testingfyp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView civProfileImg;
    private TextView tvProfileEmail, btnLogout, btnResetPassword, btnUpdateProfile;
    private EditText edtProfileUsername;

    private FirebaseAuth mAuth;
    private DatabaseReference userProfileRef;
    private StorageReference userProfileImgRef;

    private String currentUserId, username, email, updatedUsername, downloadUri, saveCurrentDate, saveCurrentTime, profileImgName;
    private Uri profileImgUri;

    private ProgressBar pbUpdateProfile;

    private static int Gallery_Pick = 1;
    private boolean profileImgChanged = false;

    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        civProfileImg = (CircleImageView) findViewById(R.id.civProfileImg);
        edtProfileUsername = (EditText) findViewById(R.id.edtProfileUsername);
        tvProfileEmail = (TextView) findViewById(R.id.tvProfileEmail);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        userProfileImgRef = FirebaseStorage.getInstance().getReference().child("users").child(currentUserId).child("profile image");
        userProfileRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        userProfileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // retrieve data from firebase and display on respective field
                username = snapshot.child("username").getValue().toString();
                edtProfileUsername.setText(username);
                email = snapshot.child("email").getValue().toString();
                tvProfileEmail.setText(email);
                profileImgUri = Uri.parse(snapshot.child("profileImgUri").getValue().toString());
                Picasso.get().load(profileImgUri).into(civProfileImg);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        btnResetPassword = (TextView) findViewById(R.id.btnResetPassword);
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResetPasswordEmail();
                AlertDialog resetPasswordDialog = new AlertDialog.Builder(view.getContext()).setTitle("Reset Password")
                        .setMessage("Reset password link has been sent to your email")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                return;
                            }
                        }).show();
            }
        });

        btnLogout = (TextView) findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToMainActivity();
            }
        });

        // while user click on profile image, redirect user to local gallery to pick profile image
        civProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        // while user click on update profile, update their status
        pbUpdateProfile = (ProgressBar) findViewById(R.id.pbUpdateProfile);
        btnUpdateProfile = (TextView) findViewById(R.id.btnUpdateProfile);
        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateUpdateProfileInfo();
            }
        });



        bottomNavigation();

    }

    private void validateUpdateProfileInfo() {
        updatedUsername = edtProfileUsername.getText().toString();
        if (TextUtils.isEmpty(updatedUsername)){
            edtProfileUsername.setText("Please type in your username");
            return;
        }
        if (profileImgChanged == false){
            downloadUri = profileImgUri.toString();
            updatePersonalInfoToFirebase();
        }else {
            updateProfileImgToFirebaseStorage();
        }
    }

    private void updateProfileImgToFirebaseStorage() {
        //generate img file name using current user id
        profileImgName = currentUserId + " profile image";
        StorageReference imgPath = userProfileImgRef.child(profileImgName);

        imgPath.putFile(profileImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        downloadUri = uri.toString();
                        Toast.makeText(ProfileActivity.this, "Image uploaded to Firebase Storage successfully.", Toast.LENGTH_SHORT).show();
                        updatePersonalInfoToFirebase();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updatePersonalInfoToFirebase() {
        pbUpdateProfile.setVisibility(View.VISIBLE);
        HashMap updateProfileMap = new HashMap();
        updateProfileMap.put("username", updatedUsername);
        updateProfileMap.put("profileImgUri", downloadUri);
        userProfileRef.updateChildren(updateProfileMap).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                pbUpdateProfile.setVisibility(View.INVISIBLE);
                Toast.makeText(ProfileActivity.this, "Successfully updated profile.", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            profileImgUri = data.getData();
            civProfileImg.setImageURI(profileImgUri);
            // set the boolean flag to true
            profileImgChanged = true;
        }else{
            Toast.makeText(ProfileActivity.this, "Error occurred while picking image from local storage.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendResetPasswordEmail() {
        mAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ProfileActivity.this, "Reset link has been sent to your email.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendToMainActivity() {
        mAuth.signOut();
        finish();
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