package com.example.testingfyp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class ExpiredItemsAdapter extends FirebaseRecyclerAdapter<Item, ExpiredItemsAdapter.myViewHolder> {

    private String fridgeKey, createdBy, containerType;

    private FirebaseAuth mAuth;
    private String currentUserId;

    private int colorAlert;

    public ExpiredItemsAdapter(String fridgeKey, String createdBy, @NonNull FirebaseRecyclerOptions<Item> options) {
        super(options);
        this.fridgeKey = fridgeKey;
        this.createdBy = createdBy;
    }

    @Override
    protected void onBindViewHolder(@NonNull ExpiredItemsAdapter.myViewHolder holder, int position, @NonNull Item model) {

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        // obtain color code
        colorAlert = holder.cvItem.getResources().getColor(R.color.nearlyExpired);

        // display data in the item card view
        holder.cardTvItemName.setText(model.getItemName());
        holder.cardTvPlacedBy.setText(model.getPlacedBy());
        holder.cvItem.setCardBackgroundColor(colorAlert);
        holder.cardTvExpirationDate.setText(model.getItemExpirationDate());
        holder.cardTvItemAvailableDay.setText(String.valueOf(model.getDays()));
        Picasso.get().load(model.getItemImgUri()).into(holder.cIvItemImg);

        // get the item container type

        FirebaseDatabase.getInstance().getReference().child("users").child(createdBy).child("fridges").child(fridgeKey)
                .child("expiredItems").child(getRef(holder.getAdapterPosition()).getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                containerType = snapshot.child("containerType").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // click edit button to update item info
        // click edit button to proceed to edit item activity
        holder.btnEditItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String curItemId = getRef(holder.getAdapterPosition()).getKey();
                Intent intent = new Intent(v.getContext(), EditItemActivity.class);
                intent.putExtra("fridgeKey", fridgeKey);
                intent.putExtra("containerType", containerType);
                intent.putExtra("createdBy", createdBy);
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
                        // remove item in expired item list
                        FirebaseDatabase.getInstance().getReference()
                                .child("users").child(createdBy).child("fridges").child(fridgeKey)
                                .child("expiredItems").child(getRef(holder.getAdapterPosition()).getKey()).removeValue();
                        // remove item in fridge container
                        FirebaseDatabase.getInstance().getReference()
                                .child("users").child(createdBy).child("fridges").child(fridgeKey)
                                .child(containerType).child("items").child(getRef(holder.getAdapterPosition()).getKey()).removeValue();
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


    @Override
    public ExpiredItemsAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cardview, parent, false);
        return new ExpiredItemsAdapter.myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{

        private CardView cvItem;
        private CircleImageView cIvItemImg;
        private TextView cardTvItemName, cardTvPlacedBy, cardTvExpirationDate, cardTvItemAvailableDay;
        private Button btnEditItem, btnDeleteItem;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            cvItem = (CardView) itemView.findViewById(R.id.cvItem);

            cIvItemImg =(CircleImageView) itemView.findViewById(R.id.cardCIvItemImage);
            cardTvItemName = (TextView) itemView.findViewById(R.id.cardTvItemName);
            cardTvPlacedBy = (TextView) itemView.findViewById(R.id.cardTvPlacedBy);
            cardTvExpirationDate = (TextView) itemView.findViewById(R.id.cardTvExpirationDate);

            btnEditItem = (Button) itemView.findViewById(R.id.btnEditItem);
            btnDeleteItem = (Button) itemView.findViewById(R.id.btnDeleteItem);
            cardTvItemAvailableDay = (TextView) itemView.findViewById(R.id.cardTvItemAvailableDay);

        }
    }
}
