package com.cookandroid.photoviewer;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.OffsetDateTime;

public class PostActivity extends AppCompatActivity {

    String site_url = "http://10.0.2.2:8000";
    EditText editTitle;
    EditText editText;
    ImageView imagePreview;
    Uri selectedImageUri;
    PutPost taskUpload;

    ActivityResultLauncher<PickVisualMediaRequest> pickImage =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imagePreview.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        editTitle = findViewById(R.id.editTitle);
        editText = findViewById(R.id.editText);
        imagePreview = findViewById(R.id.imagePreview);
    }

    public void onClickSelectImage(View v) {
        pickImage.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    public void onClickPost(View v) {
        String title = editTitle.getText().toString();
        String text = editText.getText().toString();
        if (title.isEmpty() || text.isEmpty() || selectedImageUri == null) {
            Toast.makeText(getApplicationContext(), "제목, 내용, 이미지를 모두 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (taskUpload != null && taskUpload.getStatus() == AsyncTask.Status.RUNNING) {
            return;
        }
        taskUpload = new PutPost(title, text, selectedImageUri);
        taskUpload.execute(site_url + "/api_root/Post/");
    }

    private class PutPost extends AsyncTask<String, Void, Integer> {
        String title;
        String text;
        Uri imageUri;

        PutPost(String title, String text, Uri imageUri) {
            this.title = title;
            this.text = text;
            this.imageUri = imageUri;
        }

        @Override
        protected Integer doInBackground(String... urls) {
            String boundary = "----PhotoViewerBoundary" + System.currentTimeMillis();
            String lineEnd = "\r\n";
            try {
                InputStream is = getContentResolver().openInputStream(imageUri);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] chunk = new byte[4096];
                int n;
                while ((n = is.read(chunk)) != -1) {
                    buffer.write(chunk, 0, n);
                }
                is.close();
                byte[] imageBytes = buffer.toByteArray();

                String mimeType = getContentResolver().getType(imageUri);
                String fileName = "image/png".equals(mimeType) ? "upload.png" : "upload.jpg";

                String token = "bf46b8f9337d1d27b4ef2511514c798be1a954b8";
                URL urlAPI = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection();
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                String publishedDate = OffsetDateTime.now().withNano(0).toString();
                String[][] fields = {{"author", "1"}, {"title", title}, {"text", text}, {"published_date", publishedDate}};
                for (String[] field : fields) {
                    out.writeBytes("--" + boundary + lineEnd);
                    out.writeBytes("Content-Disposition: form-data; name=\"" + field[0] + "\"" + lineEnd);
                    out.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd + lineEnd);
                    out.write(field[1].getBytes("UTF-8"));
                    out.writeBytes(lineEnd);
                }
                out.writeBytes("--" + boundary + lineEnd);
                out.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"" + fileName + "\"" + lineEnd);
                out.writeBytes("Content-Type: " + mimeType + lineEnd + lineEnd);
                out.write(imageBytes);
                out.writeBytes(lineEnd);
                out.writeBytes("--" + boundary + "--" + lineEnd);
                out.flush();
                out.close();

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
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                Toast.makeText(getApplicationContext(), "게시 성공!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "게시 실패 (코드: " + responseCode + ")", Toast.LENGTH_LONG).show();
            }
        }
    }
}
