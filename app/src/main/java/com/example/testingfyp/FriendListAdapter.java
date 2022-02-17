package com.example.testingfyp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendListAdapter extends FirebaseRecyclerAdapter<Friend, FriendListAdapter.myViewHolder> {

    private DatabaseReference userRef, friendsRef;

    private CircleImageView civPopOutProfileImg;
    private TextView tvPopOutUsername, tvPopOutEmail;
    private ImageButton imgBtnClosePopout;
    private Button btnSendFriendRequest, btnDeclineFriendRequest;

    private String currentUID, targetUserID;

    public FriendListAdapter(@NonNull FirebaseRecyclerOptions<Friend> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FriendListAdapter.myViewHolder holder, int position, @NonNull Friend model) {
        final String userIDs = getRef(position).getKey();

        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userIDs);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    final String username = snapshot.child("username").getValue().toString();
                    final String profileImgUri = snapshot.child("profileImgUri").getValue().toString();

                    Picasso.get().load(profileImgUri).into(holder.cardCIvUserProfileImg);
                    holder.cardTvUsername.setText(username);
                    holder.cardTvStatus.setText("Friend since "+ model.getDate());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.cvFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFriendPopOut(v, model, position);
            }
        });
    }

    private void showFriendPopOut(View v, Friend model, int position) {
        final Dialog userDialog = new Dialog(v.getContext());
        userDialog.setContentView(R.layout.userinfopopout);
        civPopOutProfileImg = userDialog.findViewById(R.id.civPopOutProfileImg);
        tvPopOutUsername = userDialog.findViewById(R.id.tvPopOutUsername);
        tvPopOutEmail = userDialog.findViewById(R.id.tvPopOutEmail);
        imgBtnClosePopout = userDialog.findViewById(R.id.imgBtnClosePopout);
        btnSendFriendRequest = userDialog.findViewById(R.id.btnSendFriendRequest);
        btnSendFriendRequest.setText("Unfriend");
        btnDeclineFriendRequest = userDialog.findViewById(R.id.btnDeclineFriendRequest);
        btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
        btnDeclineFriendRequest.setEnabled(false);

        friendsRef = FirebaseDatabase.getInstance().getReference().child("friends");

        currentUID = FirebaseAuth.getInstance().getUid();
        targetUserID = getRef(position).getKey();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    final String username = snapshot.child("username").getValue().toString();
                    final String profileImgUri = snapshot.child("profileImgUri").getValue().toString();
                    final String email = snapshot.child("email").getValue().toString();

                    Picasso.get().load(profileImgUri).into(civPopOutProfileImg);
                    tvPopOutUsername.setText(username);
                    tvPopOutEmail.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // if user click on unfriend then perform unfriend options
        btnSendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Confirm to unfriend this user?");
                builder.setMessage("User would not exist in friend list anymore.");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UnFriendExistingUser(v);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });

        imgBtnClosePopout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDialog.dismiss();
            }
        });



        userDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        userDialog.show();
    }

    private void UnFriendExistingUser(View v) {
        friendsRef.child(currentUID).child(targetUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendsRef.child(targetUserID).child(currentUID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(v.getContext(), "Successfully removed user from friend list",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    @Override
    public FriendListAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_cardview, parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder {
        CircleImageView cardCIvUserProfileImg;
        TextView cardTvUsername, cardTvStatus;
        CardView cvFriend;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            cardCIvUserProfileImg = (CircleImageView) itemView.findViewById(R.id.cardCIvUserProfileImg);
            cardTvUsername = (TextView) itemView.findViewById(R.id.cardTvUsername);
            cardTvStatus = (TextView) itemView.findViewById(R.id.cardTvStatus);
            cvFriend = (CardView) itemView.findViewById(R.id.cvFriend);

        }
    }
}
