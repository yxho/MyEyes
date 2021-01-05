package com.yxh.myeyes;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private String TAG = "MyEyes";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button face_button = findViewById(R.id.face_button);
        face_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(MainActivity.this,PreviewViewActivity.class);
               String ability = "FaceRecognition";
               intent.putExtra("ability",ability);
               startActivity(intent);
            }
        });

    }


}