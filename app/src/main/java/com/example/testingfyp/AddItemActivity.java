package com.example.testingfyp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddItemActivity extends AppCompatActivity {

    private DatePickerDialog datePickerDialog;
    private Button expirationDateButton;

    private ImageView ivItemImage;
    private Uri imageUri;
    private EditText edtItemName, edtItemCategory;
    private ImageButton addItemButton, clearItemButton;
    private ProgressBar pbAddItem;

    private StorageReference itemImageRef;
    private DatabaseReference itemRef;
    private FirebaseAuth mAuth;
    private String currentUserId, saveCurrentDate, saveCurrentTime, imgRandomName, downloadUri;

    private Intent intentFromFridgeActivity;
    private String itemName, itemCategory, itemStoredDate, itemExpirationDate;

    private static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        initDatePicker();
        expirationDateButton = (Button) findViewById(R.id.datePickerButton);
        expirationDateButton.setText(getTodayDate());

        intentFromFridgeActivity = getIntent();
        String fridgeKey = intentFromFridgeActivity.getStringExtra("fridgeKey");
        String containerType = intentFromFridgeActivity.getStringExtra("containerType");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        pbAddItem = (ProgressBar) findViewById(R.id.pbAddItem);

        itemImageRef = FirebaseStorage.getInstance().getReference().child(currentUserId).child("item images");
        itemRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId)
                .child("fridges").child(fridgeKey).child(containerType).child("items");

        edtItemName = (EditText) findViewById(R.id.edtItemName);
        edtItemCategory = (EditText) findViewById(R.id.edtItemCategory);

        ivItemImage = (ImageView) findViewById(R.id.ivItemImage);
        // when click the adding image image view, redirect user to their device local storage to pick image
        ivItemImage.setOnClickListener(new View.OnClickListener() {
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
    }

    private void returnToFridgeActivity() {
        startActivity(new Intent(AddItemActivity.this, FridgeActivity.class));
    }

    private void validateItemInfo() {

        itemName = edtItemName.getText().toString();
        itemCategory = edtItemCategory.getText().toString();
        itemExpirationDate = expirationDateButton.getText().toString();

        if (TextUtils.isEmpty(itemName)){
            edtItemName.setError("Required field");
            return;
        }
        if (TextUtils.isEmpty(itemCategory)){
            edtItemCategory.setError("Required field");
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
            ivItemImage.setImageURI(imageUri);
        }else{
            Toast.makeText(AddItemActivity.this, "Error occurred while picking image from local storage.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getTodayDate() {
        Calendar cal = Calendar.getInstance();
        int year =  cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month +1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return makeDateString(day, month, year);
    }

    private void initDatePicker() {

        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = makeDateString(day, month, year);
                expirationDateButton.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year =  cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);

    }

    private String makeDateString(int day, int month, int year) {
        return day + " " + getMonthFormat(month) + " " + year;
    }

    private String getMonthFormat(int month) {
        if (month==1)
            return "JAN";
        if (month==2)
            return "FEB";
        if (month==3)
            return "MAR";
        if (month==4)
            return "APR";
        if (month==5)
            return "MAY";
        if (month==6)
            return "JUN";
        if (month==7)
            return "JUL";
        if (month==8)
            return "AUG";
        if (month==9)
            return "SEP";
        if (month==10)
            return "OCT";
        if (month==11)
            return "NOV";
        if (month==12)
            return "DEC";

        //default
        return "JAN";
    }

    public void openDatePicker(View view) {

        datePickerDialog.show();

    }
}