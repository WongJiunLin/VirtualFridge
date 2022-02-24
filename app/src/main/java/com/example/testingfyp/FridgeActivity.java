package com.example.testingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.AdditionalUserInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FridgeActivity extends AppCompatActivity {

    private TextView tvFridgeNameBanner;

    private RecyclerView rvFreezer, rvShelf, rvDrawer;
    private ImageButton btnFreezerAdd, btnShelfAdd, btnDrawerAdd, imgBtnBack, imgBtnAddParticipants;
    private ItemAdapter itemAdapterForFreezer, itemAdapterForShelf, itemAdapterForDrawer;

    private Button  btnCheckExpiredItems;

    private FirebaseAuth mAuth;
    private String currentUserId;

    private Intent intentFromFridgeAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fridge);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        // recycler view for freezer area
        rvFreezer = findViewById(R.id.rvFreezer);
        rvFreezer.setLayoutManager(new LinearLayoutManager(this));

        // recycler view for shelf area
        rvShelf = findViewById(R.id.rvShelf);
        rvShelf.setLayoutManager(new LinearLayoutManager(this));

        // recycler view for drawer area
        rvDrawer = findViewById(R.id.rvDrawer);
        rvDrawer.setLayoutManager(new LinearLayoutManager(this));

        intentFromFridgeAdapter = getIntent();
        String fridgeName = intentFromFridgeAdapter.getStringExtra("fridgeName");
        String fridgeKey = intentFromFridgeAdapter.getStringExtra("fridgeKey");
        String createdBy = intentFromFridgeAdapter.getStringExtra("createdBy");
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
                checkIsHost(fridgeKey);
            }
        });

        // set up the recycler options for the freezer
        FirebaseRecyclerOptions<Item> freezerOptions =
                new FirebaseRecyclerOptions.Builder<Item>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("users").child(createdBy)
                        .child("fridges").child(fridgeKey).child("freezer").child("items").orderByChild("days"),Item.class).build();
        itemAdapterForFreezer = new ItemAdapter(fridgeKey,"freezer", createdBy, freezerOptions);
        rvFreezer.setAdapter(itemAdapterForFreezer);

        // set up the recycler options for the shelf
        FirebaseRecyclerOptions<Item> shelfOptions =
                new FirebaseRecyclerOptions.Builder<Item>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("users").child(createdBy)
                        .child("fridges").child(fridgeKey).child("shelf").child("items").orderByChild("days"),Item.class).build();
        itemAdapterForShelf = new ItemAdapter(fridgeKey,"shelf", createdBy, shelfOptions);
        rvShelf.setAdapter(itemAdapterForShelf);

        // set up the recycler options for the drawer
        FirebaseRecyclerOptions<Item> drawerOptions =
                new FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("users").child(createdBy)
                                .child("fridges").child(fridgeKey).child("drawer").child("items").orderByChild("days"),Item.class).build();
        itemAdapterForDrawer = new ItemAdapter(fridgeKey,"drawer", createdBy, drawerOptions);
        rvDrawer.setAdapter(itemAdapterForDrawer);

        //actions occurred when click on different add button at different container
        //while press on add button in freezer, item added in next step would be stored in freezer
        btnFreezerAdd = findViewById(R.id.btnAddFreezer);
        btnFreezerAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentAddItemToFreezer = new Intent(FridgeActivity.this, AddItemActivity.class);
                intentAddItemToFreezer.putExtra("fridgeKey", fridgeKey);
                intentAddItemToFreezer.putExtra("containerType","freezer");
                intentAddItemToFreezer.putExtra("createdBy", createdBy);
                startActivity(intentAddItemToFreezer);
            }
        });

        //while press on add button in shelf, item added in next step would be stored in shelf
        btnShelfAdd = findViewById(R.id.btnAddShelf);
        btnShelfAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentAddItemToShelf = new Intent(FridgeActivity.this, AddItemActivity.class);
                intentAddItemToShelf.putExtra("fridgeKey", fridgeKey);
                intentAddItemToShelf.putExtra("containerType","shelf");
                intentAddItemToShelf.putExtra("createdBy", createdBy);
                startActivity(intentAddItemToShelf);
            }
        });

        //while press on add button in drawer, item added in next step would be stored in shelf
        btnDrawerAdd = findViewById(R.id.btnAddDrawer);
        btnDrawerAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentAddItemToDrawer = new Intent(FridgeActivity.this, AddItemActivity.class);
                intentAddItemToDrawer.putExtra("fridgeKey", fridgeKey);
                intentAddItemToDrawer.putExtra("containerType","drawer");
                intentAddItemToDrawer.putExtra("createdBy", createdBy);
                startActivity(intentAddItemToDrawer);
            }
        });

        btnCheckExpiredItems = (Button) findViewById(R.id.btnCheckExpiredItems);
        btnCheckExpiredItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToExpiredItems = new Intent(FridgeActivity.this, ExpiredItemsActivity.class);
                intentToExpiredItems.putExtra("fridgeKey",fridgeKey);
                intentToExpiredItems.putExtra("createdBy", createdBy);
                startActivity(intentToExpiredItems);
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
                                Toast.makeText(FridgeActivity.this, "Only fridge host can invite other users.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendToAddParticipantsActivity(String fridgeKey) {
        Intent intent = new Intent(FridgeActivity.this, AddParticipantsActivity.class);
        intent.putExtra("fridgeKey", fridgeKey);
        startActivity(intent);
    }


    @Override
    protected void onStart() {
        super.onStart();
        itemAdapterForFreezer.startListening();
        itemAdapterForShelf.startListening();
        itemAdapterForDrawer.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        itemAdapterForFreezer.stopListening();
        itemAdapterForShelf.stopListening();
        itemAdapterForDrawer.stopListening();
    }
}