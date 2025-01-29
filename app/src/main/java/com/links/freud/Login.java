package com.links.freud;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.text.method.LinkMovementMethod;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class Login extends AppCompatActivity {

    private DatabaseReference mDatabase;
    Button Login;
    EditText email, password;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    TextView resetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Login = findViewById(R.id.btnLogin);
        email = findViewById(R.id.inputEmail);
        password = findViewById(R.id.inputPassword);
        resetPassword = findViewById(R.id.forgot_password);

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loginUser(email.getText().toString(), password.getText().toString());

            }
        });

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, ForgetPassword.class));
            }
        });


        // button to register
        TextView textView = findViewById(R.id.registerInLoginPage);

        String fullText = getString(R.string.newHere);
        String clickablePart = getString(R.string.newHere2);

        SpannableStringBuilder spannableString = new SpannableStringBuilder(fullText);

        int startIndex = fullText.indexOf(clickablePart);
        int endIndex = startIndex + clickablePart.length();

        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(Login.this, Security.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull android.text.TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
            }
                , startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void loginUser(String email, String password) {

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(Login.this, getString(R.string.TFillAl), Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    Log.d("shani", "try loggin1");
                    if (task.isSuccessful()) {
                     // Login successful
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        Log.d("shani", "try loggin3" + user.getPhoneNumber());

                        Toast.makeText(Login.this, getString(R.string.welcome2), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login.this, HomePage.class);

                        startActivity(intent);
                    }
                    else {
                        // If sign-in fails, display a message to the user.
                        Log.w("Login", "signInWithEmail:failure", task.getException());
                        Toast.makeText(Login.this, getString(R.string.somthingW), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}



