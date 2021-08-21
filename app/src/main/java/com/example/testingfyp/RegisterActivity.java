package com.example.testingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtRegUsername, edtRegEmail, edtRegPassword, edtRegConfirmPassword;
    private Button btnRegisterAccInfo;
    private ImageButton imgBtnRegAccProfileImg;
    private ProgressBar pbRegAccInfo;
    private TextView tvAlertPickProfileImg, tvAlertAccountExisted, tvRegAccBanner;

    private FirebaseAuth mAuth;
    private DatabaseReference userProfileRef;
    private StorageReference userProfileImgRef;

    private static int Gallery_Pick = 1;

    private Uri profileImgUri;
    private String username, emailAcc, password, confirmPassword;
    private String currentUserId, saveCurrentDate, saveCurrentTime, downloadUri, profileImgName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtRegUsername = (EditText) findViewById(R.id.edtRegUsername);
        edtRegEmail = (EditText) findViewById(R.id.edtRegEmail);
        edtRegPassword = (EditText) findViewById(R.id.edtRegPassword);
        edtRegConfirmPassword = (EditText) findViewById(R.id.edtRegConfirmPassword);

        btnRegisterAccInfo = (Button) findViewById(R.id.btnRegisterAccInfo);
        imgBtnRegAccProfileImg = (ImageButton) findViewById(R.id.imgBtnRegAccProfileImg);

        tvAlertPickProfileImg = (TextView) findViewById(R.id.tvAlertPickProfileImg);
        tvAlertAccountExisted = (TextView) findViewById(R.id.tvAlertAccountExisted);

        pbRegAccInfo = (ProgressBar) findViewById(R.id.pbRegAcc);

        mAuth = FirebaseAuth.getInstance();

        // while user click on the image view redirect user to their local device image gallery
        imgBtnRegAccProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // while user click on the register button perform validation checking
        btnRegisterAccInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAccountInfo();
            }
        });

        tvRegAccBanner = (TextView) findViewById(R.id.tvRegAccBanner);
        tvRegAccBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void validateAccountInfo() {
        username = edtRegUsername.getText().toString();
        emailAcc = edtRegEmail.getText().toString();
        password = edtRegPassword.getText().toString();
        confirmPassword = edtRegConfirmPassword.getText().toString();

        // check if all the input values are valid or not
        if (TextUtils.isEmpty(username)){
            edtRegUsername.setError("Please type in username");
            return;
        }
        if (TextUtils.isEmpty(emailAcc)){
            edtRegEmail.setError("Please type in your email account");
            return;
        }
        if (TextUtils.isEmpty(password)){
            edtRegPassword.setError("Please type in your password");
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)){
            edtRegConfirmPassword.setError("Please type in your password again");
            return;
        }
        if (!confirmPassword.equals(password)){
            edtRegConfirmPassword.setError("Confirm password should be same as previous password");
            return;
        }
        if (imgBtnRegAccProfileImg.getDrawable()==null){
            tvAlertPickProfileImg.setVisibility(View.VISIBLE);
            return;
        }
        if (imgBtnRegAccProfileImg.getDrawable()!=null){
            tvAlertPickProfileImg.setVisibility(View.INVISIBLE);
        }

        // if valid check account existed or not
        tvAlertPickProfileImg.setVisibility(View.INVISIBLE);
        pbRegAccInfo.setVisibility(View.VISIBLE);
        checkAccountExistence();
    }

    // check account existence by using fetchSignInMethodsForEmail in firebase
    private void checkAccountExistence() {
        mAuth.fetchSignInMethodsForEmail(emailAcc).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()){
                    // find if user is new or not
                    boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                    if (isNewUser){
                        // if is new user register the user account and store respective info into the user profile
                        tvAlertAccountExisted.setVisibility(View.INVISIBLE);
                        registerAccount();
                    }else{
                        tvAlertAccountExisted.setVisibility(View.VISIBLE);
                        pbRegAccInfo.setVisibility(View.INVISIBLE);
                        return;
                    }
                }
            }
        });
    }

    // create user account
    private void registerAccount() {
        mAuth.createUserWithEmailAndPassword(emailAcc, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // once the account had registered successfully, get current user id to store user profile info into firebase
                currentUserId = mAuth.getUid();
                userProfileRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("user profile");
                userProfileImgRef = FirebaseStorage.getInstance().getReference().child("users").child(currentUserId).child("profile image");

                Toast.makeText(RegisterActivity.this, "Successfully created account", Toast.LENGTH_SHORT).show();
                insertProfileImageToFirebaseStorage();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "Error occurred: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                pbRegAccInfo.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void insertProfileImageToFirebaseStorage() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-YYYY");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calForTime.getTime());

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
                        Toast.makeText(RegisterActivity.this, "Image uploaded to firebase storage successfully", Toast.LENGTH_SHORT).show();
                        // after storing profile image, save user profile info into firebase database
                        saveProfileInfoToFirebase();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Error occurred "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "Error occurred "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileInfoToFirebase() {
        HashMap profileMap = new HashMap();
        profileMap.put("username", username);
        profileMap.put("email", emailAcc);
        profileMap.put("profile image",downloadUri);

        userProfileRef.updateChildren(profileMap).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                pbRegAccInfo.setVisibility(View.INVISIBLE);
                Toast.makeText(RegisterActivity.this, "Successfully updated personal info", Toast.LENGTH_SHORT).show();
                sendToLoginPage();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pbRegAccInfo.setVisibility(View.INVISIBLE);
                Toast.makeText(RegisterActivity.this, "Error occurred: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendToLoginPage() {
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
    }


    //open device image gallery
    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            profileImgUri = data.getData();
            imgBtnRegAccProfileImg.setImageURI(profileImgUri);
        }else{
            Toast.makeText(RegisterActivity.this, "Error occurred while picking image from local storage.", Toast.LENGTH_SHORT).show();
        }
    }


}