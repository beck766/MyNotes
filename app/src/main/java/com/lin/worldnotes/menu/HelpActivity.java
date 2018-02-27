package com.lin.worldnotes.menu;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.lin.worldnotes.R;

public class HelpActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.menu_help);
        Button btn_saveAdvise = (Button) findViewById(R.id.btn_saveaAdvise);
        btn_saveAdvise.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(HelpActivity.this, "您的反馈信息已提交，感谢您的宝贵意见！", Toast.LENGTH_LONG)
                        .show();
            }
        });
    }
}
