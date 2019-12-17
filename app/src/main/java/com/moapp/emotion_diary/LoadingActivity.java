package com.moapp.emotion_diary;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import io.realm.Realm;

public class LoadingActivity extends Activity {

    Realm realm;
    boolean isFirst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Realm.init(this);
        setContentView(R.layout.activity_loading);
        //스레드 시작
        InitializeDict initializeDict = new InitializeDict();
        initializeDict.execute(10);
        isFirst = false;
    }

    //AsyncTask를 상속한 스레드 구현
    private class InitializeDict extends AsyncTask<Integer, Integer, Integer> {
        ProgressBar progressBar;
        int total;
        int progress;

        //스레드 시작 전 초기화 작업
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            total = 14778;
            progressBar = findViewById(R.id.progressBar);
            progressBar.setMax(total);
            progress = 0;
        }

        //진행상황이 업데이트 되었을 때 수행되는 작업
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }

        //스레드 작업 내용: 사전 파일을 읽어와서 SentiDict 타입으로 데이터베이스로 저장한다.
        @Override
        protected Integer doInBackground(Integer... integers) {
            realm = Realm.getDefaultInstance();
                InputStreamReader is = null;
                try {
                    is = new InputStreamReader(getResources().openRawResource(R.raw.dict), "euc-kr");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                BufferedReader reader = new BufferedReader(is);
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] str = line.split(",");
                        if(realm.where(SentiDict.class).equalTo("word", str[0]).findFirst() != null){
                            progress++;
                            continue;
                        }
                        isFirst=true;
                        realm.beginTransaction();
                        SentiDict sentiDict = realm.createObject(SentiDict.class, str[0]);
                        sentiDict.setScore(Integer.parseInt(str[1]));
                        sentiDict.setGram(str[0].split(" ").length);
                        realm.commitTransaction();
                        progress++;
                        publishProgress(progress);

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
            return null;
        }


        //스레드 작업이 끝난 후 수행할 작업.
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(isFirst){
            Toast.makeText(getApplicationContext(), "사전 DB 작업이 완료되었습니다.", Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }
}
