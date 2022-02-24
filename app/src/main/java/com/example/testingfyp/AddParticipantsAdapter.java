package com.example.testingfyp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.circularreveal.CircularRevealLinearLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddParticipantsAdapter extends FirebaseRecyclerAdapter<Participant, AddParticipantsAdapter.myViewHolder> {

    private String fridgeKey;
    private DatabaseReference userRef, participantsRef;

    private String currentUID, targetUserID;

    public AddParticipantsAdapter(String fridgeKey, @NonNull FirebaseRecyclerOptions<Participant> options) {
        super(options);
        this.fridgeKey = fridgeKey;
    }

    @Override
    protected void onBindViewHolder(@NonNull AddParticipantsAdapter.myViewHolder holder, int position, @NonNull Participant model) {
        targetUserID = getRef(position).getKey();
        currentUID = FirebaseAuth.getInstance().getUid();

        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(targetUserID);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String username = snapshot.child("username").getValue().toString();
                    String profileImgUri = snapshot.child("profileImgUri").getValue().toString();
                    String email = snapshot.child("email").getValue().toString();

                    Picasso.get().load(profileImgUri).into(holder.cardCIvUserProfileImg);
                    holder.cardTvUsername.setText(username);
                    holder.cardTvStatus.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        participantsRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUID).child("fridges").child(fridgeKey).child("participants");
        participantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(getRef(position).getKey())){
                    holder.btnAddParticipant.setVisibility(View.VISIBLE);
                    holder.btnAddParticipant.setEnabled(true);
                }else{
                    holder.btnAddParticipant.setEnabled(false);
                    //holder.tvParticipantExisted.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.btnAddParticipant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String option = holder.btnAddParticipant.getText().toString().toLowerCase();
                if (option.equals("add")){
                    addTargetUserToFridge(v, position, holder);
                }

            }
        });
    }

    private void addTargetUserToFridge(View v, int position, @NonNull AddParticipantsAdapter.myViewHolder holder) {
        FirebaseDatabase.getInstance().getReference().child("users").child(currentUID).child("fridges").child(fridgeKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String fridgeName = snapshot.child("fridgeName").getValue().toString();
                            String fridgeCreatedDate = snapshot.child("fridgeCreatedDate").getValue().toString();
                            String createdBy = snapshot.child("createdBy").getValue().toString();

                            HashMap fridgeMap = new HashMap();
                            fridgeMap.put("fridgeName", fridgeName);
                            fridgeMap.put("fridgeCreatedDate", fridgeCreatedDate);
                            fridgeMap.put("createdBy", createdBy);

                            FirebaseDatabase.getInstance().getReference().child("users").child(getRef(position).getKey()).child("fridges").child(fridgeKey)
                                    .updateChildren(fridgeMap).addOnSuccessListener(new OnSuccessListener() {
                                @Override
                                public void onSuccess(Object o) {
                                    // assign role to target user
                                    participantsRef.child(getRef(position).getKey()).child("role").setValue("participant").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(v.getContext(), "Successfully added user into current fridge.", Toast.LENGTH_SHORT).show();
                                                AddParticipantsAdapter.this.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public AddParticipantsAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_cardview, parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{
        CircleImageView cardCIvUserProfileImg;
        TextView cardTvUsername, cardTvStatus, tvParticipantExisted;
        Button btnAddParticipant;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            cardCIvUserProfileImg = (CircleImageView) itemView.findViewById(R.id.cardCIvUserProfileImg);
            cardTvUsername = (TextView) itemView.findViewById(R.id.cardTvUsername);
            cardTvStatus = (TextView) itemView.findViewById(R.id.cardTvStatus);
            btnAddParticipant = (Button) itemView.findViewById(R.id.btnAddParticipant);
            tvParticipantExisted = (TextView) itemView.findViewById(R.id.tvParticipantExisted);

        }
    }
}
