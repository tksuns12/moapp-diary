package com.moapp.emotion_diary;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

//일기 데이터를 저장하는 클래스

public class DiaryData extends RealmObject {
    private String content;
    private int month;
    private int date;
    private int year;
    private int emotion;

    @PrimaryKey
    private int uniqueKey;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public long getUniqueKey() {
        return uniqueKey;
    }


    public int getEmotion() {
        return emotion;
    }

    public void setEmotion(int emotion) {
        this.emotion = emotion;
    }

}
