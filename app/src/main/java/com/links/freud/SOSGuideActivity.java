package com.links.freud;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SOSGuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_guide); // נשתמש בפריסת XML בשם activity_sos_guide

        // מציאת הכפתור לסגירת מסך ההסבר
        Button closeGuideButton = findViewById(R.id.closeGuideButton);
        closeGuideButton.setOnClickListener(view -> {
            // מעבר ל-HomePage אחרי סגירת ההסבר
            Intent intent = new Intent(SOSGuideActivity.this, HomePage.class);
            startActivity(intent);
            finish();
        });
    }
}

