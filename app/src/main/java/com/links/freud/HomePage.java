package com.links.freud;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class HomePage extends AppCompatActivity {

    private TextView helloMsg;

    private ImageButton action1, action2, action3, action4, action5, action6, action7, action8, age3Button, ageYungButton, ageOtherButton;

    private String message;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    private boolean isHomePageOpen;

    private String selectedAgeGroup;

    private boolean isOld = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        SharedPreferences prefs = getSharedPreferences("com.example.freud", MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean("isFirstTimeHome", true);

        Intent intent2 = getIntent();
        String keyValue = intent2.getStringExtra("key");

        isHomePageOpen = true;

        if
        (isFirstTime) {

            Intent intent = new Intent(HomePage.this, SOSGuideActivity.class);
            startActivity(intent);

            prefs.edit().putBoolean("isFirstTimeHome", false).apply();
        }

        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkAndRequestSmsPermission();

        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        helloMsg = findViewById(R.id.homeH);

        mAuth = FirebaseAuth.getInstance();

        String currentLanguage = getResources().getConfiguration().getLocales().get(0).getLanguage();

        Log.d("shneor1", currentLanguage);

        if (mAuth.getCurrentUser() != null && !(mAuth.getCurrentUser().isAnonymous())) {
            String name2 = mAuth.getCurrentUser().getDisplayName();

            Intent intent = getIntent();
            String namee = intent.getStringExtra("name");

            if (namee != null) {
                helloMsg.setText(getString(R.string.Hello) + " " + namee);
            } else {
                helloMsg.setText(getString(R.string.Hello) + " " + name2);
            }

        } else {
            helloMsg.setText(getString(R.string.Hello));
        }

        if ("first".equals(keyValue)) {
            showAgeDialog();
        } else {
            loadAgeFromFirebase();
        }

        BottomNavigationFragment bottomNavFragment = new BottomNavigationFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottomNavFragment, bottomNavFragment)
                .commit();

        findViewById(R.id.main).post(() -> setSelectedButtonInNav());

        setSelectedButtonInNav();

        action1 = findViewById(R.id.action1);
        action2 = findViewById(R.id.action2);
        action3 = findViewById(R.id.action3);
        action4 = findViewById(R.id.action4);
        action5 = findViewById(R.id.action5);
        action6 = findViewById(R.id.action6);
        action7 = findViewById(R.id.action7);
        action8 = findViewById(R.id.action8);

        if (currentLanguage.equals("iw")) {

            action1.setImageResource(R.drawable.depression);
            action2.setImageResource(R.drawable.boycott);
            action3.setImageResource(R.drawable.anxieties);
            action4.setImageResource(R.drawable.ptsd);
            action5.setImageResource(R.drawable.selfconfidence);
            action6.setImageResource(R.drawable.divorce);
            action7.setImageResource(R.drawable.october);
            action8.setImageResource(R.drawable.familyproblems);
        } else {

            action1.setImageResource(R.drawable.depressionen);
            action2.setImageResource(R.drawable.boycotten);
            action3.setImageResource(R.drawable.anxietiesen);
            action4.setImageResource(R.drawable.ptsden);
            action5.setImageResource(R.drawable.selfconfidenceen);
            action6.setImageResource(R.drawable.divorceen);
            action7.setImageResource(R.drawable.octoberen);
            action8.setImageResource(R.drawable.familyproblemsen);
        }

        action1.setOnClickListener(v -> {
            message = getString(R.string.feelingDepression);
            setupButtonWithMessage(message);
        });

        action2.setOnClickListener(v -> {
            message = getString(R.string.feelingSocialIsolation);
            setupButtonWithMessage(message);
        });

        action3.setOnClickListener(v -> {
            message = getString(R.string.experiencingAnxiety);
            setupButtonWithMessage(message);
        });

        action4.setOnClickListener(v -> {
            message = getString(R.string.feelingSad);
            setupButtonWithMessage(message);
        });

        action5.setOnClickListener(v -> {
            message = getString(R.string.selfConfidenceIssues);
            setupButtonWithMessage(message);
        });

        action6.setOnClickListener(v -> {
            message = getString(R.string.dealingWithDivorce);
            setupButtonWithMessage(message);
        });

        action7.setOnClickListener(v -> {
            message = getString(R.string.needHelp);
            setupButtonWithMessage(message);
        });

        action8.setOnClickListener(v -> {
            message = getString(R.string.familyIssues);
            setupButtonWithMessage(message);
        });
    }

    private void setupButtonWithMessage(String message) {
        Intent intent = new Intent(HomePage.this, Chat.class);
        Intent intent1 = intent.putExtra("message", message);
        intent.putExtra("isOld", isOld);
        intent.putExtra("age", selectedAgeGroup);
        startActivity(intent);
    }

    private void loadUserData(String userId) {
        // כאן תוכל לטעון את הנתונים מ-Firebase על בסיס ה-userId
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users").child(userId);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);

                    helloMsg.setText(getString(R.string.Hello) + name);
                } else {
//                    Toast.makeText(HomePage.this, "נתוני המשתמש לא נמצאו", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("HomePage", "Failed to load user data.", databaseError.toException());
            }
        });
    }

    private void setSelectedButtonInNav() {
        getSupportFragmentManager().executePendingTransactions();
        BottomNavigationFragment bottomNavFragment = (BottomNavigationFragment) getSupportFragmentManager().findFragmentById(R.id.bottomNavFragment);
        if (bottomNavFragment != null) {
            bottomNavFragment.updateSelectedButton(R.id.rulerH);
        } else {
            Log.d("test", "Fragment is not initialized yet");
        }
    }

    private void showAgeDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.age_dialog);

        String currentLanguage = getResources().getConfiguration().getLocales().get(0).getLanguage();

        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        age3Button = dialog.findViewById(R.id.age3);
        ageYungButton = dialog.findViewById(R.id.age_yung);
        ageOtherButton = dialog.findViewById(R.id.age_other);

        if (currentLanguage.equals("iw")) {

            age3Button.setImageResource(R.drawable.age3);
            ageYungButton.setImageResource(R.drawable.age_yung);
            ageOtherButton.setImageResource(R.drawable.age_other2);
        } else {

            age3Button.setImageResource(R.drawable.age3en);
            ageYungButton.setImageResource(R.drawable.age_yungen);
            ageOtherButton.setImageResource(R.drawable.age_otheren);
        }

        age3Button.setOnClickListener(v -> {
            selectedAgeGroup = "the third age";
            isOld = true;
            saveAgeToFirebase(selectedAgeGroup, isOld);
            dialog.dismiss();
        });

        ageYungButton.setOnClickListener(v -> {
            selectedAgeGroup = "Teenagers";
            isOld = false;
            saveAgeToFirebase(selectedAgeGroup, isOld);
            dialog.dismiss();
            Log.d("shneor", selectedAgeGroup + isOld);
        });

        ageOtherButton.setOnClickListener(v -> {
            selectedAgeGroup = "regular";
            isOld = false;
            saveAgeToFirebase(selectedAgeGroup, isOld);
            dialog.dismiss();
            Log.d("shneor", selectedAgeGroup + isOld);
        });

        dialog.show();
    }

    private void saveAgeToFirebase(String ageGroup, boolean isOld) {
        String userId = mAuth.getCurrentUser().getUid();

        DatabaseReference userRef = mDatabase.child(userId);
        userRef.child("selectedAgeGroup").setValue(ageGroup);
        userRef.child("isOld").setValue(isOld);
    }

    public void checkAndRequestSmsPermission() {
        String[] permissions = {
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.SEND_SMS,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        // רשימה להחזיק את ההרשאות שעדיין לא ניתנו
        List<String> permissionsNeeded = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        // אם יש הרשאות שעדיין לא ניתנו, נבקש אותן
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    100); // ניתן לשנות את הקוד כאן ל-REQUEST_CODE הרצוי לך
        }
    }

    private void loadAgeFromFirebase() {
        String userId = mAuth.getCurrentUser().getUid(); // מזהה המשתמש הנוכחי

        DatabaseReference userRef = mDatabase.child(userId); // הפנייה למידע של המשתמש
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // אם המידע קיים ב-Firebase
                    String savedAgeGroup = dataSnapshot.child("selectedAgeGroup").getValue(String.class);
                    Boolean savedIsOld = dataSnapshot.child("isOld").getValue(Boolean.class);

                    // הצבת הערכים במשתנים
                    selectedAgeGroup = savedAgeGroup != null ? savedAgeGroup : "regular";
                    isOld = savedIsOld != null ? savedIsOld : false;

                    // המשך עבודה עם הנתונים (למשל הצגת הודעת ברוך הבא)
                    Log.d("HomePage", "Age group loaded: " + selectedAgeGroup + ", Is old: " + isOld);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("HomePage", "Failed to load age data.", databaseError.toException());
            }
        });
    }

}