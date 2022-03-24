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

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ShelfNavActivity extends AppCompatActivity {

    private TextView tvFridgeNameBanner;

    private RecyclerView rvShelf, rvMerelyExpiredItems;
    private ImageButton imgBtnBack, imgBtnAddParticipants, imgBtnClosePopout;
    private MerelyExpiredItemsAdapter merelyExpiredItemsAdapter;

    private Button btnCheckExpiredItems;

    private FirebaseAuth mAuth;
    private String currentUserId, fridgeName, fridgeKey, createdBy;

    private Intent intentFromOthers;

    private ImageButton imgBtnCreateShelf, imgBtnCloseCreateContainerPopup;
    private ContainerAdapter containerAdapter;
    private Dialog containerDialog;
    private EditText edtContainerName;
    private Button btnConfirm;
    private String containerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelf_nav);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        intentFromOthers = getIntent();
        fridgeKey = intentFromOthers.getStringExtra("fridgeKey");
        fridgeName = intentFromOthers.getStringExtra("fridgeName");
        createdBy = intentFromOthers.getStringExtra("createdBy");

        tvFridgeNameBanner = (TextView) findViewById(R.id.tvFridgeNameBanner);
        tvFridgeNameBanner.setText(fridgeName);

        imgBtnBack = (ImageButton) findViewById(R.id.imgBtnBack);
        imgBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        imgBtnAddParticipants = (ImageButton) findViewById(R.id.imgBtnAddParticipants);
        imgBtnAddParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkIsHost(fridgeKey);
            }
        });

        rvShelf = (RecyclerView) findViewById(R.id.rvShelf);
        rvShelf.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<Container> options =
                new FirebaseRecyclerOptions.Builder<Container>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("users").child(createdBy).child("fridges")
                                .child(fridgeKey).child("shelves"), Container.class)
                        .build();
        containerAdapter = new ContainerAdapter(fridgeKey, "shelves", createdBy, options);
        rvShelf.setAdapter(containerAdapter);

        containerDialog = new Dialog(this);
        imgBtnCreateShelf = (ImageButton) findViewById(R.id.imgBtnCreateShelf);
        imgBtnCreateShelf.setOnClickListener(new View.OnClickListener() {
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
                .child("shelves").child(containerName+containerCreatedDate+containerCreatedTime).updateChildren(containerMap)
                .addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(ShelfNavActivity.this, containerName+ " created!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ShelfNavActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIsHost(String fridgeKey) {
        FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("fridges")
                .child(fridgeKey).child("participants").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String role = snapshot.child("role").getValue().toString();
                            if (role.equals("host")){
                                sendToAddParticipantsActivity(fridgeKey);
                            }
                            else{
                                Toast.makeText(ShelfNavActivity.this, "Only fridge host can invite other users.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendToAddParticipantsActivity(String fridgeKey) {
        Intent intent = new Intent(ShelfNavActivity.this, AddParticipantsActivity.class);
        intent.putExtra("fridgeKey", fridgeKey);
        startActivity(intent);
    }

    private void fridgeNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.fridge_navbar);
        bottomNavigationView.setSelectedItemId(R.id.shelf);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.freezer:
                    item.setChecked(true);
                    Intent intentToFreezer = new Intent(ShelfNavActivity.this, FridgeActivity.class);
                    intentToFreezer.putExtra("fridgeName",fridgeName);
                    intentToFreezer.putExtra("fridgeKey", fridgeKey);
                    intentToFreezer.putExtra("createdBy", createdBy);
                    startActivity(intentToFreezer);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                    break;
                case R.id.shelf:
                    item.setChecked(true);
                    break;
                case R.id.drawer:
                    item.setChecked(true);
                    Intent intentToShelf = new Intent(ShelfNavActivity.this, DrawerNavActivity.class);
                    intentToShelf.putExtra("fridgeName",fridgeName);
                    intentToShelf.putExtra("fridgeKey", fridgeKey);
                    intentToShelf.putExtra("createdBy", createdBy);
                    startActivity(intentToShelf);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    break;
            }
            return false;
        });
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