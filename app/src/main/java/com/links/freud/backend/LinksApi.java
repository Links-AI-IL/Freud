package com.links.freud.backend;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LinksApi {

    // region Constant Members

    private final String API_URL = "https://claudeserver.onrender.com";

    private final int API_PORT = 443;

    private final String API_QUERY = "/query";

    private final String API_UPLOAD = "/upload";

    private final String API_TEMPLATE = "%s:%d%s";

    public static final String APP_ID = "FREUD";

    // endregion

    // region Members

    private OkHttpClient _client;

    private static String _prompt;

    private List<Message> _messages;

    private ChunkCallback _chunkCallback;

    private FullResponseCallback _fullResponseCallback;

    private Content content;

    // endregion

    // region Interface ChunkCallback

    public interface ChunkCallback {

        void onResponse(String message);

        void onError(String error);
    }

    // endregion

    // region Interface FullResponseCallback

    public interface FullResponseCallback {

        void onResponse(String message);

        void onError(String error);
    }

    public interface UploadCallback {
        void onSuccess(String response);
        void onFailure(String errorMessage);
    }

    // endregion

    // region Constructor

    public LinksApi(String prompt, ChunkCallback chunkCallback, FullResponseCallback fullResponseCallback) {
        _prompt = prompt;
        _messages = new ArrayList<>();
        _chunkCallback = chunkCallback;
        _fullResponseCallback = fullResponseCallback;
        _client = new OkHttpClient();
    }

    // endregion

    // region Public Methods

    public void sendMessage(String userMessage, String name, String gender, String language, String type) throws JSONException {
        content = new Content("text", userMessage);
        _messages.add(new Message("user", content));

        Gson gson = new Gson();
        String jsonMessages = gson.toJson(_messages);

        RequestBody formBody = new FormBody.Builder()
                .add("messages", jsonMessages)
                .add("name", name)
                .add("gender", gender)
                .add("language", language)
                .add("appId", APP_ID)
                .build();

        Request request = new Request.Builder()
                .url(getApiQuery())
                .post(formBody)
                .build();

        _client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("LinksApi onFailure", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.body().byteStream()))) {

                        String line;
                        StringBuilder fullResponseBuilder = new StringBuilder();

                        while ((line = reader.readLine()) != null) {
                            if (!line.startsWith("data: ")) continue;

                            String jsonPart = line.substring(6).trim();

                            try {
                                JSONObject obj = new JSONObject(jsonPart);

                                if (obj.has("text")) {
                                    String text = obj.getString("text");
                                    fullResponseBuilder.append(text);

                                    if (_chunkCallback != null) {
                                        _chunkCallback.onResponse(text);
                                    }
                                } else if (obj.has("error")) {
                                    String error = obj.getString("error");

                                    if (_chunkCallback != null) {
                                        _chunkCallback.onError(error);
                                    }
                                }

                            } catch (Exception e) {
                                Log.e("SSE_PARSE", "Failed to parse: " + jsonPart, e);
                            }
                        }

                        // Notify full response callback once the entire response is received
                        if (_fullResponseCallback != null) {
                            String finalResponse = fullResponseBuilder.toString();
                            Log.d("LinksApi FINAL RESPONSE",finalResponse);

                            content = new Content("text", finalResponse);
                            _messages.add(new Message("assistant", content));

                            _fullResponseCallback.onResponse(finalResponse);
                        }
                    }
                }

                else {
                    String errorMessage = "HTTP Error Code: " + response.code();
                    if (_chunkCallback != null) {
                        _chunkCallback.onError(errorMessage);
                    }
                    if (_fullResponseCallback != null) {
                        _fullResponseCallback.onError(errorMessage);
                    }
                }
            }
        });
    }

    public void uploadImage(FileRequestModel requestModel, UploadCallback callback) {
        File file = requestModel.getFile();

        RequestBody fileBody = RequestBody.create(file, MediaType.parse("application/octet-stream"));

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .addFormDataPart("type", requestModel.getType())
                .build();

        Request request = new Request.Builder()
                .url(getApiUpload())
                .post(requestBody)
                .build();

        _client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseString = response.body().string();

                callback.onSuccess(responseString);
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e.getMessage());
            }
        });
    }

    // endregion

    // region Private Methods

    private String getApiQuery() {
        return API_URL + API_QUERY;
    }

    private String getApiUpload() {
        return API_URL + API_UPLOAD;
    }

    // endregion

}
