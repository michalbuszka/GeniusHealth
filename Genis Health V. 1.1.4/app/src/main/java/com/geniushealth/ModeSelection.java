package com.geniushealth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class ModeSelection extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_mode_selection);
        findViewById(R.id.interferentialModeIcon).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(this, InterferentialMode.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.burstModulatedModeIcon).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(this, BurstModulatedMode.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.manualModeIcon).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(this, ManualMode.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.otherModeIcon).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(this, OtherMode.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.exitButton).setOnClickListener(v -> {
            finish();
            System.exit(0);
        });
    }
}