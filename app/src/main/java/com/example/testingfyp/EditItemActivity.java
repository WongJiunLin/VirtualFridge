package com.example.testingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditItemActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private Uri imageUri, itemImgHint;

    private ImageView ivEditItemImage;
    private TextInputLayout txtIptLayoutItemType, txtIptLayoutItemCategory,txtIptLayoutItemExpirationDate, txtIptLayoutItemPosition;
    private TextInputEditText edtItemName, edtItemQuantity;
    private AutoCompleteTextView dropdownItemType, dropdownItemCategory, dropdownItemPosition, tvItemExpirationDate;
    private TextView tvEditItemBanner, tvCheckExpiry, tvItemCategoryHint, tvItemShelfLifeHint, tvAddItemBanner;
    private CircleImageView civPopOutImg;
    private ImageButton editItemButton, imgBtnClosePopOut;
    private ProgressBar pbEditItem;

    private Intent intentFromItemAdapter;
    private String currentUserId;

    private FirebaseAuth mAuth;
    private DatabaseReference itemRef, itemShelfLifeRef;

    private Calendar calForStoredDate, calForExpiryDate;
    private Date storedDate, expirationDate, edtExpirationDate;
    private String itemName, itemType, itemCategory, itemStoredDate, itemExpirationDate, itemPosition, itemImageUri, itemQuantity;

    private Dialog expiryDialog;
    private String itemTypeHint, itemCategoryHint, itemShelfLifeHint;

    private String edtItemType, edtItemCategory, edtItemExpirationDate, edtItemPosition, editItemQuantity;

    private String fridgeKey, createdBy, containerKey, containerType, curItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        tvEditItemBanner = (TextView) findViewById(R.id.tvEditItemBanner);
        ivEditItemImage = (ImageView) findViewById(R.id.ivEditItemImage);
        edtItemName = (TextInputEditText) findViewById(R.id.edtItemName);
        edtItemName.setInputType(InputType.TYPE_NULL);
        edtItemQuantity = (TextInputEditText) findViewById(R.id.edtItemQuantity);
        dropdownItemType = (AutoCompleteTextView) findViewById(R.id.dropdownItemType);
        dropdownItemCategory = (AutoCompleteTextView) findViewById(R.id.dropdownItemCategory);
        tvItemExpirationDate = (AutoCompleteTextView) findViewById(R.id.tvItemExpirationDate);
        dropdownItemPosition = (AutoCompleteTextView) findViewById(R.id.dropdownItemPosition);

        pbEditItem = (ProgressBar) findViewById(R.id.pbEditItem);

        intentFromItemAdapter = getIntent();
        fridgeKey = intentFromItemAdapter.getStringExtra("fridgeKey");
        containerType = intentFromItemAdapter.getStringExtra("containerType");
        containerKey = intentFromItemAdapter.getStringExtra("containerKey");
        createdBy = intentFromItemAdapter.getStringExtra("createdBy");
        curItemId = intentFromItemAdapter.getStringExtra("curItemId");

        itemRef = FirebaseDatabase.getInstance().getReference().child("users").child(createdBy)
                        .child("fridges").child(fridgeKey).child(containerType).child(containerKey).child("items").child(curItemId);

        itemShelfLifeRef = FirebaseDatabase.getInstance().getReference().child("shelf-life");

        tvEditItemBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // assign all the necessary item type choices into adapter,
        // set the autocomplete textview with respective adapter
        txtIptLayoutItemType = (TextInputLayout) findViewById(R.id.txtIptLayoutItemType);
        String[] itemTypes = new String[]{
                "Vegetables",
                "Fruits",
                "Fresh Meat",
                "Seafood",
                "Others"
        };
        ArrayAdapter<String> itemTypeAdapter = new ArrayAdapter<>(
                EditItemActivity.this,
                R.layout.dropdown_item,
                itemTypes
        );
        dropdownItemType = (AutoCompleteTextView) findViewById(R.id.dropdownItemType);
        dropdownItemType.setAdapter(itemTypeAdapter);

        // assign all necessary item category choices into adapter,
        // set the autocomplete textview with respective adapter
        txtIptLayoutItemCategory = (TextInputLayout) findViewById(R.id.txtIptLayoutItemCategory);
        // String array that consists of all fruit categories
        String[] itemFruitCategories = new String[]{
                "Avocado",
                "Bell Pepper",
                "Berries",
                "Citrus",
                "Melon",
                "Pome Fruit",
                "Stone Fruit",
                "Tomato",
                "Tropical Fruit"
        };
        // array adapter created by using fruit categories array list
        ArrayAdapter<String> itemFruitCategoryAdapter = new ArrayAdapter<>(
                EditItemActivity.this,
                R.layout.dropdown_item,
                itemFruitCategories
        );

        // String array that consists of all vegetable categories
        String[] itemVegeCategories = new String[]{
                "Allium",
                "Cruciferous Vegetable",
                "Leafy Green",
                "Marrow",
                "Root Vegetable"
        };
        // array adapter that created by using vegetable categories array list
        ArrayAdapter<String> itemVegeCategoryAdapter = new ArrayAdapter<>(
                EditItemActivity.this,
                R.layout.dropdown_item,
                itemVegeCategories
        );

        // String array that consists of all fresh meat categories
        String[] itemFreshMeatCategories = new String[]{
                "Chilled meat",
                "Frozen Meat"
        };
        // array adapter that created by using fresh meat categories array list
        ArrayAdapter<String> itemFreshMeatCategoryAdapter = new ArrayAdapter<>(
                EditItemActivity.this,
                R.layout.dropdown_item,
                itemFreshMeatCategories
        );

        // String array that consists of all seafood categories
        String[] itemSeafoodCategories = new String[]{
                "Fresh Fish",
                "Shell Fish"
        };
        // array adapter that created by using fresh meat categories array list
        ArrayAdapter<String> itemSeafoodCategoryAdapter = new ArrayAdapter<>(
                EditItemActivity.this,
                R.layout.dropdown_item,
                itemSeafoodCategories
        );

        dropdownItemCategory = (AutoCompleteTextView) findViewById(R.id.dropdownItemCategory);
        dropdownItemType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (dropdownItemType.getText().toString().equals("Vegetables")){
                    dropdownItemCategory.setAdapter(itemVegeCategoryAdapter);
                }
                else if (dropdownItemType.getText().toString().equals("Fruits")){
                    dropdownItemCategory.setAdapter(itemFruitCategoryAdapter);
                }
                else if (dropdownItemType.getText().toString().equals("Fresh Meat")){
                    dropdownItemCategory.setAdapter(itemFreshMeatCategoryAdapter);
                }
                else if (dropdownItemType.getText().toString().equals("Seafood")){
                    dropdownItemCategory.setAdapter(itemSeafoodCategoryAdapter);
                }
                else if(dropdownItemType.getText().toString().equals("Others")){
                    dropdownItemCategory.setText("Others");
                }
                else{
                    dropdownItemType.setError("Select your item type first");
                }
            }
        });

        // assign all the necessary item position choices into adapter
        txtIptLayoutItemPosition = (TextInputLayout) findViewById(R.id.txtIptLayoutItemPosition);
        String[] itemPositions = new String[]{
                "TopLeft",
                "Top",
                "TopRight",
                "MiddleLeft",
                "Middle",
                "MiddleRight",
                "BottomLeft",
                "Bottom",
                "BottomRight"
        };
        ArrayAdapter<String> itemPositionAdapter = new ArrayAdapter<>(
                EditItemActivity.this,
                R.layout.dropdown_item,
                itemPositions
        );
        dropdownItemPosition = (AutoCompleteTextView) findViewById(R.id.dropdownItemPosition);
        dropdownItemPosition.setAdapter(itemPositionAdapter);

        itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get item info
                itemName = snapshot.child("itemName").getValue().toString();
                itemType = snapshot.child("itemType").getValue().toString();
                itemCategory = snapshot.child("itemCategory").getValue().toString();
                itemExpirationDate = snapshot.child("itemExpirationDate").getValue().toString();
                itemPosition = snapshot.child("itemPosition").getValue().toString();
                itemImageUri = snapshot.child("itemImgUri").getValue().toString();
                itemQuantity = snapshot.child("itemQuantity").getValue().toString();

                // assign item info into respective fields
                Picasso.get().load(Uri.parse(itemImageUri)).into(ivEditItemImage);
                edtItemName.setText(itemName);
                edtItemQuantity.setText(itemQuantity);
                dropdownItemType.setText(itemType, false);
                dropdownItemCategory.setText(itemCategory);
                tvItemExpirationDate.setText(itemExpirationDate);
                dropdownItemPosition.setText(itemPosition, false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // check item recommendation expiration date
        expiryDialog = new Dialog(this);
        tvCheckExpiry = (TextView) findViewById(R.id.tvCheckExpiry);
        tvCheckExpiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkItemTypeCategory(v);
            }
        });

        // while click on the txt input layout for expiration date prompt user to date picker,
        // to pick expiration date for the item
        txtIptLayoutItemExpirationDate = (TextInputLayout) findViewById(R.id.txtIptLayoutItemExpirationDate);
        tvItemExpirationDate = (AutoCompleteTextView) findViewById(R.id.tvItemExpirationDate);
        tvItemExpirationDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(),"date picker");
            }
        });

        editItemButton = (ImageButton) findViewById(R.id.editItemButton);
        editItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateItemInfo();
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        calForExpiryDate = Calendar.getInstance();
        calForExpiryDate.set(Calendar.YEAR, year);
        calForExpiryDate.set(Calendar.MONTH, month);
        calForExpiryDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        //DateFormat.FULL
        //String currentDate = DateFormat.getDateInstance().format(calForExpiryDate.getTime());
        expirationDate = calForExpiryDate.getTime();
        itemExpirationDate = currentDate.format(expirationDate);
        tvItemExpirationDate.setText(itemExpirationDate);
    }

    private void showPopOut(View v) {

        expiryDialog.setContentView(R.layout.expirationdatepopup);

        // assign the retrieve item image at pop out circle img view
        civPopOutImg = (CircleImageView) expiryDialog.findViewById(R.id.civPopOutImg);
        if (itemTypeHint.equals("others")){
            civPopOutImg.setImageResource(R.drawable.icon_not_found);
        }else{
            Picasso.get().load(itemImgHint).into(civPopOutImg);
        }

        // assign the retrieved item category at the pop out text view
        tvItemCategoryHint = (TextView) expiryDialog.findViewById(R.id.tvItemCategoryHint);
        if (itemTypeHint.equals("others")){
            tvItemCategoryHint.setText("Others");
        }
        else {
            tvItemCategoryHint.setText(itemCategoryHint);
        }

        // assign the retrieved item shelf life at the pop out text view
        tvItemShelfLifeHint = (TextView) expiryDialog.findViewById(R.id.tvItemShelfLifeHint);
        if (itemTypeHint.equals("others")){
            tvItemShelfLifeHint.setText("Storing Duration Not Found");
        }
        else{
            tvItemShelfLifeHint.setText(itemShelfLifeHint);
        }

        imgBtnClosePopOut = expiryDialog.findViewById(R.id.imgBtnClosePopOut);
        imgBtnClosePopOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expiryDialog.dismiss();
            }
        });

        expiryDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        expiryDialog.show();

    }

    private void checkItemTypeCategory(View v) {
        itemTypeHint = dropdownItemType.getText().toString().toLowerCase();
        itemCategoryHint = dropdownItemCategory.getText().toString().toLowerCase();

        if (TextUtils.isEmpty(itemTypeHint)){
            dropdownItemType.setError("Please pick your item type");
            return;
        }
        if (TextUtils.isEmpty(itemCategoryHint)){
            dropdownItemCategory.setError("Please choose your item category");
            return;
        }
        if (itemTypeHint.equals("others")){
            showPopOut(v);
        }
        else{
            itemShelfLifeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    itemShelfLifeHint = snapshot.child(itemTypeHint).child(itemCategoryHint).child("shelf-life").getValue().toString();
                    itemImgHint = Uri.parse(snapshot.child(itemTypeHint).child(itemCategoryHint).child("imageUri").getValue().toString());
                    showPopOut(v);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(EditItemActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void editItemInFirebase(){

        calForStoredDate = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        storedDate = calForStoredDate.getTime();
        itemStoredDate = sdf.format(storedDate);
        int days = 0;

        try {
            edtExpirationDate = sdf.parse(edtItemExpirationDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (!containerType.equals("freezers")){
            days = daysBetween(storedDate, edtExpirationDate);
        }else{
            days = 0;
        }


        //create map to stored the latest item info
        HashMap itemMap = new HashMap();
        itemMap.put("itemName",itemName);
        itemMap.put("itemType", edtItemType);
        itemMap.put("itemCategory",edtItemCategory);
        itemMap.put("itemStoredDate",itemStoredDate);
        itemMap.put("itemExpirationDate", edtItemExpirationDate);
        itemMap.put("itemPosition", edtItemPosition);
        itemMap.put("itemImgUri",itemImageUri);
        itemMap.put("itemQuantity", Integer.parseInt(editItemQuantity));
        itemMap.put("days",days);

        pbEditItem.setVisibility(View.VISIBLE);
        itemRef.updateChildren(itemMap).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(EditItemActivity.this, itemName+ " info updated", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditItemActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateItemInfo() {

        edtItemType = dropdownItemType.getText().toString();
        edtItemCategory = dropdownItemCategory.getText().toString();
        edtItemExpirationDate = tvItemExpirationDate.getText().toString();
        edtItemPosition = dropdownItemPosition.getText().toString();
        editItemQuantity = edtItemQuantity.getText().toString();

        if (TextUtils.isEmpty(edtItemType)){
            dropdownItemType.setError("Please choose item type");
            return;
        }
        if (TextUtils.isEmpty(edtItemCategory)){
            dropdownItemCategory.setError("Please choose item category");
            return;
        }
        if (!containerType.equals("freezers")&&TextUtils.isEmpty(edtItemExpirationDate)){
            tvItemExpirationDate.setError("Please pick item expiration date");
            return;
        }
        if (TextUtils.isEmpty(edtItemPosition)){
            dropdownItemPosition.setError("Please pick item expiration date");
            return;
        }
        if (TextUtils.isEmpty(editItemQuantity)){
            edtItemQuantity.setError("Please fill in item amount");
            return;
        }

        editItemInFirebase();
    }


    public int daysBetween(Date storedDate, Date expiryDate){
        return (int) ((expiryDate.getTime()-storedDate.getTime())/(1000*60*60*24)+1);
    }
}