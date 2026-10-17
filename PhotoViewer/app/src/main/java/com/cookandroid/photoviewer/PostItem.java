package com.cookandroid.photoviewer;

import android.graphics.Bitmap;

public class PostItem {
    String title;
    String date;
    Bitmap image;

    PostItem(String title, String date, Bitmap image) {
        this.title = title;
        this.date = date;
        this.image = image;
    }
}
