package com.lin.worldnotes;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.File;

public class PhotoViewActivity extends Activity {
    public static final String EXTRA_PATH = "path";
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        iv = new ImageView(this);
        setContentView(iv);
        String path = getIntent().getStringExtra(EXTRA_PATH);
        if (path != null) {
            iv.setImageURI(Uri.fromFile(new File(path)));
        } else {
            finish();
        }
    }


}
