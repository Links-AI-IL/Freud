package com.links.freud.backend;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ClaudeContextManager {

    // region Members

    private String _previousDocument;

    private List<Message> _conversationHistory;

    // endregion

    // region Properties

    public ClaudeContextManager() {
        _conversationHistory = new ArrayList<>();
    }

    // endregion

    // region Public Methods

    public String buildPrompt(String newQuery) {
        StringBuilder fullPrompt = new StringBuilder();

        if (_previousDocument != null && !_previousDocument.isEmpty()) {
            fullPrompt.append("Previous document content: ").append(_previousDocument).append("\n\n");
        }

        for (Message message : _conversationHistory) {
            fullPrompt.append(message.role).append(": ").append(message.content).append("\n");
        }

        if (newQuery != null && !newQuery.trim().isEmpty()) {
            fullPrompt.append("user: ").append(newQuery);
        }
        else {
            Log.e("ClaudeContextManager", "Attempted to build prompt with an empty user query.");
        }

        return fullPrompt.toString();
    }

    // endregion

}