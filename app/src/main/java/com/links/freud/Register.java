package com.links.freud;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;


public class Register extends AppCompatActivity {
    private Button btnCreate;
    private EditText name, email, password, phone, age, phone1sos, phone2sos, phone3sos;
    private CheckBox checkBox;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private GoogleSignInClient mGoogleSignInClient;
    private RadioGroup genderRadioGroup;
    private ImageButton closeButton;
    private static final int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        String currentLanguage = getResources().getConfiguration().getLocales().get(0).getLanguage();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso); // אתחול ה-GoogleSignInClient

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        TextView termsTextView = findViewById(R.id.termsCheckBox);
        String termsFullText = getString(R.string.terms1);
        String termsClickablePart = getString(R.string.terms2);

        SpannableStringBuilder termsSpannableString = new SpannableStringBuilder(termsFullText);
        int termsStartIndex = termsFullText.indexOf(termsClickablePart);
        int termsEndIndex = termsStartIndex + termsClickablePart.length();

        termsSpannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), termsStartIndex, termsEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        termsSpannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showTermsDialog();
            }

            @Override
            public void updateDrawState(@NonNull android.text.TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        }, termsStartIndex, termsEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        termsTextView.setText(termsSpannableString);
        termsTextView.setMovementMethod(LinkMovementMethod.getInstance());


        VideoView videoView = findViewById(R.id.registerVideo);
        View transparentBackground = findViewById(R.id.transparentBackground);
        ImageButton closeButton = findViewById(R.id.closeVideoButton);

        playVideo();

        Context currentContext = this;

        closeButton.setOnClickListener(v -> {
            transparentBackground.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            closeButton.setVisibility(View.GONE);
            videoView.stopPlayback();
        });

        mAuth = FirebaseAuth.getInstance();
        name = findViewById(R.id.inputN);
        email = findViewById(R.id.inputEmail);
        password = findViewById(R.id.inputPassword);
        phone = findViewById(R.id.inputP);
        age = findViewById(R.id.inputAge);
        phone1sos = findViewById(R.id.inputPSOS1);
        phone2sos = findViewById(R.id.inputPSOS2);
        phone3sos = findViewById(R.id.inputPSOS3);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);

        btnCreate = findViewById(R.id.btnCreateRegister);

        checkBox = findViewById(R.id.termsCheckBox);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isValid = true;

                if (name.getText().toString().isEmpty()) {
                    name.setError(getString(R.string.enterYourName));
                    isValid = false;
                }

                if (email.getText().toString().isEmpty()) {
                    email.setError(getString(R.string.enterEmail));
                    isValid = false;
                }

                if (password.getText().toString().isEmpty()) {
                    password.setError(getString(R.string.enterPassword));
                    isValid = false;
                }

                if (phone.getText().toString().isEmpty()) {
                    phone.setError(getString(R.string.enterPhoneNumber));
                    isValid = false;
                }

                if (age.getText().toString().isEmpty()) {
                    age.setError(getString(R.string.enterAge));
                    isValid = false;
                }

                if (phone1sos.getText().toString().isEmpty()) {
                    phone1sos.setError(getString(R.string.enterEmergencyPhone1));
                    isValid = false;
                }

                if (phone2sos.getText().toString().isEmpty()) {
                    phone2sos.setError(getString(R.string.enterEmergencyPhone2));
                    isValid = false;
                }

                int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
                String gender = "";
                if (selectedGenderId != -1) {
                    RadioButton selectedGenderButton = findViewById(selectedGenderId);
                    String selectedGenderText = selectedGenderButton.getText().toString().toLowerCase();

                    if (selectedGenderText.contains("זכר") || selectedGenderText.contains("male")) {
                        gender = "male";
                    } else if (selectedGenderText.contains("נקבה") || selectedGenderText.contains("female")) {
                        gender = "female";
                    } else {
                        gender = "other";
                    }
                } else {
                    Toast.makeText(Register.this, getString(R.string.chooseG), Toast.LENGTH_SHORT).show();
                    isValid = false;
                }

                if (!checkBox.isChecked()) {
                    Toast.makeText(Register.this, getString(R.string.acceptT), Toast.LENGTH_SHORT).show();
                    isValid = false;
                }

                if (isValid) {
                    registerUser(name.getText().toString(), email.getText().toString(),
                            password.getText().toString(), phone.getText().toString(), Integer.parseInt(age.getText().toString()), phone1sos.getText().toString(),
                            phone2sos.getText().toString(), phone3sos.getText().toString(), gender);
                }
            }
        });

        TextView textView = findViewById(R.id.loginInRegisterPage);

        String fullText = getString(R.string.userExist1);
        String clickablePart = getString(R.string.userExist2);

        SpannableStringBuilder spannableString = new SpannableStringBuilder(fullText);

        int startIndex = fullText.indexOf(clickablePart);
        int endIndex = startIndex + clickablePart.length();

        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull android.text.TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        }, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void playVideo() {

        String currentLanguage = getResources().getConfiguration().getLocales().get(0).getLanguage();

        VideoView videoView = findViewById(R.id.registerVideo);
        View transparentBackground = findViewById(R.id.transparentBackground);
        ImageButton closeButton = findViewById(R.id.closeVideoButton);

        Locale currentLocale = getResources().getConfiguration().locale;

        Log.d("shneor", "Current language: " + currentLanguage);
        if (currentLanguage.equals("iw")) {
            Log.d("shneor", "Current language: " + currentLanguage);
            // Change the button image for Hebrew
            closeButton.setImageResource(R.drawable.skiphe); // Use your Hebrew image resource here
            videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.hila_video2));
        } else {
            Log.d("shneor", "Current language: " + currentLanguage);
            // Default image
            closeButton.setImageResource(R.drawable.skipen); // Use your default image resource here
            videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mag_video2));
        }

        transparentBackground.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.VISIBLE);
        closeButton.setVisibility(View.VISIBLE);

        videoView.start();

        videoView.setOnCompletionListener(mp -> {
            transparentBackground.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            closeButton.setVisibility(View.GONE);
            videoView.stopPlayback();
        });
    }

    private void showTermsDialog() {
        TermsDialogFragment termsDialogFragment = new TermsDialogFragment();
        termsDialogFragment.show(getSupportFragmentManager(), "termsDialog");
    }

    private void registerUser(String name, String email, String password, String phone, int age, String phone1sos, String phone2sos, String phone3sos, String gender) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            firebaseUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {

                                            // User registered successfully, now store user data in the database
                                            User user = new User(name, email, password, phone, age, phone1sos, phone2sos, phone3sos, gender);
                                            String userId = firebaseUser.getUid();

                                            // Log the user object
                                            Log.d("RegisterUser", "User object: " + user.name.toString());

                                            mDatabase.child(userId).setValue(user)
                                                    .addOnCompleteListener(task1 -> {
                                                        if (task1.isSuccessful()) {
                                                            Intent intent = new Intent(Register.this, select_avatar.class);
                                                            intent.putExtra("name", name);
                                                            startActivity(intent);
                                                            Toast.makeText(Register.this, getString(R.string.userAS), Toast.LENGTH_SHORT).show();
                                                            Log.d("SignUpActivity.this", "User registered successfully");
                                                        } else {
                                                            // Log the error and notify the user
                                                            Log.d("StoreUserData", "Failed to store user data: " + task1.getException().getMessage());
                                                            Toast.makeText(Register.this, getString(R.string.userAF) + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            Log.d("shani", "Registration failed: " + task.getException().getMessage());
                                            Toast.makeText(Register.this, getString(R.string.userAF) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(Register.this, getString(R.string.userE), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}


