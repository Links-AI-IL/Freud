package com.links.freud;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public class QuestionResponseAdapter extends RecyclerView.Adapter<QuestionResponseAdapter.ViewHolder> {

    private List<QuestionResponse> questionResponseList;

    private TextToSpeech textToSpeech;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    private SharedPreferences sharedPreferences;

    private boolean isLoading = false;

    private boolean isCube = false;

    private String gender;

    private boolean isOld;

    public QuestionResponseAdapter(List<QuestionResponse> questionResponseList, Context context, boolean isOld) {
        this.questionResponseList = questionResponseList;
        this.isOld = isOld;

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                SharedPreferences prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
                String selectedLanguage = prefs.getString("My_Lang", "iw");
                Locale locale = selectedLanguage.equals("iw") ? new Locale("he", "IL") : Locale.US;
                textToSpeech.setLanguage(locale);
                setFemaleVoice();
            }
        });

        sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.question_response, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        QuestionResponse questionResponse = questionResponseList.get(position);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null && !mAuth.getCurrentUser().isAnonymous()) {

            String userId = mAuth.getCurrentUser().getUid();
            Log.d("shneor22", "id" + userId);
            mDatabase = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            fetchAvatar(userId, holder);
        }

        if (isOld) {

            holder.questionText.setTextSize(27f);
            holder.responseText.setTextSize(27f);
        } else {
            holder.questionText.setTextSize(18f);
            holder.responseText.setTextSize(18f);
        }

        if (isLoading && position == questionResponseList.size() - 1) {

            holder.questionContainer.setVisibility(View.GONE);
            holder.heartProgressBar.setVisibility(View.VISIBLE);
            holder.responseBtns.setVisibility(View.GONE);
            holder.responseIcon.setVisibility(View.VISIBLE);
            holder.responseText.setVisibility(View.VISIBLE);

        } else {
            holder.heartProgressBar.setVisibility(View.GONE);
            holder.responseIcon.setVisibility(View.VISIBLE);
            holder.responseBtns.setVisibility(View.VISIBLE);
        }

        if (isCube && (questionResponseList.size() - 1 == position)) {

            holder.questionContainer.setVisibility(View.GONE);
            holder.responseText.setText(questionResponse.getResponse());
            holder.responseText.setVisibility(View.VISIBLE);

        } else {
            holder.questionContainer.setVisibility(View.VISIBLE);
            holder.questionText.setText(questionResponse.getQuestion());
            holder.responseText.setText(questionResponse.getResponse());
            holder.responseText.setVisibility(View.VISIBLE);
        }

        holder.readMe.setOnClickListener(
                v -> playResponse(v.getContext(), questionResponse.getResponse()));

        holder.copyBtn.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied Text", questionResponse.getResponse());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(v.getContext(), "Copied to clipboard!", Toast.LENGTH_SHORT).show();
        });

        holder.helplinesBtn.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, Helplines.class);
            context.startActivity(intent);
            Toast.makeText(v.getContext(), v.getContext().getString(R.string.Thelp), Toast.LENGTH_SHORT).show();
        });

        if (isFirstTimeOpening()) {
            highlightHelpLineButton(holder);
            setFirstTimeOpened();
            scrollToPosition(holder.getAdapterPosition(), holder.itemView.getContext());
        }
    }

    @Override
    public int getItemCount() {
        return questionResponseList.size();
    }

    public void setLoading(boolean loading) {
        this.isLoading = loading;
        notifyItemChanged(questionResponseList.size() - 1);
    }

    public void setQuestion(boolean cube) {
        this.isCube = cube;
    }

    private boolean isFirstTimeOpening() {
        return sharedPreferences.getBoolean("isFirstTime", true);
    }

    private void setFirstTimeOpened() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isFirstTime", false);
        editor.apply();
    }

    private void highlightHelpLineButton(ViewHolder holder) {

        ObjectAnimator pulse = ObjectAnimator.ofFloat(holder.helplinesBtn, "scaleX", 1f, 1.2f, 1f);
        pulse.setDuration(500);
        pulse.setRepeatCount(ObjectAnimator.INFINITE);
        pulse.setRepeatMode(ObjectAnimator.REVERSE);
        pulse.start();
    }

    private void scrollToPosition(int position, Context context) {
        RecyclerView recyclerView = ((Activity) context).findViewById(R.id.recyclerView);
        recyclerView.scrollToPosition(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout questionContainer, responseBtns;
        TextView questionText, responseText;
        ImageView requestIcon, responseIcon;
        ImageButton helplinesBtn, copyBtn, readMe;
        ProgressBar progressBar;
        ImageView heartProgressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            questionContainer = itemView.findViewById(R.id.requestLL);
            requestIcon = itemView.findViewById(R.id.requestIcon);
            questionText = itemView.findViewById(R.id.request);
            responseIcon = itemView.findViewById(R.id.responseIcon);
            responseText = itemView.findViewById(R.id.response);
            responseBtns = itemView.findViewById(R.id.responseBtns);
            helplinesBtn = itemView.findViewById(R.id.helplinesBtn);
            copyBtn = itemView.findViewById(R.id.copyBtn);
            readMe = itemView.findViewById(R.id.readMe);
            progressBar = itemView.findViewById(R.id.progress_bar);

            heartProgressBar = itemView.findViewById(R.id.heartProgressBar);
            Glide.with(itemView.getContext())
                    .asGif()
                    .load(R.drawable.progres_red)
                    .into(heartProgressBar);
        }
    }

    private void playResponse(Context context, String responseText) {

        stopTextToSpeech();

        if (responseText != null && !responseText.isEmpty()) {

            String sanitizedText = responseText.replaceAll("[\\p{So}\\p{Cn}]", " ");


            SharedPreferences prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
            String selectedLanguage = prefs.getString("My_Lang", "iw"); // ברירת מחדל לעברית

            Locale locale;
            if ("iw".equals(selectedLanguage)) {
                locale = new Locale("he", "IL");
            } else {
                locale = new Locale("en", "US");
            }

            int result = textToSpeech.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(context, "Language not supported", Toast.LENGTH_SHORT).show();
                return;
            }

            setFemaleVoice();

            textToSpeech.speak(sanitizedText, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void setFemaleVoice() {
        Set<Voice> voices = textToSpeech.getVoices();
        Log.d("shneor23", "Voice name: " + voices);
        for (Voice voice : voices) {
            if (voice.getName().toLowerCase().contains("he-il-x-hee-local")) {
                textToSpeech.setVoice(voice);
                textToSpeech.setSpeechRate(0.8f);
                Log.d("shneor23", "Voice name: " + voice.getName());
                return;
            }
        }
        textToSpeech.setVoice(textToSpeech.getVoice());
    }


    public void stopTextToSpeech() {
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
    }

    public void shutdownTextToSpeech() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }

    private void fetchAvatar(String userId, ViewHolder holder) {
        mDatabase.child("avatar").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int avatarResourceId = dataSnapshot.getValue(Integer.class);
                    // Set the image to the ImageView
                    holder.requestIcon.setImageResource(avatarResourceId);
                    Log.d("shneor23", "Avatar resource ID: " + avatarResourceId);
                } else {
                    Log.e("FetchAvatar", "No avatar found for this user");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("shneor", "Error fetching avatar", databaseError.toException());
            }
        });
    }
}
