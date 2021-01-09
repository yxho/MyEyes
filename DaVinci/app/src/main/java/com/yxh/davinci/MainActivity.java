package com.yxh.davinci;

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
        Button mFaceButton = findViewById(R.id.face_button);
        Button mImageButton = findViewById(R.id.image_button);
        Button mArtButton = findViewById(R.id.art_button);
        mFaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PreviewViewActivity.class);
                String ability = "FaceRecognition";
                intent.putExtra("ability", ability);
                startActivity(intent);
            }
        });

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ImageProcessActivity.class);
                String ability = "FaceRecognition";
                intent.putExtra("ability", ability);
                startActivity(intent);
            }
        });

        mArtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PixelArtActivity.class);
                String ability = "FaceRecognition";
                intent.putExtra("ability", ability);
                startActivity(intent);
            }
        });
    }
}