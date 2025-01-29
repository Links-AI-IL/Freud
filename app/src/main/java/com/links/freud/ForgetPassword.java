package com.links.freud;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class ForgetPassword extends AppCompatActivity {

    private EditText inputEmailFP;
    private Button btnResetP;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forget_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        // אתחול רכיבי UI
        inputEmailFP = findViewById(R.id.inputEmailFP);
        btnResetP = findViewById(R.id.btnResetP);

        // לחיצה על כפתור איפוס הסיסמא
        btnResetP.setOnClickListener(v -> {
            String email = inputEmailFP.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(ForgetPassword.this, getString(R.string.TEEmail), Toast.LENGTH_SHORT).show();
                return;
            }

            resetPassword(email);
        });
    }

    // פונקציה לאיפוס סיסמא
    private void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // הצלחה בשליחת אימייל לאיפוס סיסמא
                        Toast.makeText(ForgetPassword.this, getString(R.string.TSEmail), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ForgetPassword.this, Login.class));
                    } else {
                        // טיפול בשגיאות
                        Log.e("ResetPassword1", "Error: " + task.getException().getMessage());
                        Toast.makeText(ForgetPassword.this, getString(R.string.TSEmail), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}