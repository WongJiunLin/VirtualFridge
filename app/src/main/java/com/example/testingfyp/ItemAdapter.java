package com.example.testingfyp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.units.qual.A;
import org.joda.time.Days;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import at.markushi.ui.CircleButton;
import de.hdodenhof.circleimageview.CircleImageView;

public class ItemAdapter extends FirebaseRecyclerAdapter<Item, ItemAdapter.myViewHolder> {

    private String fridgeKey, containerType, containerKey, createdBy;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String currentUserId = mAuth.getUid();

    private String currentItemId, currentUsername, containerName, placedByUsername;
    private int colorAlert, colorModerate, colorSafe;

    private Dialog itemDialog;
    private TextView tvItemName, tvPlacedBy, tvAvailableDay, tvItemPosition, tvItemQuantity, tvAvailableDayBanner;
    private CircleImageView civItemImg;
    private ImageButton imgBtnItemInfoClosePopOut;
    private LinearLayout llItemInfo;
    private CircleButton btnEditItem, btnConsumeItem, btnDeleteItem;

    public ItemAdapter(String fridgeKey, String containerType, String containerKey, String createdBy, @NonNull FirebaseRecyclerOptions<Item> options) {
        super(options);
        this.fridgeKey = fridgeKey;
        this.containerType = containerType;
        this.containerKey = containerKey;
        this.createdBy = createdBy;
    }

    @Override
    protected void onBindViewHolder(@NonNull ItemAdapter.myViewHolder holder, int position, @NonNull Item model) {

        // current Item
        currentItemId = getRef(holder.getAdapterPosition()).getKey();
        FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("username")){
                    currentUsername = snapshot.child("username").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // obtain color code
        colorAlert = holder.cvItem.getResources().getColor(R.color.nearlyExpired);
        colorModerate = holder.cvItem.getResources().getColor(R.color.moderateExpired);
        colorSafe = holder.cvItem.getResources().getColor(R.color.safeExpired);

        FirebaseDatabase.getInstance().getReference()
                .child("users").child(createdBy).child("fridges").child(fridgeKey)
                .child(containerType).child(containerKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("containerName")){
                    containerName = snapshot.child("containerName").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseDatabase.getInstance().getReference()
                .child("users").child(createdBy).child("fridges").child(fridgeKey).child(containerType).child(containerKey)
                .child("items").child(currentItemId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // get the item info
                String itemName = snapshot.child("itemName").getValue().toString();
                String placedBy = snapshot.child("placedBy").getValue().toString();
                String itemStoredDate = snapshot.child("itemStoredDate").getValue().toString();
                String itemImgUri = snapshot.child("itemImgUri").getValue().toString();
                String itemPosition = snapshot.child("itemPosition").getValue().toString();
                int itemQuantity = Integer.parseInt(snapshot.child("itemQuantity").getValue().toString());

                LocalDate currentDate = LocalDate.now();
                int daysBetween;
                // for drawers and shelves
                if (!containerType.equals("freezers")){
                    // only drawer and shelves item have expiration date
                    String itemExpirationDate = snapshot.child("itemExpirationDate").getValue().toString();
                    String[] expiryDateComp = itemExpirationDate.split("-");
                    int day = Integer.valueOf(expiryDateComp[0]);
                    int month = Integer.valueOf(expiryDateComp[1]);
                    int year = Integer.valueOf(expiryDateComp[2]);
                    LocalDate expirationDate = LocalDate.of(year,month,day);
                    daysBetween = (int) ChronoUnit.DAYS.between(currentDate,expirationDate);

                    int days = Integer.parseInt(snapshot.child("days").getValue().toString());
                    if (days<=1){
                        holder.cvItem.setCardBackgroundColor(colorAlert);
                    }
                    else if (days>1 && days<=7){
                        holder.cvItem.setCardBackgroundColor(colorModerate);
                    }
                    else if(days>7){
                        holder.cvItem.setCardBackgroundColor(colorSafe);
                    }

                    // if the item is expired, stored it at another branch
                    if (days <= 0){

                        HashMap expiredItemMap = new HashMap();
                        expiredItemMap.put("itemName",itemName);
                        expiredItemMap.put("placedBy", placedBy);
                        expiredItemMap.put("itemExpirationDate",itemExpirationDate);
                        expiredItemMap.put("itemStoredDate",itemStoredDate);
                        expiredItemMap.put("itemImgUri",itemImgUri);
                        expiredItemMap.put("itemPosition", itemPosition);
                        expiredItemMap.put("itemQuantity", itemQuantity);
                        expiredItemMap.put("containerType",containerType);
                        expiredItemMap.put("days",days);
                        expiredItemMap.put("placedAt",containerName);
                        // move item info into expired items list
                        FirebaseDatabase.getInstance().getReference()
                                .child("users").child(createdBy).child("fridges").child(fridgeKey)
                                .child("expiredItems").child(getRef(holder.getAdapterPosition()).getKey()).updateChildren(expiredItemMap);
                        // remove item info within merely expired items list
                        FirebaseDatabase.getInstance().getReference()
                                .child("users").child(createdBy).child("fridges").child(fridgeKey)
                                .child("merelyExpiredItems").child(getRef(holder.getAdapterPosition()).getKey()).removeValue();
                    }else if (days <= 3 && days >= 1){
                        HashMap merelyExpiredItemMap = new HashMap();
                        merelyExpiredItemMap.put("itemName", itemName);
                        merelyExpiredItemMap.put("placedBy",placedBy);
                        merelyExpiredItemMap.put("itemImgUri",itemImgUri);
                        merelyExpiredItemMap.put("itemQuantity", itemQuantity);
                        merelyExpiredItemMap.put("placedBy",placedBy);
                        merelyExpiredItemMap.put("itemPosition", itemPosition);
                        merelyExpiredItemMap.put("days", days);
                        merelyExpiredItemMap.put("placedAt",containerName);
                        FirebaseDatabase.getInstance().getReference()
                                .child("users").child(createdBy).child("fridges").child(fridgeKey)
                                .child("merelyExpiredItems").child(getRef(holder.getAdapterPosition()).getKey()).updateChildren(merelyExpiredItemMap);
                    }

                }else{
                    daysBetween = 0;
                    holder.cvItem.setCardBackgroundColor(colorSafe);
                }

                // calculate how many days the item had been placed in fridge
                String[] storedDateComp = itemStoredDate.split("-");
                int dayPlaced = Integer.valueOf(storedDateComp[0]);
                int monthPlaced = Integer.valueOf(storedDateComp[1]);
                int yearPlaced = Integer.valueOf(storedDateComp[2]);
                LocalDate placedDate = LocalDate.of(yearPlaced, monthPlaced, dayPlaced);
                int daysPlacedBetween = (int) ChronoUnit.DAYS.between(placedDate, currentDate);

                // update item days info
                HashMap daysBetweenMap = new HashMap();
                daysBetweenMap.put("days",daysBetween);
                daysBetweenMap.put("daysPlaced",daysPlacedBetween);
                FirebaseDatabase.getInstance().getReference()
                        .child("users").child(currentUserId).child("fridges").child(fridgeKey).child(containerType).child(containerKey)
                        .child("items").child(getRef(holder.getAdapterPosition()).getKey()).updateChildren(daysBetweenMap);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // display data in the item card view
        holder.cardTvItemName.setText(model.getItemName());
        Picasso.get().load(model.getItemImgUri()).into(holder.cIvItemImg);

        holder.cvItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showItemInfoPopup(holder, view, model);
            }
        });

    }

    private void showItemInfoPopup(ItemAdapter.myViewHolder holder, View v, Item model) {
        itemDialog = new Dialog(v.getContext());
        itemDialog.setContentView(R.layout.iteminfopopup);

        tvItemName = (TextView) itemDialog.findViewById(R.id.tvItemName);
        tvPlacedBy = (TextView) itemDialog.findViewById(R.id.tvPlacedBy);
        tvAvailableDay = (TextView) itemDialog.findViewById(R.id.tvAvailableDay);
        tvAvailableDayBanner = (TextView) itemDialog.findViewById(R.id.tvAvailableDayBanner);
        tvItemPosition = (TextView) itemDialog.findViewById(R.id.tvItemPosition);
        tvItemQuantity = (TextView) itemDialog.findViewById(R.id.tvItemQuantity);
        llItemInfo = (LinearLayout) itemDialog.findViewById(R.id.llItemInfo);
        civItemImg = (CircleImageView) itemDialog.findViewById(R.id.civItemImg);
        tvItemName.setText(model.getItemName());
        String placedByUserID = model.getPlacedBy();
        FirebaseDatabase.getInstance().getReference().child("users").child(placedByUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("username")){
                    placedByUsername = snapshot.child("username").getValue().toString();
                    tvPlacedBy.setText(placedByUsername);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        tvItemPosition.setText(model.getItemPosition());
        tvItemQuantity.setText(String.valueOf(model.getItemQuantity()));
        Picasso.get().load(model.getItemImgUri()).into(civItemImg);
        if (!containerType.equals("freezers")){
            tvAvailableDay.setText(String.valueOf(model.getDays()) + " days");
            int days = model.getDays();
            if (days<=1){
                llItemInfo.setBackgroundColor(colorAlert);
            }
            else if (days>1 && days<=7){
                llItemInfo.setBackgroundColor(colorModerate);
            }
            else if(days>7){
                llItemInfo.setBackgroundColor(colorSafe);
            }
        }else{
            tvAvailableDayBanner.setText("Days Placed:");
            tvAvailableDay.setText(String.valueOf(model.getDaysPlaced())+" days");
            llItemInfo.setBackgroundColor(colorSafe);
        }

        imgBtnItemInfoClosePopOut = (ImageButton) itemDialog.findViewById(R.id.imgBtnItemInfoClosePopOut);
        imgBtnItemInfoClosePopOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemDialog.dismiss();
            }
        });

        btnEditItem = (CircleButton) itemDialog.findViewById(R.id.btnEditItem);
        btnEditItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                String curItemId = getRef(holder.getAdapterPosition()).getKey();
                Intent intent = new Intent(view.getContext(), EditItemActivity.class);
                intent.putExtra("fridgeKey", fridgeKey);
                intent.putExtra("containerType", containerType);
                intent.putExtra("containerKey", containerKey);
                intent.putExtra("createdBy", createdBy);
                intent.putExtra("curItemId", curItemId);
                itemDialog.dismiss();
                view.getContext().startActivity(intent);

            }
        });

        btnConsumeItem = (CircleButton) itemDialog.findViewById(R.id.btnConsumeItem);
        btnConsumeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Take out this item?");
                builder.setMessage("One single item would be consumed.");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // if user choose to consume the item then find current item quantity and -1
                        int curItemQuantity = model.getItemQuantity();
                        int remainingItemQuantity = curItemQuantity - 1;
                        Log.d("Remaining item quantity", "onClick: "+remainingItemQuantity);
                        // if remaining item quantity more than 0 than update current item quantity in database
                        if (remainingItemQuantity > 0){
                            FirebaseDatabase.getInstance().getReference()
                                    .child("users").child(createdBy).child("fridges").child(fridgeKey).child(containerType).child(containerKey)
                                    .child("items").child(getRef(holder.getAdapterPosition()).getKey()).child("itemQuantity")
                                    .setValue(remainingItemQuantity).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isComplete()){
                                        Toast.makeText(view.getContext(), "One "+model.getItemName()+" has been taken out.", Toast.LENGTH_SHORT).show();
                                        // check if the consumer is host or not
                                        // if not then inform the host about item consumption
                                        String placedBy = model.getPlacedBy();
                                        if (!currentUserId.equals(placedBy)){
                                            FirebaseDatabase.getInstance().getReference().child("users").child(placedBy).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.hasChild("tokenID")){
                                                        String hostTokenID = snapshot.child("tokenID").getValue().toString();
                                                        FCMSend.pushNotification(view.getContext(), hostTokenID, "Item Consumption", currentUsername+" has consumed one of your "+model.getItemName());
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(view.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else{
                            // remove the item from system when quantity less than 0
                            FirebaseDatabase.getInstance().getReference()
                                    .child("users").child(createdBy).child("fridges").child(fridgeKey).child(containerType).child(containerKey)
                                    .child("items").child(getRef(holder.getAdapterPosition()).getKey()).removeValue();
                            //remove respective item in expired item list
                            FirebaseDatabase.getInstance().getReference()
                                    .child("users").child(createdBy).child("fridges").child(fridgeKey).child("expiredItems")
                                    .child(getRef(holder.getAdapterPosition()).getKey()).removeValue();

                            //remove respective item in merely expired item list
                            FirebaseDatabase.getInstance().getReference()
                                    .child("users").child(createdBy).child("fridges").child(fridgeKey).child("merelyExpiredItems")
                                    .child(getRef(holder.getAdapterPosition()).getKey()).removeValue();
                            Toast.makeText(view.getContext(), "No more "+ model.getItemName()+ " left.", Toast.LENGTH_SHORT).show();
                        }

                        itemDialog.dismiss();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(view.getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
        });

        btnDeleteItem = (CircleButton) itemDialog.findViewById(R.id.btnDeleteItem);
        btnDeleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Are you sure?");
                builder.setMessage("Deleted item cannot be recovered");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // remove item in respective fridge
                        FirebaseDatabase.getInstance().getReference()
                                .child("users").child(createdBy).child("fridges").child(fridgeKey).child(containerType).child(containerKey)
                                .child("items").child(getRef(holder.getAdapterPosition()).getKey()).removeValue();

                        //remove respective item in expired item list
                        FirebaseDatabase.getInstance().getReference()
                                .child("users").child(createdBy).child("fridges").child(fridgeKey).child("expiredItems")
                                .child(getRef(holder.getAdapterPosition()).getKey()).removeValue();

                        //remove respective item in merely expired item list
                        FirebaseDatabase.getInstance().getReference()
                                .child("users").child(createdBy).child("fridges").child(fridgeKey).child("merelyExpiredItems")
                                .child(getRef(holder.getAdapterPosition()).getKey()).removeValue();
                        Toast.makeText(view.getContext(), model.getItemName()+ " has been deleted.", Toast.LENGTH_SHORT).show();
                        itemDialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(view.getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
        });

        itemDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        itemDialog.show();

    }


    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cardview_2, parent, false);
        return new myViewHolder(view);
    }


    class myViewHolder extends RecyclerView.ViewHolder{

        private CardView cvItem;
        private CircleImageView cIvItemImg;
        private TextView cardTvItemName;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            cvItem = (CardView) itemView.findViewById(R.id.cvItem);

            cIvItemImg =(CircleImageView) itemView.findViewById(R.id.cardCIvItemImage);
            cardTvItemName = (TextView) itemView.findViewById(R.id.cardTvItemName);

        }
    }
}
