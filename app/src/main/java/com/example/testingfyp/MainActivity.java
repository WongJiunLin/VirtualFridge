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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private TextView tvRegisterAccount;
    private EditText edt_singInAccount, edt_signInPassword;
    private Button signInButton;

    private ProgressDialog loadingDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvRegisterAccount = (TextView) findViewById(R.id.tvRegisterAccount);
        edt_singInAccount = (EditText) findViewById(R.id.edt_signInAccount);
        edt_signInPassword = (EditText) findViewById(R.id.edt_signInPassword);
        signInButton = (Button) findViewById(R.id.signInButton);

        loadingDialog = new ProgressDialog(this);

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
        loadingDialog.setTitle("Performing user authentication");
        loadingDialog.setMessage("Please wait. System is validating your account.");
        loadingDialog.show();
        loadingDialog.setCanceledOnTouchOutside(true);

        mAuth.signInWithEmailAndPassword(signInAccount,signInPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()){
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    Toast.makeText(MainActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                }else{
                    String message = task.getException().getMessage();
                    Toast.makeText(MainActivity.this, "Error occurred: "+message, Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                }
            }
        });

    }

    private void sendToRegisterActivity() {
        startActivity(new Intent(MainActivity.this, RegisterActivity.class));
    }
}