package com.geniushealth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

public class Lock extends AppCompatActivity {
    private boolean checkLoginPin (int pin)
    {
        if (pin == Settings.appPin)
        {
            return true;
        }
        return false;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.wait_for_conntection_layout);
        BluetoothConnection connection = (BluetoothConnection)getApplicationContext();
        connection.setupBluetooth();
        setContentView(R.layout.activity_lock);
        EditText numInput = findViewById(R.id.pinInput);
        findViewById(R.id.pinButton).setOnClickListener(v -> {
            if (checkLoginPin(Integer.parseInt(numInput.getText().toString())))
            {
                Intent modeSelection = new Intent();
                modeSelection.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                modeSelection.setClass(getApplicationContext(), ModeSelection.class);
                modeSelection.putExtra("deviceName", "XM-15");
                startActivity(modeSelection);
                finish();
            }
            else
            {
                numInput.setError("Invalid pin!");
                numInput.setText("");
            }
        });
        findViewById(R.id.exitButton).setOnClickListener(v -> {
            finish();
            System.exit(0);
        });
    }
}