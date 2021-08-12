package com.example.testingfyp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Position extends AppCompatActivity {

    private Button confirmItemPositionButton;
    private TextView block;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position);

        confirmItemPositionButton = findViewById(R.id.confirmItemPositionButton);
        confirmItemPositionButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Position.this, AddItemActivity.class);
                startActivity(intent);
            }
        });

    }

    public void changeColor(View view) {
        if(view.getBackground().getConstantState()==getResources().getDrawable(R.drawable.available_bg).getConstantState()) {
            view.setBackgroundResource(R.drawable.tick_bg);
        }
        else {
            view.setBackgroundResource(R.drawable.available_bg);
        }
    }

    public void returnToTemplate(View view) {
        Intent intent = new Intent(Position.this, template1.class);
        startActivity(intent);
    }
}