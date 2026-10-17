package com.cookandroid.photoviewer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class EditActivity extends AppCompatActivity {

    String site_url = "https://alstjde241.pythonanywhere.com";
    EditText editTitle;
    EditText editText;
    int postId;
    UpdatePost taskUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        editTitle = findViewById(R.id.editTitle);
        editText = findViewById(R.id.editText);

        Intent intent = getIntent();
        postId = intent.getIntExtra("id", -1);
        editTitle.setText(intent.getStringExtra("title"));
        editText.setText(intent.getStringExtra("text"));
    }

    public void onClickSave(View v) {
        String title = editTitle.getText().toString();
        String text = editText.getText().toString();
        if (title.isEmpty() || text.isEmpty()) {
            Toast.makeText(getApplicationContext(), "제목과 내용을 모두 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (taskUpdate != null && taskUpdate.getStatus() == AsyncTask.Status.RUNNING) {
            return;
        }
        taskUpdate = new UpdatePost(title, text);
        taskUpdate.execute(site_url + "/api_root/Post/" + postId + "/");
    }

    private class UpdatePost extends AsyncTask<String, Void, Integer> {
        String title;
        String text;

        UpdatePost(String title, String text) {
            this.title = title;
            this.text = text;
        }

        @Override
        protected Integer doInBackground(String... urls) {
            try {
                JSONObject body = new JSONObject();
                body.put("title", title);
                body.put("text", text);

                String token = "bf46b8f9337d1d27b4ef2511514c798be1a954b8";
                HttpURLConnection conn = (HttpURLConnection) new URL(urls[0]).openConnection();
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestMethod("PATCH");
                conn.setDoOutput(true);
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                OutputStream out = conn.getOutputStream();
                out.write(body.toString().getBytes("UTF-8"));
                out.close();

                int responseCode = conn.getResponseCode();
                conn.disconnect();
                return responseCode;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Toast.makeText(getApplicationContext(), "수정되었습니다", Toast.LENGTH_SHORT).show();
                Intent result = new Intent();
                result.putExtra("title", title);
                result.putExtra("text", text);
                setResult(RESULT_OK, result);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "수정 실패 (코드: " + responseCode + ")", Toast.LENGTH_LONG).show();
            }
        }
    }
}
