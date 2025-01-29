package com.links.freud;

import static android.app.PendingIntent.getActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Personal_area extends AppCompatActivity {

    private EditText updateN, updateP, updatePsw, updatePsos1, updatePsos2, updatePsos3;
    private Button btnNext;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ImageView avatarP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_personal_area);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // אתחול הרכיבים
        updateN = findViewById(R.id.updateN);
        updateP = findViewById(R.id.updateP);
        updatePsw = findViewById(R.id.updatePsw);
        updatePsos1 = findViewById(R.id.updatePsos1);
        updatePsos2 = findViewById(R.id.updatePsos2);
        updatePsos3 = findViewById(R.id.updatePsos3);
        avatarP = findViewById(R.id.avatarP);
        btnNext = findViewById(R.id.btnNext);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());
            loadUserDetails();
        }

//        mAuth = FirebaseAuth.getInstance();
//        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        if (mAuth.getCurrentUser() != null && !mAuth.getCurrentUser().isAnonymous()) {

            String userId = mAuth.getCurrentUser().getUid();

            Log.d("shneor", "id" + userId);

            fetchAvatar(userId);

        }

        avatarP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    Intent intent = new Intent(Personal_area.this, select_avatar.class);
                    startActivity(intent);
                }
        });

        // לחיצה על כפתור "שמור" לשמירת הנתונים החדשים
        btnNext.setOnClickListener(v -> saveUserDetails());

//        BottomNavigationFragment bottomNavFragment = new BottomNavigationFragment();
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.bottomNavFragment, bottomNavFragment)
//                .commit();
//
//        findViewById(R.id.main).post(() -> setSelectedButtonInNav());
//
//        setSelectedButtonInNav();
    }
    private void loadUserDetails() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                // טוען את הערכים הקיימים ומציג אותם בשדות
                String name = dataSnapshot.child("name").getValue(String.class);
                String phone = dataSnapshot.child("phone").getValue(String.class);
                String password = dataSnapshot.child("password").getValue(String.class);
                String phone1sos = dataSnapshot.child("phone1sos").getValue(String.class);
                String phone2sos = dataSnapshot.child("phone2sos").getValue(String.class);
                String phone3sos = dataSnapshot.child("phone3sos").getValue(String.class);

                    updateN.setText(name != null ? name : "");
                    updateP.setText(phone != null ? phone : "");
                    updatePsw.setText(phone != null ? password : "");
                    updatePsos1.setText(phone1sos != null ? phone1sos : "");
                    updatePsos2.setText(phone2sos != null ? phone2sos : "");
                    updatePsos3.setText(phone3sos != null ? phone3sos : "");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("PersonalArea13", "Failed to load user data.", databaseError.toException());
            }
        });
    }

    // פונקציה לשמירת פרטי המשתמש ב-Firebase
    private void saveUserDetails() {
        String name = updateN.getText().toString();
        String phone = updateP.getText().toString();
        String password = updatePsw.getText().toString();
        String phone1sos = updatePsos1.getText().toString();
        String phone2sos = updatePsos2.getText().toString();
        String phone3sos = updatePsos3.getText().toString();

        if (phone.length() != 10 || phone1sos.length() != 10 || phone2sos.length() != 10) {
            Toast.makeText(Personal_area.this, getString(R.string.phoneMin10), Toast.LENGTH_SHORT).show();
            return;
        }

        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name).build();

        FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileChangeRequest)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Profile Update", "User profile updated.");
                    }
                });

        // שמירת הנתונים ב-Firebase
        mDatabase.child("name").setValue(name);
        mDatabase.child("phone").setValue(phone);
        mDatabase.child("password").setValue(password);
        mDatabase.child("phone1sos").setValue(phone1sos);
        mDatabase.child("phone2sos").setValue(phone2sos);
        mDatabase.child("phone3sos").setValue(phone3sos)

         .addOnSuccessListener(aVoid -> Toast.makeText(Personal_area.this, getString(R.string.updatePS), Toast.LENGTH_SHORT).show());

         Intent intent = new Intent(Personal_area.this, HomePage.class);

         intent.putExtra("name", name);
           startActivity(intent);

    }

    private void setSelectedButtonInNav() {
        getSupportFragmentManager().executePendingTransactions(); // וודא שכל הפעולות של ה-Fragment מסתיימות לפני שממשיכים
        BottomNavigationFragment bottomNavFragment = (BottomNavigationFragment) getSupportFragmentManager().findFragmentById(R.id.bottomNavFragment);
        if (bottomNavFragment != null) {
            bottomNavFragment.updateSelectedButton(R.id.rulerPersonalA);  // ID של כפתור הבית
        } else {
            Log.d("shneor", "Fragment is not initialized yet");
        }
    }

    private void fetchAvatar(String userId) {
        mDatabase.child("avatar").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int avatarResourceId = dataSnapshot.getValue(Integer.class);
                    // Set the image to the ImageView
                    avatarP.setImageResource(avatarResourceId);
                    Log.d("shneor", "Avatar resource ID: " + avatarResourceId);
                } else {
                    Toast.makeText(Personal_area.this, "No avatar found for this user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("shneor", "Error fetching avatar", databaseError.toException());
            }
        });
    }

}