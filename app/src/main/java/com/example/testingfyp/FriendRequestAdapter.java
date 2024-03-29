package com.example.testingfyp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestAdapter extends FirebaseRecyclerAdapter<FriendRequest, FriendRequestAdapter.myViewHolder> {

    private DatabaseReference userRef, friendRequestRef, friendsRef;
    private String currentUID, targetUserID, currentUsername, targetTokenID, saveCurrentDate;

    public FriendRequestAdapter(@NonNull FirebaseRecyclerOptions<FriendRequest> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FriendRequestAdapter.myViewHolder holder, int position, @NonNull FriendRequest model) {
        targetUserID = getRef(holder.getAdapterPosition()).getKey();
        currentUID = FirebaseAuth.getInstance().getUid();

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("friend requests");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("friends");

        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(targetUserID);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String username = snapshot.child("username").getValue().toString();
                    String profileImgUri = snapshot.child("profileImgUri").getValue().toString();

                    holder.cardTvFriendRequestUsername.setText(username);
                    Picasso.get().load(profileImgUri).into(holder.cardCIvFriendRequestProfileImg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // obtain the target device tokenID
        FirebaseDatabase.getInstance().getReference().child("users").child(targetUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("tokenID")){
                    targetTokenID = snapshot.child("tokenID").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // obtain current username
        FirebaseDatabase.getInstance().getReference().child("users").child(currentUID).addListenerForSingleValueEvent(new ValueEventListener() {
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

        holder.imgBtnAcceptFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calForDate = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                saveCurrentDate = dateFormat.format(calForDate.getTime());

                friendsRef.child(currentUID).child(getRef(holder.getAdapterPosition()).getKey()).child("date").setValue(saveCurrentDate)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    friendsRef.child(getRef(holder.getAdapterPosition()).getKey()).child(currentUID).child("date").setValue(saveCurrentDate)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        CancelFriendRequest(holder.getAdapterPosition());
                                                        // send notification to target user device
                                                        sendNotification("acceptFriendRequest", v);
                                                    }
                                                }
                                            });
                                }
                            }
                        });

            }
        });

        holder.imgBtnRejectFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userID = getRef(holder.getAdapterPosition()).getKey();
                friendRequestRef.child(currentUID).child(userID).removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    friendRequestRef.child(userID).child(currentUID).removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        FriendRequestAdapter.this.notifyDataSetChanged();
                                                        // send notification to target user device
                                                        sendNotification("declineFriendRequest", v);
                                                    }
                                                }
                                            });
                                }
                            }
                        });
            }
        });
    }

    private void sendNotification(String notificationType, View v) {
        if (notificationType.equals("acceptFriendRequest")){
            String title = "Friend Request Accepted";
            String message = currentUsername + " has accepted your friend request";
            FCMSend.pushNotification(v.getContext(), targetTokenID, title, message);
        }
        if (notificationType.equals("declineFriendRequest")){
            String title = "Friend Request Rejected";
            String message = currentUsername + " has declined your friend request";
            FCMSend.pushNotification(v.getContext(), targetTokenID, title, message);
        }
    }

    private void CancelFriendRequest(int adapterPosition) {
        String userID = getRef(adapterPosition).getKey();
        friendRequestRef.child(currentUID).child(userID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestRef.child(userID).child(currentUID).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        FriendRequestAdapter.this.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void AcceptFriendRequest(View v, int position) {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = dateFormat.format(calForDate.getTime());
        String userID = getRef(position).getKey();

        friendsRef.child(currentUID).child(userID).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendsRef.child(userID).child(currentUID).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                friendRequestRef.child(currentUID).child(targetUserID).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    friendRequestRef.child(targetUserID).child(currentUID).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){
                                                                                        FriendRequestAdapter.this.notifyDataSetChanged();
                                                                                        Toast.makeText(v.getContext(), "Accepted friend request", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    @Override
    public FriendRequestAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friendrequest_cardview, parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{

        CircleImageView cardCIvFriendRequestProfileImg;
        TextView cardTvFriendRequestUsername;
        ImageButton imgBtnAcceptFriend, imgBtnRejectFriend;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCIvFriendRequestProfileImg = itemView.findViewById(R.id.cardCIvFriendRequestProfileImg);
            cardTvFriendRequestUsername = itemView.findViewById(R.id.cardTvFriendRequestUsername);
            imgBtnAcceptFriend = itemView.findViewById(R.id.imgBtnAcceptFriend);
            imgBtnRejectFriend = itemView.findViewById(R.id.imgBtnRejectFriend);
        }
    }
}
