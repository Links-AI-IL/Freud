package com.links.freud;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.google.firebase.auth.FirebaseUser;
import com.links.freud.backend.ClaudeContextManager;
import com.links.freud.backend.LinksApi;
import com.links.freud.ui.theme.GetLocation;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

public class Chat extends AppCompatActivity {

    private static final String API_CLAUDE_KEY = "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";

    private static final String API_CLAUDE_URL = "https://api.anthropic.com/v1/complete";

    private static final String MY_CLAUDE_MODEL = "claude-3.5-turbo";

    private static final String MY_API = "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";

    private static final String MY_URL = "https://api.openai.com/v1/chat/completions";

    private static final String MY_MODEL = "gpt-3.5-turbo";

    private static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    private static final int REQUEST_CODE_TTS_SETTINGS = 102;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private static final org.apache.commons.logging.Log log = LogFactory.getLog(Chat.class);

    private GetLocation getLocation = new GetLocation();

    private ImageButton _micBtn, sendM;

    private EditText input;

    private FusedLocationProviderClient fusedLocationClient;

    private String userN, userG, lastLanguage, cube;

    private SpeechRecognizer _speechRecognizer;

    private Intent _speechRecognizerIntent;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    private RecyclerView _recyclerView;

    private QuestionResponseAdapter _adapter;

    private List<QuestionResponse> _questionResponseList;

    private ArrayList<String> keywordsList;

    private static int activeActivitiesCount = 0;

    private boolean isOld;

    private String selectedAgeGroup;

    private ImageButton scrollToBottomButton;

    private long startTime = 0;

    private Handler timerHandler = new Handler();

    private TextView timerTextView;

    private ChatManager chatManager;

    // region Members Backend

    private static ClaudeContextManager _contextManager;

    private LinksApi _linksApi;

    private String _currAnswer = "";

    private Handler _handler = new Handler();
    private Runnable _typingRunnable;

    // endregion

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        isOld = getIntent().getBooleanExtra("isOld", false);
        selectedAgeGroup = getIntent().getStringExtra("age");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        String[] keywords = getResources().getStringArray(R.array.keywords);
        keywordsList = new ArrayList<>(Arrays.asList(keywords));

        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String savedLanguage = prefs.getString("My_Lang", "iw");
        lastLanguage = savedLanguage.equals("iw") ? "he" : "en";

        Log.d("shneor1", "Loaded language: " + lastLanguage);

        sendM = findViewById(R.id.send);

        _micBtn = findViewById(R.id.record);

        timerTextView = findViewById(R.id.timerTextView);

        input = findViewById(R.id.inputQ);

        _recyclerView = findViewById(R.id.recyclerView);

        _questionResponseList = new ArrayList<>();

        _adapter = new QuestionResponseAdapter(_questionResponseList, this, isOld);

        _recyclerView.setLayoutManager(new LinearLayoutManager(this));

        _recyclerView.setHasFixedSize(true);

        _recyclerView.setAdapter(_adapter);

        _recyclerView.setItemAnimator(null);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        _contextManager = new ClaudeContextManager();

        if (!mAuth.getCurrentUser().isAnonymous() && mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();

            Log.d("shanii", "g");
            Log.d("shanii", userId);

            fetchAvatar(userId, gender -> {
                Log.d("shanii", "g1");

                if (gender != null) {
                    Log.d("shanii", "g2");

                    userG = gender;
                }
                else {

                    Log.d("shanii", "g3");

                    userG = "unknown";
                }
            });

            if (mAuth.getCurrentUser() != null) {
                mDatabase = FirebaseDatabase.getInstance().getReference("Users");

                FirebaseUser user = mAuth.getCurrentUser();
                userId = user.getUid();

                String displayName = user.getDisplayName();

                if (displayName != null && !displayName.isEmpty()) {
                    userN = displayName;
                }
                else {
                    userN = "Dear user";
                }

                if (displayName == null || displayName.isEmpty()) {
                    displayName = user.getEmail();
                }

                userN = displayName != null ? displayName : "Dear user";
            }

            else {
                userN = "Dear user";
                userG = "unknown";
            }

        }

        else {
            userN = "Dear user";
            userG = "unknown";
            Toast.makeText(Chat.this, getString(R.string.TAnonimusC), Toast.LENGTH_SHORT).show();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        _speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        _speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

                Toast.makeText(Chat.this, getString(R.string.TStartSpeek), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {

                _speechRecognizer.stopListening();
            }

            @Override
            public void onError(int error) {

                Log.e("SpeechError", "Error code: " + error);
            }

            @Override
            public void onResults(Bundle results) {

                long startTime = System.currentTimeMillis();
                Log.d("SpeechRecognizer", "Results received at: " + startTime);

                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {

                    String spokenText = matches.get(0);
                    input.setText(spokenText);

                    long endTime = System.currentTimeMillis();
                    Log.d("SpeechRecognizer", "Text set at: " + endTime + " after " + (endTime - startTime) + " ms");

                    sendM.performClick();
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });

        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                timerTextView.setVisibility(View.VISIBLE);

                long elapsedTime = System.currentTimeMillis() - startTime;
                int seconds = (int) (elapsedTime / 1000);
                timerTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60));

                timerHandler.postDelayed(this, 1000);
            }
        };

        _micBtn.setOnTouchListener((v, event) -> {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    _adapter.stopTextToSpeech();

                    input.setHint("");

                    startTime = System.currentTimeMillis();
                    timerHandler.post(timerRunnable);

                    if (_speechRecognizer == null) {
                        _speechRecognizer = SpeechRecognizer.createSpeechRecognizer(Chat.this);
                    }

                    _speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    _speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    _speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false);

                    Locale currentLocale = getResources().getConfiguration().locale;
                    if (currentLocale.getLanguage().equals("iw")) {
                        _speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "he-IL");
                    } else {
                        _speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                    }

                    _speechRecognizer.startListening(_speechRecognizerIntent);
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:

                    timerHandler.removeCallbacks(timerRunnable);
                    if (_speechRecognizer != null) {
                        _speechRecognizer.stopListening();
                        Log.d("SpeechRecognizer", "Stopped listening");
                    }

                    timerTextView.setVisibility(View.GONE);
                    input.setHint("רשמו הודעה...");
                    return true;

                default:
                    return false;
            }
        });

        moveKeyboard();

        BottomNavigationFragment bottomNavFragment = new BottomNavigationFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottomNavFragment, bottomNavFragment)
                .commit();

        findViewById(R.id.main).post(() -> setSelectedButtonInNav());

        setSelectedButtonInNav();

        String languageInstruction;

        if (lastLanguage.equals("iw")) {
            languageInstruction = "Answer in Hebrew only. Use clear formatting, paragraphs, and appropriate emojis.";
        } else if (lastLanguage.equals("en")) {
            languageInstruction = "Answer in English only. Use clear formatting, paragraphs, and appropriate emojis.";
        } else {
            languageInstruction = "Answer in Hebrew only. Use clear formatting, paragraphs, and appropriate emojis";
        }

        String genderInstruction = "The user name is " + userN + " and their gender is " + userG + ". and is age group " + selectedAgeGroup + " so Talk to him accordingly and write only with diacritics only in Hebrew.";

        String responseInstruction;

        responseInstruction = "Introduce yourself as Hila and speak like you are female, address the user by name, speak to the user according to their gender only without /, Your role is to help with mental health issues and emotional situations only. you should always respond with empathy, give helpful advice based on the user's emotional state and encourage the user to continue the conversation.";

        String fullPrompt = languageInstruction + genderInstruction + responseInstruction + "The user has asked: ";

        chatManager = new ChatManager(this, fullPrompt);

        sendM.setOnClickListener(v -> sendMessage(v));

        _linksApi = new LinksApi(
                input.getText().toString(),
                new LinksApi.ChunkCallback() {
                    @Override
                    public void onResponse(String message) {
                        // Update the UI with the chunk response
                        runOnUiThread(() -> {
                            _adapter.setLoading(false);

                            _adapter.setLoadingFull(false);

                            if (_handler != null) {
                                _handler.removeCallbacks(_typingRunnable);
                            }

                            updateResponse(message);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        // Handle chunk response error
                        runOnUiThread(() -> {
                            Toast.makeText(Chat.this, "Error receiving chunk: " + error, Toast.LENGTH_SHORT).show();
                            Log.e("ChunkError", error);
                        });
                    }
                },

                new LinksApi.FullResponseCallback() {
                    @Override
                    public void onResponse(String message) {
                        // Handle the full response
                        runOnUiThread(() -> {
                            _adapter.setLoadingFull(true);

                            if (_handler != null) {
                                _handler.removeCallbacks(_typingRunnable);
                            }

                            setFinalResponse(message);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        // Handle full response error
                        runOnUiThread(() -> {
                            Toast.makeText(Chat.this, "Error receiving full response: " + error, Toast.LENGTH_SHORT).show();
                            Log.e("FullResponseError", error);
                        });
                    }
                }
        );

        handleIncomingMessage();

    }

    // region Backend

    private void handleIncomingMessage() {
        String message = getIntent().getStringExtra("message");

        if (message != null && !message.isEmpty()) {
            input.setText(message);
            String query = input.getText().toString().trim();

            if (!query.isEmpty()) {
                addNewQuestionResponse(query, "");

                _adapter.setLoading(true);
                _adapter.setQuestion(true);

                _handler = new Handler();
                _typingRunnable = typing();
                _handler.post(_typingRunnable);

                // Fetch gender first before sending the message
                fetchAvatar(mAuth.getCurrentUser().getUid(), gender -> {
                    Log.d("ChatActivity", "Gender fetched: " + gender);
                    userG = gender != null ? gender : "unknown"; // Default to "unknown" if null

                    // Now that gender is fetched, send the message
                    sendToClaude(query, "text");
                });
            }
        }
    }


    private void sendMessage(View view) {
        String query = input.getText().toString().trim();

        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter your query.", Toast.LENGTH_SHORT).show();
            return;
        }

        showDialog(query, view);

        addNewQuestionResponse(query, getString(R.string.typing));

        _adapter.setLoadingFull(false);

        _adapter.setQuestion(false);

        _handler = new Handler();
        _typingRunnable = typing();
        _handler.post(_typingRunnable);

        sendToClaude(query, "text");
    }

    private Runnable typing() {
        return new Runnable() {
            int dotCount = 0;

            @Override
            public void run() {
                String messageText = getString(R.string.typing);
                StringBuilder builder = new StringBuilder(messageText);

                for (int i = 0; i < dotCount; i++) {
                    builder.append(" . ");
                }

                int lastPosition = _questionResponseList.size() - 1;
                if (lastPosition >= 0) {
                    _questionResponseList.get(lastPosition).setResponse(builder.toString());
                    _adapter.notifyItemChanged(lastPosition);
                }

                dotCount = (dotCount + 1) % 4;

                _handler.postDelayed(this, 300);
            }
        };
    }

    private void sendToClaude(String text, String type) {
        if ((text == null || text.trim().isEmpty()) && type == null) {
            Toast.makeText(Chat.this, "Please enter some text or provide an image to send.", Toast.LENGTH_SHORT).show();
            Log.e("sendToClaude", "Attempted to send an empty query and no image.");
            return;
        }

        _adapter.setLoading(true);

        String finalText = _contextManager.buildPrompt(text);

        long startTime = System.currentTimeMillis();

        try {
            _linksApi.sendMessage(finalText, userN, userG, lastLanguage, type);
        } catch (JSONException e) {
            Log.e("ClaudeApi", "JSONException: " + e.getMessage());
            Toast.makeText(Chat.this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            _adapter.setLoading(false);
        }

        // Clear the text field
        if (input != null) {
            input.setText("");
        }
    }

    private void addNewQuestionResponse(String question, String response) {
        _questionResponseList.add(new QuestionResponse(question, response));
        _adapter.notifyItemInserted(_questionResponseList.size() - 1);
        _recyclerView.post(() -> _recyclerView.smoothScrollToPosition(_questionResponseList.size() - 1));
    }

    private void updateResponse(String chunk) {
        int lastPosition = _questionResponseList.size() - 1;
        if (lastPosition < 0) return;

        _currAnswer += chunk;
        _questionResponseList.get(lastPosition).setResponse(_currAnswer);

        RecyclerView.ViewHolder vh = _recyclerView.findViewHolderForAdapterPosition(lastPosition);
        if (vh instanceof QuestionResponseAdapter.ViewHolder) {
            QuestionResponseAdapter.ViewHolder holder = (QuestionResponseAdapter.ViewHolder) vh;

            holder.typeText(chunk, () -> holder.itemView.post(() -> scrollToItemBottom(lastPosition)));
        }
    }

    private void setFinalResponse(String finalResponse) {
        _currAnswer = "";
    }

    private void scrollToItemBottom(int position) {
        RecyclerView.ViewHolder vh = _recyclerView.findViewHolderForAdapterPosition(position);
        if (vh == null) return;

        if (!(vh instanceof QuestionResponseAdapter.ViewHolder)) return;

        QuestionResponseAdapter.ViewHolder holder = (QuestionResponseAdapter.ViewHolder) vh;

        holder.itemView.post(() -> {
            int rvHeight = _recyclerView.getHeight();
            int itemTop = holder.itemView.getTop();
            int itemHeight = holder.itemView.getHeight();
            int itemAbsoluteBottom = itemTop + itemHeight;
            int scrollAmount = itemAbsoluteBottom - rvHeight;

            if (scrollAmount > 0) {
                _recyclerView.smoothScrollBy(0, scrollAmount);
            }

        });
    }

    private void moveKeyboard() {
        LinearLayout editTextContainer = findViewById(R.id.currQuestion);
        final View rootLayout = findViewById(R.id.main);

        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int lastKeypadHeight = 0;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootLayout.getWindowVisibleDisplayFrame(r);

                int screenHeight = rootLayout.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                // Only update layout if the keyboard state changes
                if (keypadHeight != lastKeypadHeight) {
                    lastKeypadHeight = keypadHeight;

                    if (keypadHeight > screenHeight * 0.15) {
                        // Keyboard is open
                        Log.d("shani", "Keyboard open: moving container up");

                        int[] location = new int[2];
                        editTextContainer.getLocationOnScreen(location);

                        int containerY = location[1];
                        int distanceToMove = containerY - r.bottom + editTextContainer.getHeight();

                        if (distanceToMove > 0) {
                            editTextContainer.setTranslationY(-distanceToMove);
                            adjustRecyclerViewMargin(keypadHeight, editTextContainer.getHeight());
                        }
                    } else {
                        // Keyboard is closed
                        Log.d("shani", "Keyboard closed: resetting container position");

                        editTextContainer.setTranslationY(0);
                        resetRecyclerViewMargin();
                    }
                }
            }
        });
    }

    private void adjustRecyclerViewMargin(int keypadHeight, int containerHeight) {
        ViewGroup.LayoutParams recyclerParams = _recyclerView.getLayoutParams();
        if (recyclerParams instanceof ViewGroup.MarginLayoutParams) {
            float marginReductionFactor = 0.7f;
            int exactMargin = (int) ((keypadHeight - containerHeight) * marginReductionFactor);
            ((ViewGroup.MarginLayoutParams) recyclerParams).bottomMargin = Math.max(0, exactMargin);
            _recyclerView.setLayoutParams(recyclerParams);
        }
    }

    private void resetRecyclerViewMargin() {
        ViewGroup.LayoutParams recyclerParams = _recyclerView.getLayoutParams();
        if (recyclerParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) recyclerParams).bottomMargin = 0;
            _recyclerView.setLayoutParams(recyclerParams);
        }
    }

    // endregion

    @Override
    protected void onStart() {
        super.onStart();
        activeActivitiesCount++;
        Log.d("ChatActivity", "Activity started. Active activities count: " + activeActivitiesCount);
    }


    private void showDialog(String query, View view) {

        boolean containsKeyword = false;
        for (String keyword : keywordsList) {
            if (query.contains(keyword) || keyword.contains(query)) {
                containsKeyword = true;
                break;
            }
        }
        if (containsKeyword) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_chat, null);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();

            if (dialog.getWindow() != null) {

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            Button positiveButton = dialogView.findViewById(R.id.positiveButton);
            Button negativeButton = dialogView.findViewById(R.id.negativeButton);

            positiveButton.setOnClickListener(v -> {

                String phoneNumber = "tel:1-800-241-201"; // ער"ן
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse(phoneNumber));
                try {
                    startActivity(callIntent);
                } catch (SecurityException e) {

                    e.printStackTrace();
                    Toast.makeText(this, getString(R.string.TToHelplines), Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            });

            negativeButton.setOnClickListener(v -> {

                Log.e("shneor", "Location 3");

                // TODO להחליף למספר של ערן!!!

                getLocation.getLastLocation(view.getContext(), "0554424754", query.toString());

                Log.e("shneor", "Location 4");

                dialog.dismiss();
            });

            dialog.show();
        }

    }

    private void setSelectedButtonInNav() {

        getSupportFragmentManager().executePendingTransactions();
        BottomNavigationFragment bottomNavFragment = (BottomNavigationFragment) getSupportFragmentManager().findFragmentById(R.id.bottomNavFragment);
        if (bottomNavFragment != null) {

            bottomNavFragment.updateSelectedButton(R.id.rulerC);
        } else {
            Log.d("shneor", "Fragment is not initialized yet");
        }
    }

    private void updateRecyclerViewPosition() {

        LinearLayout editTextContainer = findViewById(R.id.currQuestion);
        final View rootLayout = findViewById(R.id.main);
        final RecyclerView _recyclerView = findViewById(R.id.recyclerView);


        Rect r = new Rect();
        rootLayout.getWindowVisibleDisplayFrame(r);
        int screenHeight = rootLayout.getRootView().getHeight();
        int keypadHeight = screenHeight - r.bottom;

        if (keypadHeight > screenHeight * 0.15) {
            ViewGroup.LayoutParams recyclerParams = _recyclerView.getLayoutParams();
            if (recyclerParams instanceof ViewGroup.MarginLayoutParams) {
                float marginReductionFactor = 0.7f;
                int exactMargin = (int) ((keypadHeight - editTextContainer.getHeight()) * marginReductionFactor);
                ((ViewGroup.MarginLayoutParams) recyclerParams).bottomMargin = Math.max(0, exactMargin);
                _recyclerView.setLayoutParams(recyclerParams);

                _recyclerView.post(() -> {
                    _recyclerView.smoothScrollToPosition(_recyclerView.getAdapter().getItemCount() - 1);
                });
            }
        }
    }

    private String formatResponse(String response) {

        return response.replace("\\n", "\n");
    }

    private void fetchAvatar(String userId, OnGenderFetchedCallback callback) {
        mDatabase.child(userId).child("gender").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("shanii" , "tryyww");

                if (dataSnapshot.exists()) {
                    Log.d("shanii" , "tryy3");

                    userG = dataSnapshot.getValue(String.class);
                    Log.d("shneor3", "User gender: " + userG);

                    // Trigger the callback with the fetched gender
                    if (callback != null) {
                        callback.onGenderFetched(userG);
                    }
                } else {
                    Log.d("shneor3", "No gender data available.");
                    if (callback != null) {
                        callback.onGenderFetched(null);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("shneor3", "Error fetching gender", databaseError.toException());
                if (callback != null) {
                    callback.onGenderFetched(null);
                }
            }
        });
    }

    interface OnGenderFetchedCallback {
        void onGenderFetched(String gender);
    }


    @Override
    protected void onPause() {

        super.onPause();

        if (_adapter != null) {

            _adapter.stopTextToSpeech();
        }
    }

    @Override
    protected void onDestroy() {

        if (_adapter != null) {

            _adapter.shutdownTextToSpeech();
        }
        super.onDestroy();
    }

}