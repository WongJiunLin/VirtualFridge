package com.example.testingfyp;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ExpiredItemsAdapter extends FirebaseRecyclerAdapter<Item, ExpiredItemsAdapter.myViewHolder> {

    private String fridgeKey;

    private FirebaseAuth mAuth;
    private String currentUserId;

    private int colorAlert;

    public ExpiredItemsAdapter(String fridgeKey, @NonNull FirebaseRecyclerOptions<Item> options) {
        super(options);
        this.fridgeKey = fridgeKey;
    }

    @Override
    protected void onBindViewHolder(@NonNull ExpiredItemsAdapter.myViewHolder holder, int position, @NonNull Item model) {

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getUid();

        // obtain color code
        colorAlert = holder.cvItem.getResources().getColor(R.color.nearlyExpired);

        // display data in the item card view
        holder.cardTvItemName.setText(model.getItemName());
        holder.cvItem.setCardBackgroundColor(colorAlert);
        holder.cardTvExpirationDate.setText(model.getItemExpirationDate());
        holder.cardTvItemAvailableDay.setText(String.valueOf(model.getDays()));
        Picasso.get().load(model.getItemImgUri()).into(holder.cIvItemImg);

        // click edit button to update item info
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
                                .child("users").child(currentUserId).child("fridges").child(fridgeKey)
                                .child("expiredItems").child(getRef(position).getKey()).removeValue();
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
