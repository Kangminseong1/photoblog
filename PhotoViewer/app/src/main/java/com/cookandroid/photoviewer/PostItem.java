package com.cookandroid.photoviewer;

import android.graphics.Bitmap;

public class PostItem {
    int id;
    String title;
    String text;
    String date;
    String imageUrl;
    Bitmap image;

    PostItem(int id, String title, String text, String date, String imageUrl, Bitmap image) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.date = date;
        this.imageUrl = imageUrl;
        this.image = image;
    }
}
