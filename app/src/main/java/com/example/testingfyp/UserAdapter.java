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

public class UserAdapter extends FirebaseRecyclerAdapter<User, UserAdapter.myViewHolder> {

    private CircleImageView civPopOutProfileImg;
    private TextView tvPopOutUsername, tvPopOutEmail;
    private ImageButton imgBtnClosePopout;
    private Button btnSendFriendRequest, btnDeclineFriendRequest;

    private String currentUID, targetUserID, saveCurrentDate, targetTokenID, currentUsername;
    private String CURRENT_STATUS;

    private DatabaseReference friendRequestRef, friendsRef;

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
                showUserPopOut(v, model, holder.getAdapterPosition());
            }
        });
    }

    private void showUserPopOut(View v, User model, int position){
        final Dialog userDialog = new Dialog(v.getContext());
        userDialog.setContentView(R.layout.userinfopopout);
        civPopOutProfileImg = userDialog.findViewById(R.id.civPopOutProfileImg);
        tvPopOutUsername = userDialog.findViewById(R.id.tvPopOutUsername);
        tvPopOutEmail = userDialog.findViewById(R.id.tvPopOutEmail);
        imgBtnClosePopout = userDialog.findViewById(R.id.imgBtnClosePopout);
        btnSendFriendRequest = userDialog.findViewById(R.id.btnSendFriendRequest);
        btnDeclineFriendRequest = userDialog.findViewById(R.id.btnDeclineFriendRequest);
        btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
        btnDeclineFriendRequest.setEnabled(false);

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("friend requests");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("friends");

        // check if the target user is same as the current login user
        // if yes then do not show the add friend options
        // if no then provide add friend request option
        currentUID = FirebaseAuth.getInstance().getUid();
        targetUserID = getRef(position).getKey();

        // obtain the target device token id
        FirebaseDatabase.getInstance().getReference().child("users").child(targetUserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
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
        FirebaseDatabase.getInstance().getReference().child("users").child(currentUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
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

        CURRENT_STATUS = "not_friend";

        Picasso.get().load(model.getProfileImgUri()).into(civPopOutProfileImg);
        tvPopOutUsername.setText(model.getUsername());
        tvPopOutEmail.setText(model.getEmail());

        imgBtnClosePopout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userDialog.dismiss();
            }
        });

        if (!currentUID.equals(targetUserID)){
            btnSendFriendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnSendFriendRequest.setEnabled(false);
                    if(CURRENT_STATUS.equals("not_friend")){
                        SendFriendRequestToPerson(v);
                    }
                    if (CURRENT_STATUS.equals("request_sent")){
                        CancelFriendRequest(v);
                    }
                    if (CURRENT_STATUS.equals("request_received")){
                        AcceptFriendRequest(v);
                    }
                    if (CURRENT_STATUS.equals("friends")){
                        UnfriendExistingFriend(v);
                    }
                }
            });
        }else{
            btnSendFriendRequest.setVisibility(View.INVISIBLE);
            btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
        }

        maintenanceOfButtons();

        userDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        userDialog.show();
    }

    private void UnfriendExistingFriend(View v) {
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
                                                btnSendFriendRequest.setEnabled(true);
                                                CURRENT_STATUS = "not_friend";
                                                btnSendFriendRequest.setText("Add");

                                                btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                btnDeclineFriendRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendRequest(View v) {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = dateFormat.format(calForDate.getTime());

        friendsRef.child(currentUID).child(targetUserID).child("date").setValue(saveCurrentDate)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        friendsRef.child(targetUserID).child(currentUID).child("date").setValue(saveCurrentDate)
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

                                                                                    // send notification to target user device
                                                                                    sendNotification("acceptFriendRequest", v);

                                                                                    btnSendFriendRequest.setEnabled(true);
                                                                                    CURRENT_STATUS = "friends";
                                                                                    btnSendFriendRequest.setText("Unfriend");

                                                                                    btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                                                    btnDeclineFriendRequest.setEnabled(false);
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

    private void CancelFriendRequest(View v) {
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
                                                btnSendFriendRequest.setEnabled(true);
                                                CURRENT_STATUS = "not_friend";
                                                btnSendFriendRequest.setText("Add");

                                                btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                btnDeclineFriendRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendFriendRequestToPerson(View v) {
        friendRequestRef.child(currentUID).child(targetUserID).child("request_type").setValue("sent")
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        friendRequestRef.child(targetUserID).child(currentUID).child("request_type").setValue("received")
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    // send notification to the target user device
                                    sendNotification("sendFriendRequest", v);

                                    btnSendFriendRequest.setEnabled(true);
                                    CURRENT_STATUS = "request_sent";
                                    btnSendFriendRequest.setText("Cancel");

                                    btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                    btnDeclineFriendRequest.setEnabled(false);
                                }
                            }
                        });
                    }
                }
            });
    }

    private void maintenanceOfButtons() {
        friendRequestRef.child(currentUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(targetUserID)){
                            String request_type = snapshot.child(targetUserID).child("request_type").getValue().toString();

                            if (request_type.equals("sent")){
                                CURRENT_STATUS = "request_sent";
                                btnSendFriendRequest.setText("Cancel");

                                btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                btnDeclineFriendRequest.setEnabled(false);
                            }
                            else if (request_type.equals("received")){
                                CURRENT_STATUS = "request_received";
                                btnSendFriendRequest.setText("Accept Request");

                                btnDeclineFriendRequest.setVisibility(View.VISIBLE);
                                btnDeclineFriendRequest.setEnabled(true);

                                btnDeclineFriendRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelFriendRequest(v);
                                        //send notification to the target user device
                                        sendNotification("declineFriendRequest", v);
                                    }
                                });
                            }
                        }
                        else {
                            friendsRef.child(currentUID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild(targetUserID)){
                                        CURRENT_STATUS = "friends";
                                        btnSendFriendRequest.setText("Unfriend");

                                        btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                        btnDeclineFriendRequest.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendNotification(String notificationType, View v) {
        if (notificationType.equals("sendFriendRequest")){
            String title = "Friend Request";
            String message = currentUsername + " has sent you a friend request";
            FCMSend.pushNotification(v.getContext(), targetTokenID, title, message);
        }
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
