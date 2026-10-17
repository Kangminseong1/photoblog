package com.cookandroid.photoviewer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailActivity extends AppCompatActivity {

    String site_url = "https://alstjde241.pythonanywhere.com";
    ImageView imageDetail;
    TextView titleView;
    TextView bodyView;
    int postId;

    ActivityResultLauncher<Intent> openEdit =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    titleView.setText(result.getData().getStringExtra("title"));
                    bodyView.setText(result.getData().getStringExtra("text"));
                    setResult(RESULT_OK);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        imageDetail = findViewById(R.id.imageDetail);
        titleView = findViewById(R.id.textDetailTitle);
        TextView dateView = findViewById(R.id.textDetailDate);
        bodyView = findViewById(R.id.textDetailBody);

        Intent intent = getIntent();
        postId = intent.getIntExtra("id", -1);
        titleView.setText(intent.getStringExtra("title"));
        dateView.setText(intent.getStringExtra("date"));
        bodyView.setText(intent.getStringExtra("text"));
        new LoadImage().execute(intent.getStringExtra("imageUrl"));
    }

    public void onClickEdit(View v) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("id", postId);
        intent.putExtra("title", titleView.getText().toString());
        intent.putExtra("text", bodyView.getText().toString());
        openEdit.launch(intent);
    }

    public void onClickDelete(View v) {
        new AlertDialog.Builder(this)
                .setMessage("이 게시물을 삭제할까요?")
                .setPositiveButton("삭제", (dialog, which) ->
                        new DeletePost().execute(site_url + "/api_root/Post/" + postId + "/"))
                .setNegativeButton("취소", null)
                .show();
    }

    private class DeletePost extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... urls) {
            try {
                String token = "bf46b8f9337d1d27b4ef2511514c798be1a954b8";
                HttpURLConnection conn = (HttpURLConnection) new URL(urls[0]).openConnection();
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestMethod("DELETE");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                int responseCode = conn.getResponseCode();
                conn.disconnect();
                return responseCode;
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                Toast.makeText(getApplicationContext(), "삭제되었습니다", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "삭제 실패 (코드: " + responseCode + ")", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class LoadImage extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(urls[0]).openConnection();
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                InputStream is = conn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
                conn.disconnect();
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageDetail.setImageBitmap(bitmap);
            }
        }
    }
}
