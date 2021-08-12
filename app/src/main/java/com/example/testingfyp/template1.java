package com.example.testingfyp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class template1 extends AppCompatActivity {

    private TextView freezer;
    private EditText edtFridgeName;
    private Button chooseFridge;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template1);

        edtFridgeName = (EditText) findViewById(R.id.edtFridgeName);
        chooseFridge = (Button) findViewById(R.id.chooseTemplateButton);

        chooseFridge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fridgeName = edtFridgeName.getText().toString();
                Intent intent = new Intent(template1.this, HomeActivity.class);
                intent.putExtra("fridgeName",fridgeName);
                startActivity(intent);
            }
        });

    }

    public void chooseItemPosition(View view) {
        Intent intent = new Intent(template1.this,Position.class);
        startActivity(intent);
    }


}