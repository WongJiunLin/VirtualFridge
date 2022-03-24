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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class ItemAdapter extends FirebaseRecyclerAdapter<Item, ItemAdapter.myViewHolder> {

    private String fridgeKey, containerType, containerKey, createdBy;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String currentUserId = mAuth.getUid();

    private String currentItemId;
    private int colorAlert, colorModerate, colorSafe;

    private Dialog itemDialog;
    private TextView tvItemName, tvPlacedBy, tvAvailableDay, tvItemPosition;
    private CircleImageView civItemImg;
    private ImageButton imgBtnItemInfoClosePopOut;
    private LinearLayout llItemInfo;
    private Button btnEditItem, btnDeleteItem;

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

        // obtain color code
        colorAlert = holder.cvItem.getResources().getColor(R.color.nearlyExpired);
        colorModerate = holder.cvItem.getResources().getColor(R.color.moderateExpired);
        colorSafe = holder.cvItem.getResources().getColor(R.color.safeExpired);

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


                String itemExpirationDate = snapshot.child("itemExpirationDate").getValue().toString();

                String[] expiryDateComp = itemExpirationDate.split("-");
                int day = Integer.valueOf(expiryDateComp[0]);
                int month = Integer.valueOf(expiryDateComp[1]);
                int year = Integer.valueOf(expiryDateComp[2]);

                LocalDate currentDate = LocalDate.now();
                LocalDate expirationDate = LocalDate.of(year,month,day);
                int daysBetween = (int) ChronoUnit.DAYS.between(currentDate,expirationDate);

                HashMap daysBetweenMap = new HashMap();
                daysBetweenMap.put("days",daysBetween);
                FirebaseDatabase.getInstance().getReference()
                        .child("users").child(currentUserId).child("fridges").child(fridgeKey).child(containerType).child(containerKey)
                        .child("items").child(getRef(holder.getAdapterPosition()).getKey()).updateChildren(daysBetweenMap);

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
                    expiredItemMap.put("containerType",containerType);
                    expiredItemMap.put("days",days);
                    FirebaseDatabase.getInstance().getReference()
                            .child("users").child(createdBy).child("fridges").child(fridgeKey)
                            .child("expiredItems").child(getRef(holder.getAdapterPosition()).getKey()).updateChildren(expiredItemMap);

                }else if (days <= 3 && days >= 1){
                    HashMap merelyExpiredItemMap = new HashMap();
                    merelyExpiredItemMap.put("itemName", itemName);
                    merelyExpiredItemMap.put("itemImgUri",itemImgUri);
                    merelyExpiredItemMap.put("days", days);
                    FirebaseDatabase.getInstance().getReference()
                            .child("users").child(createdBy).child("fridges").child(fridgeKey)
                            .child("merelyExpiredItems").child(getRef(holder.getAdapterPosition()).getKey()).updateChildren(merelyExpiredItemMap);
                }

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
        tvItemPosition = (TextView) itemDialog.findViewById(R.id.tvItemPosition);
        llItemInfo = (LinearLayout) itemDialog.findViewById(R.id.llItemInfo);
        civItemImg = (CircleImageView) itemDialog.findViewById(R.id.civItemImg);

        tvItemName.setText(model.getItemName());
        tvPlacedBy.setText(model.getPlacedBy());
        tvAvailableDay.setText(String.valueOf(model.getDays()) + " days");
        tvItemPosition.setText(model.getItemPosition());
        Picasso.get().load(model.getItemImgUri()).into(civItemImg);

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

        imgBtnItemInfoClosePopOut = (ImageButton) itemDialog.findViewById(R.id.imgBtnItemInfoClosePopOut);
        imgBtnItemInfoClosePopOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemDialog.dismiss();
            }
        });

        btnEditItem = (Button) itemDialog.findViewById(R.id.btnEditItem);
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

        btnDeleteItem = (Button) itemDialog.findViewById(R.id.btnDeleteItem);
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
