package com.example.testingfyp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    private String fridgeKey, containerType;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String currentUserId = mAuth.getUid();

    private String itemExpirationDate;
    private int colorAlert, colorModerate, colorSafe;

    public ItemAdapter(String fridgeKey, String containerType, @NonNull FirebaseRecyclerOptions<Item> options) {
        super(options);
        this.fridgeKey = fridgeKey;
        this.containerType = containerType;
    }

    @Override
    protected void onBindViewHolder(@NonNull ItemAdapter.myViewHolder holder, int position, @NonNull Item model) {

        // current Item
        String currentItemId = getRef(position).getKey();

        // obtain color code
        colorAlert = holder.cvItem.getResources().getColor(R.color.nearlyExpired);
        colorModerate = holder.cvItem.getResources().getColor(R.color.moderateExpired);
        colorSafe = holder.cvItem.getResources().getColor(R.color.safeExpired);

        FirebaseDatabase.getInstance().getReference()
                .child("users").child(currentUserId).child("fridges").child(fridgeKey).child(containerType)
                .child("items").child(currentItemId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // get the item info
                String itemName = snapshot.child("itemName").getValue().toString();
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
                        .child("users").child(currentUserId).child("fridges").child(fridgeKey).child(containerType)
                        .child("items").child(getRef(position).getKey()).updateChildren(daysBetweenMap);

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
                    expiredItemMap.put("itemExpirationDate",itemExpirationDate);
                    expiredItemMap.put("itemStoredDate",itemStoredDate);
                    expiredItemMap.put("itemImgUri",itemImgUri);
                    expiredItemMap.put("containerType",containerType);
                    expiredItemMap.put("days",days);
                    FirebaseDatabase.getInstance().getReference()
                            .child("users").child(currentUserId).child("fridges").child(fridgeKey)
                            .child("expiredItems").child(getRef(position).getKey()).updateChildren(expiredItemMap);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // display data in the item card view
        holder.cardTvItemName.setText(model.getItemName());
        holder.cardTvExpirationDate.setText(model.getItemExpirationDate());
        holder.cardTvItemAvailableDay.setText(String.valueOf(model.getDays()));
        Picasso.get().load(model.getItemImgUri()).into(holder.cIvItemImg);

        // click edit button to proceed to edit item activity
        holder.btnEditItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String curItemId = getRef(position).getKey();
                Intent intent = new Intent(v.getContext(), EditItemActivity.class);
                intent.putExtra("fridgeKey", fridgeKey);
                intent.putExtra("containerType", containerType);
                intent.putExtra("curItemId", curItemId);
                v.getContext().startActivity(intent);
            }
        });

        // click delete button to remove item
        holder.btnDeleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.cardTvItemName.getContext());
                builder.setTitle("Are you sure?");
                builder.setMessage("Deleted item cannot be recovered");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // remove item in respective fridge
                        FirebaseDatabase.getInstance().getReference()
                                .child("users").child(currentUserId).child("fridges").child(fridgeKey).child(containerType)
                                .child("items").child(getRef(position).getKey()).removeValue();

                        //remove respective item in expired item list
                        FirebaseDatabase.getInstance().getReference()
                                .child("users").child(currentUserId).child("fridges").child(fridgeKey).child("expiredItems")
                                .child(getRef(position).getKey()).removeValue();

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(holder.cardTvItemName.getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
        });
    }


    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cardview, parent, false);
        return new myViewHolder(view);
    }


    class myViewHolder extends RecyclerView.ViewHolder{

        private CardView cvItem;
        private CircleImageView cIvItemImg;
        private TextView cardTvItemName, cardTvExpirationDate, cardTvItemAvailableDay;
        private Button btnEditItem, btnDeleteItem;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            cvItem = (CardView) itemView.findViewById(R.id.cvItem);

            cIvItemImg =(CircleImageView) itemView.findViewById(R.id.cardCIvItemImage);
            cardTvItemName = (TextView) itemView.findViewById(R.id.cardTvItemName);
            cardTvExpirationDate = (TextView) itemView.findViewById(R.id.cardTvExpirationDate);

            btnEditItem = (Button) itemView.findViewById(R.id.btnEditItem);
            btnDeleteItem = (Button) itemView.findViewById(R.id.btnDeleteItem);
            cardTvItemAvailableDay = (TextView) itemView.findViewById(R.id.cardTvItemAvailableDay);

        }
    }
}
