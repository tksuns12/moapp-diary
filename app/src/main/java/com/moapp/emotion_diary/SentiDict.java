package com.moapp.emotion_diary;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

//감성 사전 데이터베이스

public class SentiDict extends RealmObject {

    private int score;
    private int gram;

    @PrimaryKey
    private String word;


    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getGram() {
        return gram;
    }

    public void setGram(int gram) {
        this.gram = gram;
    }
}
