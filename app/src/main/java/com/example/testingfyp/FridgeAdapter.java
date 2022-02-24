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
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class FridgeAdapter extends FirebaseRecyclerAdapter<Fridge, FridgeAdapter.myViewHolder> {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String currentUserId = mAuth.getUid();

    public FridgeAdapter(@NonNull FirebaseRecyclerOptions<Fridge> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FridgeAdapter.myViewHolder holder, int position, @NonNull Fridge model) {
        String fridgeName = model.getFridgeName();
        String fridgeCreatedDate = model.getFridgeCreatedDate();
        String createdBy = model.getCreatedBy();

        holder.tvFridgeName.setText(fridgeName);
        holder.tvFridgeCreatedDate.setText(fridgeCreatedDate);

        //click on enter button
        holder.btnEnterFridge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fridgeKey = getRef(position).getKey();
                Intent intent = new Intent(v.getContext(),FridgeActivity.class);
                intent.putExtra("fridgeName",fridgeName);
                intent.putExtra("fridgeKey",fridgeKey);
                intent.putExtra("createdBy",createdBy);
                v.getContext().startActivity(intent);
            }
        });
        //click on delete button
        holder.btnDeleteFridge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.tvFridgeName.getContext());
                builder.setTitle("Are you sure?");
                builder.setMessage("Deleted fridge cannot be recovered");

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // if host deleted the fridge, then remove related fridge from all participants and host.
                        // if participants deleted the fridge then remove current fridge from participants only and quit the fridge.
                        if (createdBy.equals(currentUserId)){
                            // remove related fridge from other participants
                            FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId)
                                    .child("fridges").child(getRef(position).getKey()).child("participants").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        for (DataSnapshot ds : snapshot.getChildren()){
                                            String participantId = ds.getKey();
                                            FirebaseDatabase.getInstance().getReference().child("users").child(participantId)
                                                    .child("fridges").child(getRef(position).getKey()).removeValue();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            // remove fridge from host
                            FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId)
                                    .child("fridges").child(getRef(position).getKey()).removeValue();
                            Toast.makeText(v.getContext(), "Successfully deleted "+fridgeName, Toast.LENGTH_SHORT).show();
                            FridgeAdapter.this.notifyDataSetChanged();
                        } else {
                            // remove fridge from participants
                            FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId)
                                    .child("fridges").child(getRef(position).getKey()).removeValue();
                            // remove the participants details from host.
                            FirebaseDatabase.getInstance().getReference().child("users").child(createdBy)
                                    .child("fridges").child(getRef(position).getKey())
                                    .child("participants").child(currentUserId).removeValue();
                            Toast.makeText(v.getContext(), "Successfully deleted "+fridgeName+" from your side", Toast.LENGTH_SHORT).show();
                            FridgeAdapter.this.notifyDataSetChanged();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(holder.tvFridgeName.getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
        });
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fridge_cardview, parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{

        TextView tvFridgeCreatedDate, tvFridgeName;
        Button btnEnterFridge, btnDeleteFridge;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            tvFridgeCreatedDate = (TextView) itemView.findViewById(R.id.tvFridgeCreatedDate);
            tvFridgeName = (TextView) itemView.findViewById(R.id.tvFridgeName);

            btnEnterFridge = (Button) itemView.findViewById(R.id.btnEnterFridge);
            btnDeleteFridge = (Button) itemView.findViewById(R.id.btnDeleteFridge);

        }
    }

}

