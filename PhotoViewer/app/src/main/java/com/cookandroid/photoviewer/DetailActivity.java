package com.cookandroid.photoviewer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailActivity extends AppCompatActivity {

    ImageView imageDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        imageDetail = findViewById(R.id.imageDetail);
        TextView titleView = findViewById(R.id.textDetailTitle);
        TextView dateView = findViewById(R.id.textDetailDate);
        TextView bodyView = findViewById(R.id.textDetailBody);

        Intent intent = getIntent();
        titleView.setText(intent.getStringExtra("title"));
        dateView.setText(intent.getStringExtra("date"));
        bodyView.setText(intent.getStringExtra("text"));
        new LoadImage().execute(intent.getStringExtra("imageUrl"));
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
