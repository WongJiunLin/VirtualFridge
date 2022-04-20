package com.example.testingfyp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
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

import com.example.testingfyp.ml.Model;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.joda.time.Days;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.w3c.dom.Text;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddItemActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private Uri imageUri, itemImgHint;

    private TextInputLayout txtIptLayoutItemType, txtIptLayoutItemCategory,txtIptLayoutItemExpirationDate, txtIptLayoutItemPosition;
    private AutoCompleteTextView dropdownItemType, dropdownItemCategory,tvItemExpirationDate, dropdownItemPosition;
    private TextInputEditText edtItemName, edtItemQuantity;
    private ImageButton addItemButton, clearItemButton, imgBtnClosePopOut, imgBtnClassifyItem, imgBtnClassifiedItemClosePopOut, imgBtnCloseNotRecommendedPopup;
    private ImageView ivItemImage;
    private ProgressBar pbAddItem;

    private CircleImageView civPopOutImg, civClassifiedItemImg, civNotRecommendedItemImg;

    private StorageReference itemImageRef;
    private DatabaseReference itemRef, itemShelfLifeRef;
    private FirebaseAuth mAuth;
    private String currentUserId, currentUsername, saveCurrentDate, saveCurrentTime, imgRandomName, downloadUri;

    private Intent intentFromOthers;
    private Date storedDate, expirationDate;
    private String itemName, itemType, itemCategory, itemStoredDate, itemExpirationDate, itemExpiryDate, itemPosition;
    private int itemQuantity;
    private String fridgeKey, containerType, containerKey, itemTypeHint, itemCategoryHint, itemShelfLifeHint, createdBy;
    private ArrayAdapter<String> itemTypeAdapter;

    private String classifiedResult;
    private Bitmap image;

    private Calendar calForStoredDate, calForExpiryDate;

    private TextView tvCheckExpiry, tvItemCategoryHint, tvItemShelfLifeHint, tvAddItemBanner;
    private TextView tvClassifiedItemName, tvClassifiedItemCategory, tvClassifiedItemType, tvClassifiedItemShelfLife;
    private Dialog expiryDialog, classifiedDialog, notRecommendedDialog;
    private Button btnCorrectClassified, btnWrongClassified, btnContinueAdd, btnCancelAdd;

    private static int Gallery_Pick = 1;

    private int imageSize = 128;
    private int PIXEL_SIZE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        intentFromOthers = getIntent();
        fridgeKey = intentFromOthers.getStringExtra("fridgeKey");
        containerType = intentFromOthers.getStringExtra("containerType");
        containerKey = intentFromOthers.getStringExtra("containerKey");
        createdBy = intentFromOthers.getStringExtra("createdBy");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    currentUsername = snapshot.child("username").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        pbAddItem = (ProgressBar) findViewById(R.id.pbAddItem);

        itemImageRef = FirebaseStorage.getInstance().getReference().child("users").child(createdBy).child("fridges").child(fridgeKey).child(containerType).child(containerKey).child("item images");
        itemRef = FirebaseDatabase.getInstance().getReference().child("users").child(createdBy)
                .child("fridges").child(fridgeKey).child(containerType).child(containerKey).child("items");
        itemShelfLifeRef = FirebaseDatabase.getInstance().getReference().child("shelf-life");

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
        itemTypeAdapter = new ArrayAdapter<>(
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
                "Chilled meat",
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
                AddItemActivity.this,
                R.layout.dropdown_item,
                itemPositions
        );
        dropdownItemPosition = (AutoCompleteTextView) findViewById(R.id.dropdownItemPosition);
        dropdownItemPosition.setAdapter(itemPositionAdapter);

        edtItemName = (TextInputEditText) findViewById(R.id.edtItemName);
        edtItemQuantity = (TextInputEditText) findViewById(R.id.edtItemQuantity);

        // click on the tick icon for item adding
        addItemButton = (ImageButton) findViewById(R.id.addItemButton);
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateItemInfo();
            }
        });

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

        // while click on the back button close current activity
        notRecommendedDialog = new Dialog(this);
        tvAddItemBanner = (TextView) findViewById(R.id.tvAddItemBanner);
        tvAddItemBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        classifiedDialog = new Dialog(this);
        ivItemImage = (ImageView) findViewById(R.id.ivItemImage);
        // when click the adding image image view, redirect user to their device local storage to pick image
        ivItemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 2);
            }
        });

        // while click on the camera prompt to the ClassifyItem Activity
        imgBtnClassifyItem = (ImageButton) findViewById(R.id.imgBtnClassifyItem);
        imgBtnClassifyItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 3);
                }else{
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });

    }

    private void classifyImage(Bitmap image) {
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 128, 128, 3}, DataType.FLOAT32); // version 3

            int size = 4 * imageSize * imageSize * PIXEL_SIZE; // version 2
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(size); // version 2
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize*imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            // iterate over each pixel and extract R, G and B values. Add those values individually to the byte buffer.
            for(int i = 0; i < imageSize; i++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; //RGB

                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f/1));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f/1));
                    byteBuffer.putFloat((val & 0xFF) * (1.f/1));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidence = outputFeature0.getFloatArray();
            // find the index of the class with biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i=0; i < confidence.length; i++){
                if (confidence[i] > maxConfidence){
                    maxConfidence = confidence[i];
                    maxPos = i;
                }
            }

            // for dataset v2 ( not using fruit 360)
            String[] classes = {"fruits_bell pepper_pepper", "fruits_berries_pumpkin", "fruits_citrus_lemon",
                    "fruits_citrus_orange", "fruits_melon_watermelon", "fruits_pome fruit_apple",
                    "fruits_pome fruit_pear", "fruits_tomato_tomato", "fruits_tropical fruit_banana",
                    "fruits_tropical fruit_papaya", "fruits_tropical fruit_pineapple", "seafood_fresh fish_salmon",
                    "seafood_fresh fish_scallops", "seafood_fresh fish_swordfish", "seafood_fresh fish_tuna",
                    "seafood_shell fish_crab", "seafood_shell fish_lobster tail", "seafood_shell fish_mussel",
                    "seafood_shell fish_oyster", "seafood_shell fish_shrimp", "vegetables_allium_ginger",
                    "vegetables_cruciferous vegetable_broccoli", "vegetables_cruciferous vegetable_cabbage",
                    "vegetables_cruciferous vegetable_cauliflower", "vegetables_marrow_cucumber", "vegetables_marrow_eggplant",
                    "vegetables_root vegetable_carrot", "vegetables_root vegetable_corn", "vegetables_root vegetable_potato",
                    "vegetables_root vegetable_radish", "vegetables_root vegetable_sweet potato"};

            // Releases model resources if no longer used.
            model.close();
            classifiedResult = classes[maxPos];
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    private void showClassifiedItemPopup() {
        classifiedDialog.setContentView(R.layout.classifiediteminfopopup);

        //assign the classified result to the popup dialog
        civClassifiedItemImg = (CircleImageView) classifiedDialog.findViewById(R.id.civClassifiedItemImg);
        civClassifiedItemImg.setImageBitmap(image);

        String[] classifiedItem = classifiedResult.split("_");
        tvClassifiedItemName = (TextView) classifiedDialog.findViewById(R.id.tvClassifiedItemName);
        tvClassifiedItemName.setText(classifiedItem[2]);

        tvClassifiedItemCategory = (TextView) classifiedDialog.findViewById(R.id.tvClassifiedItemCategory);
        tvClassifiedItemCategory.setText(classifiedItem[1]);

        tvClassifiedItemType = (TextView) classifiedDialog.findViewById(R.id.tvClassifiedItemType);
        tvClassifiedItemType.setText(classifiedItem[0]);

        imgBtnClassifiedItemClosePopOut = (ImageButton) classifiedDialog.findViewById(R.id.imgBtnClassifiedItemClosePopOut);
        imgBtnClassifiedItemClosePopOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                classifiedDialog.dismiss();
            }
        });

        tvClassifiedItemShelfLife = (TextView) classifiedDialog.findViewById(R.id.tvClassifiedItemShelfLife);
        // obtain the recommendation shelf-life
        FirebaseDatabase.getInstance().getReference().child("shelf-life").child(classifiedItem[0]).child(classifiedItem[1]).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("shelf-life")){
                    String recommendedShelfLife = snapshot.child("shelf-life").getValue().toString();
                    // assign recommended shelf-life to respective field
                    tvClassifiedItemShelfLife.setText(recommendedShelfLife);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //while click on yes button, fill the classified item info into the add item field
        //click the no button then dismiss the dialog
        btnCorrectClassified = (Button) classifiedDialog.findViewById(R.id.btnCorrectClassified);
        btnCorrectClassified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edtItemName.setText(classifiedItem[2]);
                dropdownItemCategory.setText(classifiedItem[1]);
                dropdownItemType.setText(classifiedItem[0], false);
                ivItemImage.setImageBitmap(image);
                classifiedDialog.dismiss();
            }
        });

        btnWrongClassified = (Button) classifiedDialog.findViewById(R.id.btnWrongClassified);
        btnWrongClassified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ivItemImage.setImageBitmap(image);
                classifiedDialog.dismiss();
            }
        });

        classifiedDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        classifiedDialog.show();

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
                    Toast.makeText(AddItemActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                }
            });
        }
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


    private void validateItemInfo() {

        itemName = edtItemName.getText().toString();
        itemType = dropdownItemType.getText().toString();
        itemCategory = dropdownItemCategory.getText().toString();
        itemPosition = dropdownItemPosition.getText().toString();
        itemExpirationDate = tvItemExpirationDate.getText().toString();
        String strItemQuantity = edtItemQuantity.getText().toString();

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
        if (TextUtils.isEmpty(strItemQuantity)){
            edtItemQuantity.setError("Required field");
            return;
        }else{
            itemQuantity = Integer.parseInt(strItemQuantity);
        }

        // for drawer and shelf need to input expiration date
        if (!containerType.equals("freezers")&&TextUtils.isEmpty(itemExpirationDate)){
            tvItemExpirationDate.setError("Please pick item expiration date");
            return;
        }
        if (TextUtils.isEmpty(itemPosition)){
            tvItemExpirationDate.setError("Please choose item position");
            return;
        }

        // if current container type is freezer then not recommend user to store
        // things beside seafood and fresh meat.
        if (containerType.equals("freezers")){
            if (itemType.equals("Fruits") || itemType.equals("Vegetables")){
                showNotRecommendedPopup();
            }else{
                insertImageToFirebaseStorage();
            }
        } else{
            insertImageToFirebaseStorage();
        }

    }

    private void showNotRecommendedPopup() {
        notRecommendedDialog.setContentView(R.layout.notrecommendedpopup);
        imgBtnCloseNotRecommendedPopup = (ImageButton) notRecommendedDialog.findViewById(R.id.imgBtnCloseNotRecommendedPopup);
        imgBtnCloseNotRecommendedPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notRecommendedDialog.dismiss();
            }
        });
        imgBtnCloseNotRecommendedPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notRecommendedDialog.dismiss();
            }
        });

        btnContinueAdd = (Button) notRecommendedDialog.findViewById(R.id.btnContinueAdd);
        btnContinueAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notRecommendedDialog.dismiss();
                insertImageToFirebaseStorage();
            }
        });

        btnCancelAdd = (Button) notRecommendedDialog.findViewById(R.id.btnCancelAdd);
        btnCancelAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notRecommendedDialog.dismiss();
            }
        });

        notRecommendedDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        notRecommendedDialog.show();

    }

    private void insertImageToFirebaseStorage() {

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
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

        calForStoredDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        storedDate = calForStoredDate.getTime();
        itemStoredDate = currentDate.format(storedDate);
        int days = 0;

        if (expirationDate==null){
            days = 0;
        }else{
            days = daysBetween(storedDate, expirationDate);
        }

        //create map to stored the current item info
        HashMap itemMap = new HashMap();
        itemMap.put("itemName",itemName);
        itemMap.put("itemType", itemType);
        itemMap.put("itemCategory",itemCategory);
        itemMap.put("itemStoredDate",itemStoredDate);
        itemMap.put("itemExpirationDate", itemExpirationDate);
        itemMap.put("itemPosition", itemPosition);
        itemMap.put("itemQuantity",itemQuantity);
        itemMap.put("placedBy", currentUsername);
        itemMap.put("itemImgUri",downloadUri);
        itemMap.put("days",days);

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

    private int daysBetween(Date storedDate, Date expiryDate) {
        return (int) ((expiryDate.getTime()-storedDate.getTime())/(1000*60*60*24)+1);
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
        if (resultCode == RESULT_OK){
            if (requestCode == 3){
                image = (Bitmap) data.getExtras().get("data");
                // resize the bitmap image to fit with the classification model
                int dimension = Math.min(image.getWidth(), image.getHeight());
                // rescaling the image dimension
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                //imageView.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
                showClassifiedItemPopup();
            }else if (requestCode == 2){
                imageUri = data.getData();
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //imageView.setImageBitmap(image);
                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
                showClassifiedItemPopup();
            } else if (requestCode == Gallery_Pick){
                imageUri = data.getData();
                ivItemImage.setImageURI(imageUri);
            }
        }

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
}