package com.example.testingfyp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends FirebaseRecyclerAdapter<User, UserAdapter.myViewHolder> {

    public UserAdapter(@NonNull FirebaseRecyclerOptions<User> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull UserAdapter.myViewHolder holder, int position, @NonNull User model) {
        //display searched user info on the card view
        holder.cardTvUsername.setText(model.getUsername());
        Picasso.get().load(model.getProfileImgUri()).into(holder.cardCIvUserProfileImg);

        holder.cvUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserPopOut(v, model, position);
            }
        });
    }

    private void showUserPopOut(View v, User model, int position){
        final Dialog userDialog = new Dialog(v.getContext());
        userDialog.setContentView(R.layout.userinfopopout);
        CircleImageView civPopOutProfileImg = userDialog.findViewById(R.id.civPopOutProfileImg);
        TextView tvPopOutUsername = userDialog.findViewById(R.id.tvPopOutUsername);
        TextView tvPopOutEmail = userDialog.findViewById(R.id.tvPopOutEmail);
        ImageButton imgBtnClosePopout = userDialog.findViewById(R.id.imgBtnClosePopout);
        Button btnSendFriendRequest = userDialog.findViewById(R.id.btnSendFriendRequest);
        Button btnDeclineFriendRequest = userDialog.findViewById(R.id.btnDeclineFriendRequest);

        // check if the target user is same as the current login user
        // if yes then do not show the add friend options
        String currentUID = FirebaseAuth.getInstance().getUid();
        String targetUserID = getRef(position).getKey();

        if (currentUID.equals(targetUserID)){
            btnSendFriendRequest.setVisibility(View.INVISIBLE);
            btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
        }

        Picasso.get().load(model.getProfileImgUri()).into(civPopOutProfileImg);
        tvPopOutUsername.setText(model.getUsername());
        tvPopOutEmail.setText(model.getEmail());

        imgBtnClosePopout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDialog.dismiss();
            }
        });

        userDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        userDialog.show();
    }

    @Override
    public UserAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_cardview, parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView cardCIvUserProfileImg;
        private TextView cardTvUsername;
        private CardView cvUser;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCIvUserProfileImg = (CircleImageView) itemView.findViewById(R.id.cardCIvUserProfileImg);
            cardTvUsername = (TextView) itemView.findViewById(R.id.cardTvUsername);
            cvUser = (CardView) itemView.findViewById(R.id.cvUser);
        }
    }
}
