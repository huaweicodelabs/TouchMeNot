package com.huawei.touchmenot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.huawei.touchmenot.kotlin.main.SplashActivity;

public class EntryChoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_choice);

        findViewById(R.id.btn_kotlin).setOnClickListener(view -> {
            Intent intent = new Intent(EntryChoiceActivity.this, SplashActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btn_java).setOnClickListener(view -> {
            Intent intent = new Intent(EntryChoiceActivity.this, com.huawei.touchmenot.java.main.SplashActivity.class);
            startActivity(intent);
            finish();
        });
    }
}