package com.example.testingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AdditionalUserInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class FridgeActivity extends AppCompatActivity {

    private TextView tvFridgeNameBanner;

    private RecyclerView rvFreezer, rvMerelyExpiredItems;
    private ImageButton imgBtnBack, imgBtnAddParticipants, imgBtnClosePopout;
    private MerelyExpiredItemsAdapter merelyExpiredItemsAdapter;

    private FirebaseAuth mAuth;
    private String currentUserId, fridgeName, fridgeKey, createdBy;

    private Intent intentFromFridgeAdapter;

    // version 2
    private ImageButton imgBtnCreateFreezer, imgBtnCloseCreateContainerPopup;
    private ContainerAdapter containerAdapter;
    private Dialog containerDialog;
    private EditText edtContainerName;
    private Button btnConfirm;
    private String containerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fridge);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();


        intentFromFridgeAdapter = getIntent();
        fridgeName = intentFromFridgeAdapter.getStringExtra("fridgeName");
        fridgeKey = intentFromFridgeAdapter.getStringExtra("fridgeKey");
        createdBy = intentFromFridgeAdapter.getStringExtra("createdBy");
        tvFridgeNameBanner = findViewById(R.id.tvFridgeNameBanner);
        tvFridgeNameBanner.setText(fridgeName);

        imgBtnBack = findViewById(R.id.imgBtnBack);

        // while click on the tvFridgeNameBanner and close current activity
        imgBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // while click on add participants image button trigger the participants adding activity
        imgBtnAddParticipants = findViewById(R.id.imgBtnAddParticipants);
        imgBtnAddParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIsHost();
            }
        });

        rvFreezer = (RecyclerView) findViewById(R.id.rvFreezer);
        rvFreezer.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<Container> options =
                new FirebaseRecyclerOptions.Builder<Container>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("users").child(createdBy).child("fridges")
                .child(fridgeKey).child("freezers"), Container.class)
                .build();
        containerAdapter = new ContainerAdapter(fridgeKey, "freezers", createdBy, options);
        rvFreezer.setAdapter(containerAdapter);

        containerDialog = new Dialog(this);
        imgBtnCreateFreezer = (ImageButton) findViewById(R.id.imgBtnCreateFreezer);
        imgBtnCreateFreezer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateContainerPopUp();
            }
        });

        fridgeNavigation();
    }

    private void showCreateContainerPopUp() {
        containerDialog.setContentView(R.layout.createcontainerpopup);
        edtContainerName = (EditText) containerDialog.findViewById(R.id.edtContainerName);

        // if user confirm creation, further record would be created in database
        btnConfirm = (Button) containerDialog.findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                containerName = edtContainerName.getText().toString();
                if (!TextUtils.isEmpty(containerName)){
                    createContainerIntoDatabase();
                    containerDialog.dismiss();
                }else{
                    edtContainerName.setError("Please type in container name");
                    return;
                }
            }
        });

        imgBtnCloseCreateContainerPopup = (ImageButton) containerDialog.findViewById(R.id.imgBtnCloseCreateContainerPopOut);
        imgBtnCloseCreateContainerPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                containerDialog.dismiss();
            }
        });

        containerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        containerDialog.show();

    }

    private void createContainerIntoDatabase() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-YYYY");
        String containerCreatedDate =currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        String containerCreatedTime = currentTime.format(calForTime.getTime());

        HashMap containerMap = new HashMap();
        containerMap.put("containerName", containerName);
        containerMap.put("containerCreatedDate", containerCreatedDate);
        containerMap.put("createdBy", currentUserId);

        FirebaseDatabase.getInstance().getReference().child("users").child(createdBy).child("fridges").child(fridgeKey)
                .child("freezers").child(containerName+containerCreatedDate+containerCreatedTime).updateChildren(containerMap)
                .addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(FridgeActivity.this, containerName+ " created!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(FridgeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fridgeNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.fridge_navbar);
        bottomNavigationView.setSelectedItemId(R.id.freezer);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.freezer:
                    item.setChecked(true);
                    break;
                case R.id.drawer:
                    item.setChecked(true);
                    Intent intentToDrawerNav = new Intent(FridgeActivity.this, DrawerNavActivity.class);
                    intentToDrawerNav.putExtra("fridgeName",fridgeName);
                    intentToDrawerNav.putExtra("fridgeKey", fridgeKey);
                    intentToDrawerNav.putExtra("createdBy", createdBy);
                    startActivity(intentToDrawerNav);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    break;
                case R.id.shelf:
                    item.setChecked(true);
                    Intent intentToShelfNav = new Intent(FridgeActivity.this, ShelfNavActivity.class);
                    intentToShelfNav.putExtra("fridgeName",fridgeName);
                    intentToShelfNav.putExtra("fridgeKey", fridgeKey);
                    intentToShelfNav.putExtra("createdBy", createdBy);
                    startActivity(intentToShelfNav);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    break;
            }
            return false;
        });
    }

    private void checkIsHost() {
        if (createdBy.equals(currentUserId)){
            sendToAddParticipantsActivity();
        }else{
            Toast.makeText(this, "Only fridge host can invite other users.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendToAddParticipantsActivity() {
        Intent intent = new Intent(FridgeActivity.this, AddParticipantsActivity.class);
        intent.putExtra("fridgeKey", fridgeKey);
        startActivity(intent);
    }


    @Override
    protected void onStart() {
        super.onStart();
        containerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        containerAdapter.stopListening();
    }
}