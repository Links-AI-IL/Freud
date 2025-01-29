package com.links.freud;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Contact_us extends AppCompatActivity {

    private static final String SENDGRID_API_KEY = "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";

    private static final String SENDGRID_URL = "https://api.sendgrid.com/v3/mail/send";

    private RadioButton feedback1, feedback2, feedback3, feedback4;

    private EditText name, email, message;

    private Button sendM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_us);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        name = findViewById(R.id.nameC);
        email = findViewById(R.id.emailC);
        message = findViewById(R.id.messageC);
        sendM = findViewById(R.id.btnSend);

        feedback1 = findViewById(R.id.feedback1);
        feedback2 = findViewById(R.id.feedback2);
        feedback3 = findViewById(R.id.feedback3);
        feedback4 = findViewById(R.id.feedback4);

        // מאזיני לחיצה לכל כפתור
        feedback1.setOnClickListener(radioButtonClickListener);
        feedback2.setOnClickListener(radioButtonClickListener);
        feedback3.setOnClickListener(radioButtonClickListener);
        feedback4.setOnClickListener(radioButtonClickListener);

        BottomNavigationFragment bottomNavFragment = new BottomNavigationFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottomNavFragment, bottomNavFragment)
                .commit();

        findViewById(R.id.main).post(() -> setSelectedButtonInNav());

        setSelectedButtonInNav();

        sendM.setOnClickListener(view -> {
            String name1 = name.getText().toString();
            String email1 = email.getText().toString();
            String message1 = message.getText().toString();

            String feedback = "";
            boolean isFeedbackSelected = false;

            if (feedback1.isChecked()) {
                feedback = "במידה מועטה";
                isFeedbackSelected = true;
            } else if (feedback2.isChecked()) {
                feedback = "במידה בינונית";
                isFeedbackSelected = true;
            } else if (feedback3.isChecked()) {
                feedback = "במידה רבה";
                isFeedbackSelected = true;
            } else if (feedback4.isChecked()) {
                feedback = "מאוד שימושית";
                isFeedbackSelected = true;
            }
            if (!isFeedbackSelected) {
                // אם אף כפתור לא נבחר, הצג הודעה וחזור
                Toast.makeText(Contact_us.this, getString(R.string.TChooseOne), Toast.LENGTH_SHORT).show();
                return;
            }

            if (name1.isEmpty() || email1.isEmpty() || message1.isEmpty()) {
                Toast.makeText(Contact_us.this, getString(R.string.TFillAl), Toast.LENGTH_SHORT).show();
            } else {
                sendEmailUsingSendGrid(name1, email1, message1, feedback);
            }
        });
    }

    private View.OnClickListener radioButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // איפוס הבחירה בכל הכפתורים לפני הגדרת הכפתור שנבחר
            feedback1.setChecked(false);
            feedback2.setChecked(false);
            feedback3.setChecked(false);
            feedback4.setChecked(false);

            // סימון הכפתור שנלחץ
            ((RadioButton) v).setChecked(true);
        }
    };

    private void setSelectedButtonInNav() {
        getSupportFragmentManager().executePendingTransactions();
        BottomNavigationFragment bottomNavFragment = (BottomNavigationFragment) getSupportFragmentManager().findFragmentById(R.id.bottomNavFragment);

        if (bottomNavFragment != null) {
            bottomNavFragment.updateSelectedButton(R.id.rulerPhone);

        } else {
            Log.d("shneor", "Fragment is not initialized yet");
        }
    }

    private void sendEmailUsingSendGrid(String name, String email, String message, String feedback) {
        OkHttpClient client = new OkHttpClient();

        // Create the email content in JSON
        String jsonBody = "{"
                + "\"personalizations\": [{\"to\": [{\"email\": \"freud@links-workplace.com\"}]}],"
                + "\"from\": {\"email\": \"shani@links-workplace.com\"},"
                + "\"subject\": \"User Comments from " + name + "\","
                + "\"content\": [{\"type\": \"text/plain\", \"value\": \"Name: " + name
                + "\\nEmail: " + email + "\\nComments: " + feedback + "\\nMessage: " + message + "\"}]"
                + "}";

        Log.d("JSON Body", jsonBody);

        // Create the request body
        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
        // Create the request
        Request request = new Request.Builder()
                .url(SENDGRID_URL)
                .addHeader("Authorization", "Bearer " + SENDGRID_API_KEY) // Ensure the API key is correct
                .addHeader("Content-Type", "application/json") // Ensure content-type is set correctly
                .post(body)
                .build();

        // Make the HTTP request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(Contact_us.this, getString(R.string.TFail) + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("test1", "test1");
                    e.printStackTrace(); // Log the exception
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(Contact_us.this, getString(R.string.TThanksF), Toast.LENGTH_SHORT).show());
                    Intent intent = new Intent(Contact_us.this, HomePage.class);
                    startActivity(intent);
                }
                else {
                    String responseBody = response.body().string(); // Log the response body for debugging
                    runOnUiThread(() -> {
                        Toast.makeText(Contact_us.this, getString(R.string.TFail) + responseBody, Toast.LENGTH_LONG).show();
                        Log.e("SendGrid Error Response", responseBody);
                        System.out.println("Error Response: " + responseBody);
                    });
                }
            }
        });
    }
}