package com.example.testingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private TextView tvRegisterAccount, tvAlertLoginError;
    private EditText edt_singInAccount, edt_signInPassword;
    private Button signInButton;

    private ProgressBar pbLogin;

    private FirebaseAuth mAuth;

    private String currentUID, tokenID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvRegisterAccount = (TextView) findViewById(R.id.tvRegisterAccount);
        edt_singInAccount = (EditText) findViewById(R.id.edt_signInAccount);
        edt_signInPassword = (EditText) findViewById(R.id.edt_signInPassword);
        signInButton = (Button) findViewById(R.id.signInButton);

        pbLogin = (ProgressBar) findViewById(R.id.pbLogin);
        tvAlertLoginError = (TextView) findViewById(R.id.tvAlertLoginError);

        mAuth = FirebaseAuth.getInstance();

        tvRegisterAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToRegisterActivity();
            }
        });
        
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountLogin();
            }
        });

    }

    private void accountLogin() {
        String signInAccount = edt_singInAccount.getText().toString();
        String signInPassword = edt_signInPassword.getText().toString();

        if (TextUtils.isEmpty(signInAccount)){
            edt_singInAccount.setError("Require field");
            return;
        }
        if (TextUtils.isEmpty(signInPassword)){
            edt_signInPassword.setError("Require field");
            return;
        }

        pbLogin.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(signInAccount,signInPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()){
                    tvAlertLoginError.setVisibility(View.INVISIBLE);
                    pbLogin.setVisibility(View.INVISIBLE);
                    // if successfully login, obtain and update the user device token id
                    currentUID = mAuth.getUid();
                    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
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