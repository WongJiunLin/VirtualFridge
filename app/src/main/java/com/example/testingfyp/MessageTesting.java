package com.example.testingfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class MessageTesting extends AppCompatActivity {

    private Button btnToken;
    private TextView tvToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_testing);

        tvToken = findViewById(R.id.tvToken);
        btnToken = findViewById(R.id.btnToken);

        btnToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = "This is Vridggy";
                String message = "Hi this is notification";
                String token = "dOhELcYYTma1KolSWyg-P0:APA91bELidBtW0fmN1_sLeU1qDX4RZfpiTym3Mi5181Wuwm9ZQZvG_rzTOZ27YqDuwc_lkzLc3-nqAUtmDnI8y4rz2VLidN0ElxhA6v6VOBv12A3k31Paxeg01pY1ucZEHAIdv75W3EU";
                FCMSend.pushNotification(
                        MessageTesting.this,
                        token,
                        title,
                        message
                );
            }
        });

    }
}