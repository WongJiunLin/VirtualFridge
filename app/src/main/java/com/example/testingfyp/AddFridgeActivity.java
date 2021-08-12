package com.example.testingfyp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class AddFridgeActivity extends AppCompatActivity {

    private EditText edtFridgeName;
    private Button btnCreateFridge;

    private FirebaseAuth mAuth;
    private DatabaseReference fridgeRef;

    private String fridgeName, currentUserId;
    private String fridgeCreatedDate, fridgeCreatedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fridge);

        edtFridgeName = (EditText) findViewById(R.id.edtFridgeName);
        btnCreateFridge = (Button) findViewById(R.id.btnCreateFridge);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        fridgeRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("fridges");

        btnCreateFridge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the fridge name from the edit text
                fridgeName = edtFridgeName.getText().toString();
                if (TextUtils.isEmpty(fridgeName)){
                    edtFridgeName.setError("Required field");
                    return;
                }
                insertFridge();
            }
        });
    }

    private void insertFridge() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-YYYY");
        fridgeCreatedDate =currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        fridgeCreatedTime = currentTime.format(calForTime.getTime());

        HashMap fridgeMap = new HashMap();
        fridgeMap.put("fridgeName",fridgeName);
        fridgeMap.put("fridgeCreatedDate",fridgeCreatedDate);

        fridgeRef.child(fridgeName+fridgeCreatedDate+fridgeCreatedTime).updateChildren(fridgeMap).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(AddFridgeActivity.this, "New Fridge has been created", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddFridgeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}