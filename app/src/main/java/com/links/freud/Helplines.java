package com.links.freud;

import static java.util.Locale.filter;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Helplines extends AppCompatActivity {


    private EditText searchField;
    private LinearLayout helplinesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_helplines);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        searchField = findViewById(R.id.searchH);
        helplinesLayout = findViewById(R.id.helplinesLayout);

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        BottomNavigationFragment bottomNavFragment = new BottomNavigationFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottomNavFragment, bottomNavFragment)
                .commit();
    }

    private void filter(String text) {
        for (int i = 0; i < helplinesLayout.getChildCount(); i++) {
            View view = helplinesLayout.getChildAt(i);

            // בודקים אם מדובר ב-LinearLayout שמכיל את המידע על קו סיוע
            if (view instanceof LinearLayout) {
                LinearLayout helplineItem = (LinearLayout) view;

                boolean matchFound = false;

                // נעבור על כל הילדים של ה-LinearLayout כדי למצוא את כל ה-TextView
                for (int j = 0; j < helplineItem.getChildCount(); j++) {
                    View childView = helplineItem.getChildAt(j);

                    // בדיקה אם מדובר ב-TextView
                    if (childView instanceof TextView) {
                        TextView textView = (TextView) childView;

                        // אם ה-TextView מכיל את הטקסט המבוקש, מצאנו התאמה
                        if (textView.getText().toString().toLowerCase().contains(text.toLowerCase())) {
                            matchFound = true;
                            break;
                        }
                    }
                }

                // אם מצאנו התאמה, נציג את ה-LinearLayout, אחרת נסתיר אותו
                if (matchFound) {
                    helplineItem.setVisibility(View.VISIBLE);
                } else {
                    helplineItem.setVisibility(View.GONE);
                }
            }
        }
    }
}