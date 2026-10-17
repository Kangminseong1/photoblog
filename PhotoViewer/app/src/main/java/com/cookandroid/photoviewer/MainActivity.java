package com.cookandroid.photoviewer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    TextView textView;
    String site_url = "http://10.0.2.2:8000";
    CloudImage taskDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
    }

    public void onClickDownload(View v) {
        if (taskDownload != null && taskDownload.getStatus() == AsyncTask.Status.RUNNING) {
            taskDownload.cancel(true);
        }
        taskDownload = new CloudImage();
        taskDownload.execute(site_url + "/api_root/Post/");
        Toast.makeText(getApplicationContext(), "Download", Toast.LENGTH_LONG).show();
    }

    public void onClickUpload(View v) {
        Intent intent = new Intent(this, PostActivity.class);
        startActivity(intent);
    }

    private class CloudImage extends AsyncTask<String, Integer, List<Bitmap>> {
        @Override
        protected List<Bitmap> doInBackground(String... urls) {
            List<Bitmap> bitmapList = new ArrayList<>();
            try {
                String apiUrl = urls[0];
                String token = "bf46b8f9337d1d27b4ef2511514c798be1a954b8";
                URL urlAPI = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection();
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    is.close();
                    conn.disconnect();
                    String strJson = result.toString();
                    JSONArray aryJson = new JSONArray(strJson);
                    for (int i = 0; i < aryJson.length(); i++) {
                        JSONObject post_json = (JSONObject) aryJson.get(i);
                        String imageUrl = post_json.getString("image");
                        if (!imageUrl.equals("")) {
                            imageUrl = imageUrl.replace("127.0.0.1", "10.0.2.2")
                                    .replace("localhost", "10.0.2.2");
                            URL myImageUrl = new URL(imageUrl);
                            HttpURLConnection imgConn = (HttpURLConnection) myImageUrl.openConnection();
                            imgConn.setConnectTimeout(3000);
                            imgConn.setReadTimeout(3000);
                            InputStream imgStream = imgConn.getInputStream();
                            Bitmap imageBitmap = BitmapFactory.decodeStream(imgStream);
                            if (imageBitmap != null) {
                                bitmapList.add(imageBitmap);
                            }
                            imgStream.close();
                            imgConn.disconnect();
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return bitmapList;
        }

        @Override
        protected void onPostExecute(List<Bitmap> images) {
            if (images.isEmpty()) {
                textView.setText("불러올 이미지가 없습니다.");
            } else {
                textView.setText("이미지 로드 성공!");
                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                ImageAdapter adapter = new ImageAdapter(images);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setAdapter(adapter);
            }
        }
    }
}