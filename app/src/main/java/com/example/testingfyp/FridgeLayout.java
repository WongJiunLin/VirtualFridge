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
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FridgeLayout extends AppCompatActivity {

    ImageButton imgBtnBack, imgBtnAddParticipants, imgBtnClosePopout;
    TextView tvFridgeNameBanner, freezerTop, shelfMiddle, drawerBottom, tvPopUpBanner;
    LinearLayout llExpiredItemsDialog;
    RecyclerView rvMerelyExpiredItems, rvExpiredItems;
    MerelyExpiredItemsAdapter merelyExpiredItemsAdapter, expiredItemsAdapter;

    Intent intentFromFridgeAdapter;
    String fridgeName, fridgeKey, createdBy, currentUserID;

    private int colorAlert, colorBlack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fridge_layout);

        currentUserID = FirebaseAuth.getInstance().getUid();

        intentFromFridgeAdapter = getIntent();
        fridgeName = intentFromFridgeAdapter.getStringExtra("fridgeName");
        fridgeKey = intentFromFridgeAdapter.getStringExtra("fridgeKey");
        createdBy = intentFromFridgeAdapter.getStringExtra("createdBy");

        imgBtnBack = (ImageButton) findViewById(R.id.imgBtnBack);
        imgBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tvFridgeNameBanner = (TextView) findViewById(R.id.tvFridgeNameBanner);
        tvFridgeNameBanner.setText(fridgeName);

        freezerTop = (TextView) findViewById(R.id.freezerTop);
        freezerTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToFreezer = new Intent(FridgeLayout.this, FridgeActivity.class);
                intentToFreezer.putExtra("fridgeName", fridgeName);
                intentToFreezer.putExtra("fridgeKey", fridgeKey);
                intentToFreezer.putExtra("createdBy", createdBy);
                startActivity(intentToFreezer);
            }
        });

        shelfMiddle = (TextView) findViewById(R.id.shelfMiddle);
        shelfMiddle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToShelf = new Intent(FridgeLayout.this, ShelfNavActivity.class);
                intentToShelf.putExtra("fridgeName", fridgeName);
                intentToShelf.putExtra("fridgeKey", fridgeKey);
                intentToShelf.putExtra("createdBy", createdBy);
                startActivity(intentToShelf);
            }
        });

        drawerBottom = (TextView) findViewById(R.id.drawerBottom);
        drawerBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToDrawer = new Intent(FridgeLayout.this, DrawerNavActivity.class);
                intentToDrawer.putExtra("fridgeName", fridgeName);
                intentToDrawer.putExtra("fridgeKey", fridgeKey);
                intentToDrawer.putExtra("createdBy", createdBy);
                startActivity(intentToDrawer);
            }
        });

        imgBtnAddParticipants = (ImageButton) findViewById(R.id.imgBtnAddParticipants);
        imgBtnAddParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkIsHost();
            }
        });

        // show all expired items info if existed
        FirebaseDatabase.getInstance().getReference().child("users").child(createdBy)
                .child("fridges").child(fridgeKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("expiredItems")){
                    showExpiredItemsPopUp();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // show all the merely expired items info if existed
        FirebaseDatabase.getInstance().getReference().child("users").child(createdBy)
                .child("fridges").child(fridgeKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("merelyExpiredItems")){
                    showMerelyExpiredItemsPopUp();
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void showExpiredItemsPopUp() {
        Dialog expiredItemDialog = new Dialog(this);
        expiredItemDialog.setContentView(R.layout.merelyexpiredpopout);

        tvPopUpBanner = expiredItemDialog.findViewById(R.id.tvPopUpBanner);
        tvPopUpBanner.setText("Expired Items");
        colorBlack = tvPopUpBanner.getResources().getColor(R.color.black);
        tvPopUpBanner.setTextColor(colorBlack);

        llExpiredItemsDialog = expiredItemDialog.findViewById(R.id.llExpiredItemsDialog);
        colorAlert = llExpiredItemsDialog.getResources().getColor(R.color.nearlyExpired);
        llExpiredItemsDialog.setBackgroundColor(colorAlert);

        imgBtnClosePopout = expiredItemDialog.findViewById(R.id.imgBtnClosePopout);
        rvExpiredItems = expiredItemDialog.findViewById(R.id.rvMerelyExpiredItems);
        rvExpiredItems.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<Item> options = new FirebaseRecyclerOptions.Builder<Item>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("users").child(createdBy)
                        .child("fridges").child(fridgeKey).child("expiredItems"), Item.class).build();
        expiredItemsAdapter = new MerelyExpiredItemsAdapter(options);
        expiredItemsAdapter.startListening();
        rvExpiredItems.setAdapter(expiredItemsAdapter);

        imgBtnClosePopout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expiredItemDialog.dismiss();
            }
        });

        expiredItemDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        expiredItemDialog.show();

    }

    private void showMerelyExpiredItemsPopUp() {
        Dialog merelyExpiredItemsDialog = new Dialog(this);
        merelyExpiredItemsDialog.setContentView(R.layout.merelyexpiredpopout);

        imgBtnClosePopout = merelyExpiredItemsDialog.findViewById(R.id.imgBtnClosePopout);
        rvMerelyExpiredItems = merelyExpiredItemsDialog.findViewById(R.id.rvMerelyExpiredItems);

        rvMerelyExpiredItems.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<Item> options = new FirebaseRecyclerOptions.Builder<Item>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("users").child(createdBy)
                        .child("fridges").child(fridgeKey).child("merelyExpiredItems"), Item.class).build();
        merelyExpiredItemsAdapter = new MerelyExpiredItemsAdapter(options);
        merelyExpiredItemsAdapter.startListening();
        rvMerelyExpiredItems.setAdapter(merelyExpiredItemsAdapter);

        imgBtnClosePopout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                merelyExpiredItemsDialog.dismiss();
            }
        });

        merelyExpiredItemsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        merelyExpiredItemsDialog.show();
    }

    private void checkIsHost() {
        if (createdBy.equals(currentUserID)){
            sendToAddParticipantsActivity();
        }else{
            Toast.makeText(this, "Only fridge host can invite other users.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendToAddParticipantsActivity() {
        Intent intent = new Intent(FridgeLayout.this, AddParticipantsActivity.class);
        intent.putExtra("fridgeKey", fridgeKey);
        startActivity(intent);
    }
}