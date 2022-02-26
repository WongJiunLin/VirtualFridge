package com.example.testingfyp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MerelyExpiredItemsAdapter extends FirebaseRecyclerAdapter<Item, MerelyExpiredItemsAdapter.myViewHolder> {


    public MerelyExpiredItemsAdapter(@NonNull FirebaseRecyclerOptions<Item> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull MerelyExpiredItemsAdapter.myViewHolder holder, int position, @NonNull Item model) {
        holder.cardTvMerelyExpiredItemName.setText(model.getItemName());
        holder.cardTvMerelyExpiredDays.setText(String.valueOf(model.getDays())+"days");
        Picasso.get().load(model.getItemImgUri()).into(holder.cardCIvMerelyExpiredItemImg);
    }

    @Override
    public MerelyExpiredItemsAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.merelyexpireditem_cardview, parent, false);
        return new MerelyExpiredItemsAdapter.myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{

        CircleImageView cardCIvMerelyExpiredItemImg;
        TextView cardTvMerelyExpiredItemName, cardTvMerelyExpiredDays;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            cardCIvMerelyExpiredItemImg = (CircleImageView) itemView.findViewById(R.id.cardCIvMerelyExpiredItemImg);
            cardTvMerelyExpiredItemName = (TextView) itemView.findViewById(R.id.cardTvMerelyExpiredItemName);
            cardTvMerelyExpiredDays = (TextView) itemView.findViewById(R.id.cardTvMerelyExpiredDays);

        }
    }
}
