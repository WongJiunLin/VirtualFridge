package com.example.testingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditItemActivity extends AppCompatActivity {

    private Uri imageUri;

    private ImageView ivEditItemImage;
    private TextInputEditText edtItemName;
    private AutoCompleteTextView dropdownItemType, dropdownItemCategory, tvItemExpirationDate;

    private Intent intentFromItemAdapter;
    private String currentUserId;

    private FirebaseAuth mAuth;
    private DatabaseReference itemRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        ivEditItemImage = (ImageView) findViewById(R.id.ivEditItemImage);
        edtItemName = (TextInputEditText) findViewById(R.id.edtItemName);
        dropdownItemType = (AutoCompleteTextView) findViewById(R.id.dropdownItemType);
        dropdownItemCategory = (AutoCompleteTextView) findViewById(R.id.dropdownItemCategory);
        tvItemExpirationDate = (AutoCompleteTextView) findViewById(R.id.tvItemExpirationDate);

        intentFromItemAdapter = getIntent();
        String fridgeKey = intentFromItemAdapter.getStringExtra("fridgeKey");
        String containerType = intentFromItemAdapter.getStringExtra("containerType");
        String curItemId = intentFromItemAdapter.getStringExtra("curItemId");

        itemRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId)
                        .child("fridges").child(fridgeKey).child(containerType).child("items").child(curItemId);

        itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get item info
                String itemName = snapshot.child("itemName").getValue().toString();
                String itemType = snapshot.child("itemType").getValue().toString();
                String itemCategory = snapshot.child("itemCategory").getValue().toString();
                String itemExpirationDate = snapshot.child("itemExpirationDate").getValue().toString();
                String itemImageUri = snapshot.child("itemImgUri").getValue().toString();

                // assign item info into respective fields
                Picasso.get().load(Uri.parse(itemImageUri)).into(ivEditItemImage);
                edtItemName.setText(itemName);
                dropdownItemType.setText(itemType);
                dropdownItemCategory.setText(itemCategory);
                tvItemExpirationDate.setText(itemExpirationDate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}