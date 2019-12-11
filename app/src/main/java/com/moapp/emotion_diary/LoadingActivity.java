package com.moapp.emotion_diary;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;

import io.realm.Realm;

public class LoadingActivity extends AppCompatActivity {

    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        realm = Realm.getDefaultInstance();
        startLoading();
    }

    private void startLoading() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (realm.where(SentiDict.class).equalTo("gram", 1).findFirst() != null){
                    finish();
                } else {
                    InputStreamReader is = new InputStreamReader(getResources().openRawResource(R.raw.dict));
                    BufferedReader reader = new BufferedReader(is);
                    try {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String str[] = line.split(",");
                            realm.beginTransaction();
                            SentiDict sentiDict = realm.createObject(SentiDict.class, str[0]);
                            sentiDict.setScore(Integer.parseInt(str[1]));
                            sentiDict.setGram(str[0].split(" ").length);
                            realm.commitTransaction();
                        }
                    }
                    catch (IOException ex) {
                        // handle exception
                    }
                    finally {
                        try {
                            is.close();
                        }
                        catch (IOException e) {
                            // handle exception
                        }
                    }
                }

            }
        }, 2000);
    }
}
