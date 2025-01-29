package com.links.freud;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class Security extends AppCompatActivity {

    private ImageView imageLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_security);

        String currentLanguage = getResources().getConfiguration().getLocales().get(0).getLanguage();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView imageLogo = findViewById(R.id.imageLogo);

        Locale currentLocale = getResources().getConfiguration().locale;

        Log.d("shneor", "Current language: " + currentLanguage);
        if (currentLanguage.equals("iw")) {
            Log.d("shneor", "Current language: " + currentLanguage);
            // Change the button image for Hebrew
            imageLogo.setImageResource(R.drawable.logohebrew); // Use your Hebrew image resource here
        } else {
            Log.d("shneor", "Current language: " + currentLanguage);
            // Default image
            imageLogo.setImageResource(R.drawable.logoen); // Use your default image resource here
        }

        Button btnNext = findViewById(R.id.btnNextSecurity);
        btnNext.setOnClickListener(view -> {
            Intent intent = new Intent(Security.this,Register.class);
            startActivity(intent);
        });
    }
}