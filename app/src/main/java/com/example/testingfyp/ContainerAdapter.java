package com.example.testingfyp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

public class ContainerAdapter extends FirebaseRecyclerAdapter<Container, ContainerAdapter.myViewHolder> {

    private String fridgeKey, containerType, createdBy;
    private MenuBuilder menuBuilder;
    public ContainerAdapter(String fridgeKey, String containerType, String createdBy, @NonNull FirebaseRecyclerOptions<Container> options) {
        super(options);
        this.fridgeKey = fridgeKey;
        this.containerType = containerType;
        this.createdBy = createdBy;
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull Container model) {
        String containerName = model.getContainerName();
        holder.tvContainerName.setText(containerName);

        if (containerType.equals("freezers")){
            holder.ivContainer.setImageResource(R.drawable.icon_fridge_freezer);
        }else if (containerType.equals("drawers")){
            holder.ivContainer.setImageResource(R.drawable.icon_fridge_drawer);
        }else if (containerType.equals("shelves")){
            holder.ivContainer.setImageResource(R.drawable.icon_fridge_shelf);
        }

        // if user press enter then forward to specific container for item viewing
        holder.cvContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String containerKey = getRef(holder.getAdapterPosition()).getKey();
                Intent intent = new Intent(view.getContext(), ItemActivity.class);
                intent.putExtra("createdBy", createdBy);
                intent.putExtra("fridgeKey", fridgeKey);
                intent.putExtra("containerName", containerName);
                intent.putExtra("containerType",containerType);
                intent.putExtra("containerKey", containerKey);
                view.getContext().startActivity(intent);
            }
        });

        // while user long click the card, display the delete option
        holder.cvContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                PopupMenu popupMenu = new PopupMenu(view.getContext(), holder.cvContainer);
                popupMenu.inflate(R.menu.container_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.delete:
                                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                                builder.setTitle("Are you sure?");
                                builder.setMessage("Deleted container cannot be recovered.");

                                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // remove the selected container from database
                                        FirebaseDatabase.getInstance().getReference().child("users").child(createdBy)
                                                .child("fridges").child(fridgeKey).child(containerType)
                                                .child(getRef(holder.getAdapterPosition()).getKey()).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isComplete()){
                                                            Toast.makeText(view.getContext(), "Successfully deleted.", Toast.LENGTH_SHORT).show();
                                                            ContainerAdapter.this.notifyDataSetChanged();
                                                        }
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(view.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(holder.tvContainerName.getContext(), "Cancelled.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                builder.show();
                        }
                        return false;
                    }
                });
                popupMenu.show();
                return false;
            }
        });
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.container_cardview, parent, false);
        return new myViewHolder(view);
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        TextView tvContainerName;
        CardView cvContainer;
        ImageView ivContainer;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContainerName = (TextView) itemView.findViewById(R.id.tvContainerName);
            cvContainer = (CardView) itemView.findViewById(R.id.cvContainer);
            ivContainer = (ImageView) itemView.findViewById(R.id.ivContainer);

        }
    }
}
