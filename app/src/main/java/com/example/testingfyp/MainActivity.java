package com.example.testingfyp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.w3c.dom.Text;

import java.util.HashMap;

import at.markushi.ui.CircleButton;

public class MainActivity extends AppCompatActivity {

    private TextView tvRegisterAccount, tvAlertLoginError, tvForgetPassword;
    private TextInputEditText edt_signInAccount, edt_signInPassword;
    private Button signInButton;
    private ProgressBar pbLogin;
    private CircleButton imgBtnGoogleSignIn;

    private FirebaseAuth mAuth;
    private String currentUID, tokenID, email, googleId;

    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount account;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edt_signInAccount = (TextInputEditText) findViewById(R.id.edt_signInAccount);
        edt_signInPassword = (TextInputEditText) findViewById(R.id.edt_signInPassword);

        pbLogin = (ProgressBar) findViewById(R.id.pbLogin);
        tvAlertLoginError = (TextView) findViewById(R.id.tvAlertLoginError);

        mAuth = FirebaseAuth.getInstance();
        // obtain the Register Account TextView
        tvRegisterAccount = (TextView) findViewById(R.id.tvRegisterAccount);
        tvRegisterAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToRegisterActivity();
            }
        });

        // while user press on login button trigger accountLogin function
        signInButton = (Button) findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountLogin();
            }
        });

        // while user click on forget password, trigger reset password function
        tvForgetPassword = (TextView) findViewById(R.id.tvForgotPassword);
        tvForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = edt_signInAccount.getText().toString();
                if (TextUtils.isEmpty(email)){
                    edt_signInAccount.setError("Please type in your email before changing password");
                }else{
                    sendResetPasswordEmail();
                }

            }
        });

        // if user click google icon, then proceed google login
        requestGoogleSignIn();
        imgBtnGoogleSignIn = (CircleButton) findViewById(R.id.imgBtnGoogleSignIn);
        imgBtnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });

    }

    private void requestGoogleSignIn(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("918869352937-aftoua2tfe8baperglpou5cunhqdhvh5.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInWithGoogle(){
        pbLogin.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1000){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "something goes wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            googleId = mAuth.getUid();
                            String username = account.getDisplayName();
                            String email = account.getEmail();
                            String profileImgUri = account.getPhotoUrl().toString();
                            HashMap userMap = new HashMap();
                            userMap.put("username", username);
                            userMap.put("email", email);
                            userMap.put("profileImgUri", profileImgUri);
                            FirebaseDatabase.getInstance().getReference().child("users")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.hasChild(googleId)){
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("users").child(googleId).updateChildren(userMap);
                                    }else{
                                        return;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(new OnCompleteListener<String>() {
                                        @Override
                                        public void onComplete(@NonNull Task<String> task) {
                                            if (task.isSuccessful()){
                                                tokenID = task.getResult();
                                                FirebaseDatabase.getInstance().getReference().child("users").child(googleId)
                                                        .child("tokenID").setValue(tokenID);
                                            }
                                        }
                                    });
                            pbLogin.setVisibility(View.INVISIBLE);
                            Toast.makeText(MainActivity.this, "Successfully login using Google Account", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        }else{
                            Toast.makeText(MainActivity.this, "Failed login using Google Account", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void sendResetPasswordEmail() {
        mAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(MainActivity.this, "Reset link has been sent to your email.",
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void accountLogin() {
        String signInAccount = edt_signInAccount.getText().toString();
        String signInPassword = edt_signInPassword.getText().toString();

        if (TextUtils.isEmpty(signInAccount)){
            edt_signInAccount.setError("Require field");
            return;
        }
        if (TextUtils.isEmpty(signInPassword)){
            edt_signInPassword.setError("Require field");
            return;
        }

        pbLogin.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(signInAccount,signInPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()){
                    tvAlertLoginError.setVisibility(View.INVISIBLE);
                    pbLogin.setVisibility(View.INVISIBLE);
                    // if successfully login, obtain and update the user device token id
                    currentUID = mAuth.getUid();
                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (task.isSuccessful()){
                                tokenID = task.getResult();
                                FirebaseDatabase.getInstance().getReference().child("users").child(currentUID)
                                        .child("tokenID").setValue(tokenID);
                            }
                        }
                    });
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    Toast.makeText(MainActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();

                }else{
                    pbLogin.setVisibility(View.INVISIBLE);
                    tvAlertLoginError.setVisibility(View.VISIBLE);
                    String message = task.getException().getMessage();
                    Toast.makeText(MainActivity.this, "Error occurred: "+message, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void sendToRegisterActivity() {
        startActivity(new Intent(MainActivity.this, RegisterActivity.class));
    }
}