package com.links.freud;

public class QuestionResponse {

    private String _question;

    private String _response;

    // endregion

    // region Constructors

    public QuestionResponse() {}

    public QuestionResponse(String question, String response) {
        _question = question;
        _response = response;
    }

    // endregion

    // region Properties

    public String getQuestion() {
        return _question;
    }

    public void setQuestion(String question) {
        _question = question;
    }

    public String getResponse() {
        return _response;
    }

    public void setResponse(String response) {
        _response = response;
    }

    // endregion

}
