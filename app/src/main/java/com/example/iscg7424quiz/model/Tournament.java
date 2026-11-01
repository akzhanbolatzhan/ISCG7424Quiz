package com.example.iscg7424quiz.model;


import java.util.List;

public class Tournament {

    private String id;
    private String name;
    private String category;
    private int categoryId;
    private String difficulty;
    private String startDate;
    private String endDate;
    private int rating;
    private List<Question> questions;

    public Tournament() {
    }

    public Tournament(String id, String name,
                      String category, int categoryId,
                      String difficulty, String startDate,
                      String endDate, int rating,
                      List<Question> questions) {

        this.id = id;
        this.name = name;
        this.category = category;
        this.categoryId = categoryId;
        this.difficulty = difficulty;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rating = rating;
        this.questions = questions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}