package com.links.freud;

import static android.app.ProgressDialog.show;
//import static com.example.freud.Chat.LOCATION_PERMISSION_REQUEST_CODE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BottomNavigationFragment extends Fragment {

    public BottomNavigationFragment() {
        // Constructor ריק נדרש
    }

    private ImageButton btnRulerSos, btnRulerPhone, btnRulerHome, btnRulerPersonalA, btnRulerC;

    private FirebaseAuth mAuth;

    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate את קובץ ה-XML של ה-Bottom Navigation
        View view = inflater.inflate(R.layout.bottom_nav, container, false);

        btnRulerPhone = view.findViewById(R.id.rulerPhone);
        btnRulerHome = view.findViewById(R.id.rulerH);
        btnRulerPersonalA = view.findViewById(R.id.rulerPersonalA);
        btnRulerC = view.findViewById(R.id.rulerC);
        btnRulerSos = view.findViewById(R.id.rulerSos);

        // הוספת פעולות לכל כפתור
        btnRulerSos.setOnClickListener(v -> {
            // פעולה לביצוע כאשר לוחצים על כפתור ה-SOS
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            LayoutInflater dialogInflater = LayoutInflater.from(requireContext());  // שימוש בפונקציה שמגיעה מ-Fragment
            View dialogView = dialogInflater.inflate(R.layout.dialog_custom, null);  // טוען את ה-Custom Layout שיצרת
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();

            // הגדרת רקע שקוף לדיאלוג
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            // כפתור הסגירה של הדיאלוג
            ImageButton closeButton = dialogView.findViewById(R.id.closeButtonSos);
            closeButton.setOnClickListener(closeView -> dialog.dismiss());

            // תופס את הכפתור מה-XML ומבצע פעולה כשנלחץ עליו
            Button btnSend = dialogView.findViewById(R.id.btnSend);
            btnSend.setOnClickListener(sendView -> {
                mAuth = FirebaseAuth.getInstance();
                if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isAnonymous()) {
                    // במקרה של משתמש אנונימי
                    Toast.makeText(getContext(), getString(R.string.TSosSmsAnonimus), Toast.LENGTH_SHORT).show();
                } else {
                    sendSMSSOS();
                }

                dialog.dismiss(); // סוגר את ה-Dialog לאחר שליחה
            });
            // מציג את ה-Dialog
            dialog.show();
        });

        btnRulerPhone.setOnClickListener(v -> {

            if (getActivity().getClass().getName().equals(Contact_us.class.getName())) {
                return;
            } else {
                Intent intent = new Intent(getActivity(), Contact_us.class); // מעבר לעמוד טלפון
                startActivity(intent);
            }
        });

        btnRulerHome.setOnClickListener(v -> {

            if (getActivity().getClass().getName().equals(HomePage.class.getName())) {
                return;
            } else {
                // פעולה לביצוע כאשר לוחצים על כפתור הבית
                Intent intent = new Intent(getActivity(), HomePage.class); // מעבר לעמוד הבית
                startActivity(intent);
            }
//            btnRulerHome.setImageResource(R.drawable.homep);
        });

        btnRulerPersonalA.setOnClickListener(v -> {

            mAuth = FirebaseAuth.getInstance();

            if (mAuth.getCurrentUser() != null) {
                // אם המשתמש מחובר, ניגשים לנתונים שלו
                if (mAuth.getCurrentUser().isAnonymous()) {
                    // במקרה של משתמש אנונימי
                    Toast.makeText(getActivity(), getString(R.string.TPersonalAnonimus), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getActivity(), Personal_area.class); // מעבר לאזור אישי
                    startActivity(intent);
                }
            }

        });

        btnRulerC.setOnClickListener(v -> {

            if (getActivity().getClass().getName().equals(Chat.class.getName())) {
                return;
            } else {
                // פעולה לביצוע כאשר לוחצים על כפתור הצ'אט
                Intent intent = new Intent(getActivity(), Chat.class); // מעבר לעמוד הצ'אט
                startActivity(intent);
            }
        });

        return view;
    }

    public void updateSelectedButton(int buttonId) {
        // תחילה איפוס כל הכפתורים לצבע המקורי
        resetButtons();

        // עדכון הכפתור הנבחר לצבע או רקע אחר
        if (buttonId == R.id.rulerH && btnRulerHome != null) {
            btnRulerHome.setImageResource(R.drawable.homep);
        } else if (buttonId == R.id.rulerPersonalA && btnRulerPersonalA != null) {
            btnRulerPersonalA.setImageResource(R.drawable.personalareap);
        } else if (buttonId == R.id.rulerC && btnRulerC != null) {
            btnRulerC.setImageResource(R.drawable.chatp);
        } else if (buttonId == R.id.rulerPhone && btnRulerPhone != null) {
            btnRulerPhone.setImageResource(R.drawable.phonep);
        }
    }

    private void resetButtons() {
        if (btnRulerPhone != null) {
            btnRulerPhone.setImageResource(R.drawable.phone);
        }
        if (btnRulerHome != null) {
            btnRulerHome.setImageResource(R.drawable.home);
        }
        if (btnRulerPersonalA != null) {
            btnRulerPersonalA.setImageResource(R.drawable.personalarea);
        }
        if (btnRulerC != null) {
            btnRulerC.setImageResource(R.drawable.chat);
        }
    }

    private void sendSMSSOS() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String phone = dataSnapshot.child("phone").getValue(String.class);
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String phone1sos = dataSnapshot.child("phone1sos").getValue(String.class);
                    String phone2sos = dataSnapshot.child("phone2sos").getValue(String.class);
                    String phone3sos = dataSnapshot.child("phone3sos").getValue(String.class);

                    // קבלת המיקום הנוכחי של המשתמש
                    getLastLocation(location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            String message = getString(R.string.sosSMS1) + name + " (" + phone + ").";
                            message += getString(R.string.sosSMS2) + " https://www.google.com/maps?q=" + latitude + "," + longitude;

                            // שליחת הודעה לכל מספר שאינו ריק
                            if (phone1sos != null && !phone1sos.isEmpty()) {
                                sendSMS(phone1sos, message, getContext());
                            }
                            if (phone2sos != null && !phone2sos.isEmpty()) {
                                sendSMS(phone2sos, message, getContext());
                            }
                            if (phone3sos != null && !phone3sos.isEmpty()) {
                                sendSMS(phone3sos, message, getContext());
                            }
                        } else {
//                            Toast.makeText(getContext(), "לא ניתן לאתר את המיקום שלך", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), getString(R.string.TSosSmsAnonimus), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Error fetching SOS contacts", databaseError.toException());
            }
        });
    }

    private void getLastLocation(OnSuccessListener<Location> onSuccessListener) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(onSuccessListener)
                    .addOnFailureListener(e -> Log.e("LocationError", "Failed to get location", e));
        } else {
            Log.e("LocationError", "Location permission not granted");
            // בקש את ההרשאה אם היא לא ניתנה
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


//    private void sendSMSSOS() {
//        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
//        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
//
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                Log.d("shneor", "1 ");
//
//                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
//                    Log.e("shneor", "User is not authenticated");
//                    return;
//                }
//
//                if (dataSnapshot.exists()) {
//                    Log.d("shneor", "2 ");
//                    String phone = dataSnapshot.child("phone").getValue(String.class);
//                    String name = dataSnapshot.child("name").getValue(String.class);
//                    String phone1sos = dataSnapshot.child("phone1sos").getValue(String.class);
//                    String phone2sos = dataSnapshot.child("phone2sos").getValue(String.class);
//                    String phone3sos = dataSnapshot.child("phone3sos").getValue(String.class);
//
//                    Log.d("shneor", "3 " + phone1sos);
//
//                    String message = "היי, נשלחה אליך בקשת עזרה דרך אפליקציית Freud מ- " + name.toString() + " (" + phone + ").";
//                    message += "\nבבקשה עזרו לי, אני זקוק לעזרה דחופה!" + mapUrl;
//
//
//                    // שליחת הודעה לכל מספר שאינו ריק
//                    if (phone1sos != null && !phone1sos.isEmpty()) {
//                        sendSMS(phone1sos, message, getContext());
//                    }
//                    if (phone2sos != null && !phone2sos.isEmpty()) {
//                        sendSMS(phone2sos, message, getContext());
//                    }
//                    if (phone3sos != null && !phone3sos.isEmpty()) {
//                        sendSMS(phone3sos, message, getContext());
//                    }
//                } else {
//                    Toast.makeText(getContext(), "לא נמצאו מספרי חירום", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.e("FirebaseError", "Error fetching SOS contacts", databaseError.toException());
//            }
//        });
//    }

    // שליחת הודעת SMS למספר יחיד
    private void sendSMS(String phoneNumber, String message, Context context) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
            Toast.makeText(context, getString(R.string.TSosSms) + phoneNumber, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, getString(R.string.TSosSmsX), Toast.LENGTH_SHORT).show();
        }
    }

//    public void getLastLocation2(Context context) {
//        // Check for location permissions
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
//        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//
//            // Get last known location
//            Task<Location> locationResult = fusedLocationClient.getLastLocation();
//            locationResult.addOnSuccessListener(new OnSuccessListener<Location>() {
//                @Override
//                public void onSuccess(Location location) {
//                    if (location != null) {
//
//                        double latitude = location.getLatitude();
//                        double longitude = location.getLongitude();
//
//                        String uri1 = "https://waze.com/ul?q=66%20Acacia%20Avenue&ll=" + latitude +"," + longitude + "&navigate=yes";
//                        String mapUrl = "https://www.google.com/maps?q=" + latitude + "," + longitude;
//
//                    } else {
//                        Log.e("shneor", "Location is null");
//                    }
//                }
//            }).addOnFailureListener(e -> {
//                Log.e("shneor", "Failed to get location", e);
//            });
//        } else {
//            Log.e("shneor", "Location permission not granted");
//        }
//    }


}
