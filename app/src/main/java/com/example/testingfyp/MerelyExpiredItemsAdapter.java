package com.example.testingfyp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import at.markushi.ui.CircleButton;
import de.hdodenhof.circleimageview.CircleImageView;

public class MerelyExpiredItemsAdapter extends FirebaseRecyclerAdapter<Item, MerelyExpiredItemsAdapter.myViewHolder> {

    private Dialog expiredItemDialog;
    private TextView tvItemName, tvPlacedBy, tvAvailableDay, tvItemPosition, tvItemQuantity, tvContainerName;
    private CircleImageView civItemImg;
    private ImageButton imgBtnItemInfoClosePopOut;
    private LinearLayout llItemInfo;

    private int colorModerate;

    public MerelyExpiredItemsAdapter(@NonNull FirebaseRecyclerOptions<Item> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull MerelyExpiredItemsAdapter.myViewHolder holder, int position, @NonNull Item model) {
        holder.cardTvMerelyExpiredItemName.setText(model.getItemName());
        holder.cardTvMerelyExpiredDays.setText(String.valueOf(model.getDays())+"days");
        Picasso.get().load(model.getItemImgUri()).into(holder.cardCIvMerelyExpiredItemImg);

        // define color code
        colorModerate = holder.cvItem.getResources().getColor(R.color.moderateExpired);

        // while press on the expired item display the item info
        holder.cvItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExpiredItemInfoPopup(holder, model, view);
            }
        });
    }

    private void showExpiredItemInfoPopup(myViewHolder holder, Item model, View view) {
        expiredItemDialog = new Dialog(view.getContext());
        expiredItemDialog.setContentView(R.layout.expirediteminfopopup);

        civItemImg = (CircleImageView) expiredItemDialog.findViewById(R.id.civItemImg);
        Picasso.get().load(model.getItemImgUri()).into(civItemImg);
        tvItemName = (TextView) expiredItemDialog.findViewById(R.id.tvItemName);
        tvItemName.setText(model.getItemName());
        tvPlacedBy = (TextView) expiredItemDialog.findViewById(R.id.tvPlacedBy);
        tvPlacedBy.setText(model.getPlacedBy());
        tvAvailableDay = (TextView) expiredItemDialog.findViewById(R.id.tvAvailableDay);
        tvAvailableDay.setText(String.valueOf(model.getDays())+" days");
        tvItemPosition = (TextView) expiredItemDialog.findViewById(R.id.tvItemPosition);
        tvItemPosition.setText(model.getItemPosition());
        tvItemQuantity = (TextView) expiredItemDialog.findViewById(R.id.tvItemQuantity);
        tvItemQuantity.setText(String.valueOf(model.getItemQuantity()));
        tvContainerName = (TextView) expiredItemDialog.findViewById(R.id.tvContainerName);
        tvContainerName.setText(model.getPlacedAt());
        llItemInfo = (LinearLayout) expiredItemDialog.findViewById(R.id.llItemInfo);
        llItemInfo.setBackgroundColor(colorModerate);

        imgBtnItemInfoClosePopOut = (ImageButton) expiredItemDialog.findViewById(R.id.imgBtnItemInfoClosePopOut);
        imgBtnItemInfoClosePopOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expiredItemDialog.dismiss();
            }
        });

        expiredItemDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        expiredItemDialog.show();
    }

    @Override
    public MerelyExpiredItemsAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.merelyexpireditem_cardview, parent, false);
        return new MerelyExpiredItemsAdapter.myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{

        CircleImageView cardCIvMerelyExpiredItemImg;
        TextView cardTvMerelyExpiredItemName, cardTvMerelyExpiredDays;
        CardView cvItem;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            cardCIvMerelyExpiredItemImg = (CircleImageView) itemView.findViewById(R.id.cardCIvMerelyExpiredItemImg);
            cardTvMerelyExpiredItemName = (TextView) itemView.findViewById(R.id.cardTvMerelyExpiredItemName);
            cardTvMerelyExpiredDays = (TextView) itemView.findViewById(R.id.cardTvMerelyExpiredDays);
            cvItem = (CardView) itemView.findViewById(R.id.cvItem);

        }
    }
}
