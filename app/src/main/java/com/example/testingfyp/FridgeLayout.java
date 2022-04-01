package com.example.testingfyp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class FridgeLayout extends AppCompatActivity {

    ImageButton imgBtnBack;
    TextView tvFridgeNameBanner, freezerTop, shelfMiddle, drawerBottom;
    Intent intentFromFridgeAdapter;

    String fridgeName, fridgeKey, createdBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fridge_layout);

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
    }
}