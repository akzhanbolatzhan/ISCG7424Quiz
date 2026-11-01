package com.example.iscg7424quiz.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QuestionResponse {

    @SerializedName("response_code")
    private int responseCode;

    private List<Question> results;

    public int getResponseCode() {
        return responseCode;
    }

    public List<Question> getResults() {
        return results;
    }
}