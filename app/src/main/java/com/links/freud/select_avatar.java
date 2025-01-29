package com.links.freud;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class select_avatar extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private int selectedAvatarUrl = 0;
    private ImageButton selectedAvatarButton;
    private ImageView checkmarkView;
//    private static final String KEY = "key";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_avatar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        checkmarkView = new ImageView(this);
        checkmarkView.setImageResource(R.drawable.selected);
        checkmarkView.setVisibility(View.INVISIBLE); // הסתרת הסימון בתחילה

        // הוספת סימן ה-וי לפריסה
        addContentView(checkmarkView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        ));

        // הגדרת מאזינים לכפתורי הבחירה של האווטארים
        setAvatarSelection(findViewById(R.id.avatar1), R.drawable.avt1);
        setAvatarSelection(findViewById(R.id.avatar2), R.drawable.avt2);
        setAvatarSelection(findViewById(R.id.avatar3), R.drawable.avt3);
        setAvatarSelection(findViewById(R.id.avatar4), R.drawable.avt4);
        setAvatarSelection(findViewById(R.id.avatar5), R.drawable.avt5);
        setAvatarSelection(findViewById(R.id.avatar6), R.drawable.avt6);
        setAvatarSelection(findViewById(R.id.avatar7), R.drawable.avt7);
        setAvatarSelection(findViewById(R.id.avatar8), R.drawable.avt8);

        // לחיצה על הכפתור "הבא" לשמירת האווטאר
        Button btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(view ->

                saveAvatarToDatabase()
        );
    }

    private void setAvatarSelection(ImageButton avatarButton, int avatarResourceId) {
        selectedAvatarButton = avatarButton;
        selectedAvatarUrl = avatarResourceId;
        checkmarkView.setVisibility(View.INVISIBLE);

        avatarButton.setOnClickListener(view -> {
            Log.d("shani", "ok");
            // הצבת הוי מעל האווטאר שנבחר
            if (selectedAvatarButton != null) {
                Log.d("shani", "ok1");

                selectedAvatarButton.setForeground(null);  // הסרת הסימון מהאווטאר הקודם
            }
            selectedAvatarButton = avatarButton;
            selectedAvatarUrl = avatarResourceId;

            Log.d("shani", "ok2");
// שמירת ה-ID של המשאב

            // הגדרת מיקום סימן הוי מעל הכפתור הנבחר
            int[] location = new int[2];
            avatarButton.getLocationOnScreen(location);
            checkmarkView.setX(location[0] + avatarButton.getWidth() / 2 - checkmarkView.getWidth() / 2);
            checkmarkView.setY(location[1] + avatarButton.getHeight() / 2 - checkmarkView.getHeight() / 2);

            checkmarkView.setVisibility(View.VISIBLE);  // הצגת הוי

            Log.d("shani", "ok3");

        });
    }

    private void saveAvatarToDatabase() {

        Log.d("shneor", "test1");

        // בדוק אם אווטאר נבחר
        if (selectedAvatarUrl == 0) {
            Toast.makeText(this, getString(R.string.chooseAvt), Toast.LENGTH_SHORT).show();
            return;
        }
        else {

            // קבל את מזהה המשתמש
            String userId = mAuth.getCurrentUser().getUid();

            Log.d("shneor", "משתמש" + userId);

            // שמירת כתובת האווטאר בדאטאבייס
            mDatabase.child(userId).child("avatar").setValue(selectedAvatarUrl)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(select_avatar.this, getString(R.string.successfullyAvt), Toast.LENGTH_SHORT).show();

                            Log.d("shneor", "אווטאר נשמר בהצלחה למשתמש: " + userId);


                            // מעבר לעמוד הצ'אט או עמוד אחר
                            Intent intent = new Intent(select_avatar.this, HomePage.class);
                            intent.putExtra("key", "first"); // מידע לפתיחת הדיאלוג
                            String name2= mAuth.getCurrentUser().getDisplayName();
                            intent.putExtra("name", name2);

                            startActivity(intent);
                        } else {
                            Toast.makeText(select_avatar.this, getString(R.string.failedAvt), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}