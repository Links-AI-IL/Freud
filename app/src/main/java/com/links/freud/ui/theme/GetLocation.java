package com.links.freud.ui.theme;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.links.freud.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;


public class GetLocation {

    private static final int SMS_PERMISSION_CODE = 1 ;
    private FusedLocationProviderClient fusedLocationClient;

    public void getLastLocation(Context context , String numberOne, String question) {
        // Check for location permissions
        Log.e("shneor", "22 ");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // Get last known location
            Task<Location> locationResult = fusedLocationClient.getLastLocation();
            locationResult.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Log.e("shneor", "Location is exist");
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Log.d("Location111", "Latitude: " + latitude + ", Longitude: " + longitude);
                        String uri1 = "https://waze.com/ul?q=66%20Acacia%20Avenue&ll=" + latitude +"," + longitude + "&navigate=yes";
                        String mapUrl = "https://www.google.com/maps?q=" + latitude + "," + longitude;
                        sendSMS(numberOne, "שלום, זוהי הודעה מצוות האפליקציה של Freud." +
                                " אחד המשתמשים שלנו נמצא במצוקה. מצורף ציטוט ההודעה שקיבלנו מהמשתמש ומיקום מדויק שלו: "
                                + "\n" + "Click link to go location" + "\n" + mapUrl + "\n" + "This is the user text: " + question , context);
                    } else {
                        Log.e("shneor", "Location is null");
                    }
                }
            }).addOnFailureListener(e -> {
                Log.e("shneor", "Failed to get location", e);
            });
        } else {
            Log.e("shneor", "Location permission not granted");
        }
    }

//    @SuppressLint("RestrictedApi")
    private void sendSMS(String phoneNumber, String message ,Context context) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            // בדיקה אם ההודעה ארוכה או מכילה תווים שאינם באנגלית
            if (message.length() > 70 || !message.matches("\\A\\p{ASCII}*\\z")) {
                // פיצול ההודעה למספר חלקים במידת הצורך
                ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
            } else {
                // שליחה רגילה אם ההודעה קצרה וכוללת רק תווים באנגלית
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            }

//            Toast.makeText(context, "SMS sent!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Failed to send SMS", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == SMS_PERMISSION_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted, send SMS
//            } else {
//                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

}