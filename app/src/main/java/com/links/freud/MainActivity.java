package com.links.freud;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    private ImageView imageLogo2;

    private Button languageHeText, languageEnText, btnLogin, btnRegister, btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setDefaultLocale();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        String currentLanguage = getResources().getConfiguration().getLocales().get(0).getLanguage();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        languageHeText = findViewById(R.id.languageHeText);

        languageEnText = findViewById(R.id.languageEnText);

        languageEnText.setOnClickListener(view -> setLocale("en", true)); // אנגלית

        languageHeText.setOnClickListener(view -> setLocale("iw", true)); // עברית

        checkAppVersion();

        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,Login.class);
                startActivity(intent);
            }
        });

        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this,Security.class);
            startActivity(intent);
        });

        btnNext = findViewById(R.id.btnGuest);

        btnNext.setOnClickListener(view -> {
            loginAnonymously();
        });

        ImageView imageLogo = findViewById(R.id.imageView2);

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

    }
    private void loginAnonymously() {
        mAuth.signInAnonymously().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // הכניסה האנונימית הצליחה
                Log.d("Auth", "signInAnonymously:success");
                FirebaseUser user = mAuth.getCurrentUser();
                // כעת תוכל להוביל את המשתמש לעמוד הבית או לדף אישי
                Intent intent = new Intent(MainActivity.this,HomePage.class);
                intent.putExtra("name", "");
                startActivity(intent);

                Intent intent2 = new Intent(MainActivity.this, HomePage.class);
                intent2.putExtra("key", "first");
                startActivity(intent2);
            } else {
                // אם הכניסה האנונימית נכשלה
                Log.w("Auth", "signInAnonymously:failure", task.getException());
                Toast.makeText(MainActivity.this, getString(R.string.anonimusF), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAppVersion() {
        mDatabase.child("appVersion").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String latestVersion = dataSnapshot.getValue(String.class);
                    String currentVersion = BuildConfig.VERSION_NAME;  // גרסה נוכחית של האפליקציה

                    if (!currentVersion.equals(latestVersion)) {
                        Intent intent = new Intent(MainActivity.this,Update.class);
                        startActivity(intent);                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // טיפול בשגיאה במקרה של בעיה בבדיקת הנתונים
            }
        });
    }

    private void setDefaultLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String savedLanguage = prefs.getString("My_Lang", ""); // בדוק אם נשמרה שפה

        // תמיד הגדר עברית כברירת מחדל ושמור אותה אם לא נשמרה שפה
        if (savedLanguage.isEmpty()) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("My_Lang", "iw"); // ברירת מחדל לעברית
            editor.apply();
            setLocale("iw", false); // הגדר עברית כברירת מחדל
        } else {
            applySavedLanguage(); // החל את השפה שנשמרה
        }
    }



    private void setLocale(String langCode, boolean shouldRestart) {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String currentLanguage = prefs.getString("My_Lang", "iw");  // ברירת מחדל לעברית

        // החלף שפה רק אם היא שונה מהשפה הנוכחית
        if (!currentLanguage.equals(langCode)) {
            // שמור את השפה החדשה
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("My_Lang", langCode);
            editor.apply();

            // הגדר את השפה החדשה
            Locale locale = new Locale(langCode);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.setLocale(locale);

            getResources().updateConfiguration(config, getResources().getDisplayMetrics());

            // אתחל מחדש את הפעילות אם נדרש
            if (shouldRestart) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                startActivity(intent);
            }
        }
    }

    private void applySavedLanguage() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String savedLanguage = prefs.getString("My_Lang", "iw"); // ברירת מחדל לעברית
        Locale locale = new Locale(savedLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

}