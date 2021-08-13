package com.example.testingfyp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddItemActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private DatePickerDialog datePickerDialog;

    private Uri imageUri;

    private TextInputLayout txtIptLayoutItemType, txtIptLayoutItemCategory,txtIptLayoutItemExpirationDate;
    private AutoCompleteTextView dropdownItemType, dropdownItemCategory,tvItemExpirationDate;
    private TextInputEditText edtItemName;
    private ImageButton addItemButton, clearItemButton, imgBtnClosePopOut, imgBtnItemImage;
    private ProgressBar pbAddItem;

    private StorageReference itemImageRef;
    private DatabaseReference itemRef;
    private FirebaseAuth mAuth;
    private String currentUserId, saveCurrentDate, saveCurrentTime, imgRandomName, downloadUri;

    private Intent intentFromFridgeActivity;
    private String itemName, itemType, itemCategory, itemStoredDate, itemExpirationDate;
    private String fridgeKey, containerType;

    private TextView tvCheckExpiry;
    private Dialog expiryDialog;

    private static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        intentFromFridgeActivity = getIntent();
        fridgeKey = intentFromFridgeActivity.getStringExtra("fridgeKey");
        containerType = intentFromFridgeActivity.getStringExtra("containerType");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        pbAddItem = (ProgressBar) findViewById(R.id.pbAddItem);

        itemImageRef = FirebaseStorage.getInstance().getReference().child("users").child(currentUserId).child("fridges").child(fridgeKey).child("item images");
        itemRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId)
                .child("fridges").child(fridgeKey).child(containerType).child("items");

        // assign all the necessary item type choices into adapter,
        // set the autocomplete textview with respective adapter
        txtIptLayoutItemType = (TextInputLayout) findViewById(R.id.txtIptLayoutItemType);
        String[] itemTypes = new String[]{
                "Vegetable",
                "Fruit",
                "Fresh Meat",
                "Seafood"
        };
        ArrayAdapter<String> itemTypeAdapter = new ArrayAdapter<>(
                AddItemActivity.this,
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
                AddItemActivity.this,
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
                AddItemActivity.this,
                R.layout.dropdown_item,
                itemVegeCategories
        );

        // String array that consists of all fresh meat categories
        String[] itemFreshMeatCategories = new String[]{
                "Chilled Meat",
                "Frozen Meat"
        };
        // array adapter that created by using fresh meat categories array list
        ArrayAdapter<String> itemFreshMeatCategoryAdapter = new ArrayAdapter<>(
                AddItemActivity.this,
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
                AddItemActivity.this,
                R.layout.dropdown_item,
                itemSeafoodCategories
        );

        dropdownItemCategory = (AutoCompleteTextView) findViewById(R.id.dropdownItemCategory);
        //dropdownItemCategory.setAdapter(itemVegeCategoryAdapter);
        dropdownItemType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (dropdownItemType.getText().toString().equals("Vegetable")){
                    dropdownItemCategory.setAdapter(itemVegeCategoryAdapter);
                }
                else if (dropdownItemType.getText().toString().equals("Fruit")){
                    dropdownItemCategory.setAdapter(itemFruitCategoryAdapter);
                }
                else if (dropdownItemType.getText().toString().equals("Fresh Meat")){
                    dropdownItemCategory.setAdapter(itemFreshMeatCategoryAdapter);
                }
                else if (dropdownItemType.getText().toString().equals("Seafood")){
                    dropdownItemCategory.setAdapter(itemSeafoodCategoryAdapter);
                }else{
                    dropdownItemType.setError("Select your item type first");
                }
            }
        });




        edtItemName = (TextInputEditText) findViewById(R.id.edtItemName);

        imgBtnItemImage = (ImageButton) findViewById(R.id.imgBtnItemImage);
        // when click the adding image image view, redirect user to their device local storage to pick image
        imgBtnItemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        // click on the tick icon for item adding
        addItemButton = (ImageButton) findViewById(R.id.addItemButton);
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateItemInfo();
            }
        });
        // click on the clear button to redirect back to fridge page
        clearItemButton = (ImageButton) findViewById(R.id.clearItemButton);
        clearItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToFridgeActivity();
            }
        });

        expiryDialog = new Dialog(this);
        tvCheckExpiry = (TextView) findViewById(R.id.tvCheckExpiry);
        tvCheckExpiry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopOut(v);
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

    }

    private void showPopOut(View v) {
        expiryDialog.setContentView(R.layout.expirationdatepopup);
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

    private void returnToFridgeActivity() {
        startActivity(new Intent(AddItemActivity.this, FridgeActivity.class));
    }

    private void validateItemInfo() {

        itemName = edtItemName.getText().toString();
        itemType = dropdownItemType.getText().toString();
        itemCategory = dropdownItemCategory.getText().toString();
        itemExpirationDate = tvItemExpirationDate.getText().toString();

        if (TextUtils.isEmpty(itemName)){
            edtItemName.setError("Required field");
            return;
        }
        if (TextUtils.isEmpty(itemType)){
            dropdownItemType.setError("Please choose item type");
            return;
        }
        if (TextUtils.isEmpty(itemCategory)){
            dropdownItemCategory.setError("Please choose item category");
            return;
        }
        if (TextUtils.isEmpty(itemExpirationDate)){
            tvItemExpirationDate.setError("Please pick item expiration date");
            return;
        }

        insertImageToFirebaseStorage();

    }

    private void insertImageToFirebaseStorage() {

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-YYYY");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        // generate the image file name using the current date and time and the img last path
        imgRandomName = saveCurrentDate + " " + saveCurrentTime;
        StorageReference filePath = itemImageRef.child(imageUri.getLastPathSegment() + imgRandomName);
        // enable progress bar while start storing item info
        pbAddItem.setVisibility(View.VISIBLE);
        filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        downloadUri = uri.toString();
                        Toast.makeText(AddItemActivity.this, "Image uploaded to firebase storage successfully", Toast.LENGTH_SHORT).show();

                        SaveItemToFirebase();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddItemActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddItemActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void SaveItemToFirebase() {

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-YYYY");
        itemStoredDate = currentDate.format(calForDate.getTime());

        //create map to stored the current item info
        HashMap itemMap = new HashMap();
        itemMap.put("itemName",itemName);
        itemMap.put("itemType", itemType);
        itemMap.put("itemCategory",itemCategory);
        itemMap.put("itemStoredDate",itemStoredDate);
        itemMap.put("itemExpirationDate", itemExpirationDate);
        itemMap.put("itemImgUri",downloadUri);

        //store the hashmap into the firebase real-time database
        itemRef.child(itemName+itemStoredDate+saveCurrentTime).updateChildren(itemMap).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(AddItemActivity.this, "New Item Added", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddItemActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imageUri = data.getData();
            imgBtnItemImage.setImageURI(imageUri);
        }else{
            Toast.makeText(AddItemActivity.this, "Error occurred while picking image from local storage.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        tvItemExpirationDate.setText(currentDate);
    }
}