package com.example.testingfyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FridgeActivity extends AppCompatActivity {

    private TextView tvFridgeNameBanner;

    private RecyclerView rvFreezer, rvShelf, rvDrawer;
    private ImageButton btnFreezerAdd, btnShelfAdd, btnDrawerAdd;
    private ItemAdapter itemAdapterForFreezer, itemAdapterForShelf, itemAdapterForDrawer;

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
        tvFridgeNameBanner = findViewById(R.id.tvFridgeNameBanner);
        tvFridgeNameBanner.setText(fridgeName);

        // set up the recycler options for the freezer
        FirebaseRecyclerOptions<Item> freezerOptions =
                new FirebaseRecyclerOptions.Builder<Item>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId)
                        .child("fridges").child(fridgeKey).child("freezer").child("items").orderByChild("days"),Item.class).build();
        itemAdapterForFreezer = new ItemAdapter(fridgeKey,"freezer",freezerOptions);
        rvFreezer.setAdapter(itemAdapterForFreezer);

        // set up the recycler options for the shelf
        FirebaseRecyclerOptions<Item> shelfOptions =
                new FirebaseRecyclerOptions.Builder<Item>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId)
                        .child("fridges").child(fridgeKey).child("shelf").child("items").orderByChild("days"),Item.class).build();
        itemAdapterForShelf = new ItemAdapter(fridgeKey,"shelf",shelfOptions);
        rvShelf.setAdapter(itemAdapterForShelf);

        // set up the recycler options for the drawer
        FirebaseRecyclerOptions<Item> drawerOptions =
                new FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId)
                                .child("fridges").child(fridgeKey).child("drawer").child("items").orderByChild("days"),Item.class).build();
        itemAdapterForDrawer = new ItemAdapter(fridgeKey,"drawer",drawerOptions);
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
                startActivity(intentAddItemToDrawer);
            }
        });

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