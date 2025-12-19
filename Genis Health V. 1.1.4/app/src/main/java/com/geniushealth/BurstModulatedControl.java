package com.geniushealth;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BurstModulatedControl extends AppCompatActivity {
    protected Xionis xionis;
    protected long time1, time2;
    protected TextView[] frequencyInfo = new TextView[6];
    protected TextView[] amplitudeInfo = new TextView[6];
    protected TextView[] timeInfo = new TextView[6];
    protected TextView[] showProgramNum = new TextView[6];
    protected Button[] amplitudeButtonPlus = new Button[6];
    protected Button[] amplitudeButtonMinus = new Button[6];
    protected Button[] pauseButton = new Button[6];
    protected int[] amplitudeOperation = new int[6];
    protected int[] programNum = new int[6];
    protected boolean[] isDeviceOnOff = new boolean[6];
    protected int[] onOffOperation = new int[6];
    protected boolean[] runStatus = new boolean[6];
    protected boolean startStatus = false;
    protected boolean canRead = true;
    protected boolean runAllDevices = false;
    protected boolean stopAllDevices = false;
    protected boolean isNotClosed = true;
    protected BluetoothConnection bluetoothConnection;
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void disableInputs ()
    {
        Drawable disabledOff = getResources().getDrawable(R.drawable.stopb_a);
        Drawable disabledMinus = getResources().getDrawable(R.drawable.minus_a);
        Drawable disabledPlus = getResources().getDrawable(R.drawable.plus_a);
        for (int i = 0; i < 6; i++)
        {
            if (!isDeviceOnOff[i])
            {
                amplitudeButtonPlus[i].setEnabled(false);
                amplitudeButtonPlus[i].setForeground(disabledPlus);
                amplitudeButtonMinus[i].setEnabled(false);
                amplitudeButtonMinus[i].setForeground(disabledMinus);
                pauseButton[i].setEnabled(false);
                pauseButton[i].setForeground(disabledOff);
            }
        }
    }
    protected void timeCounter (TextView text, int index)
    {
        int timeLeft = xionis.xionisDevice[index].register[Xionis.Index.timer];
        String minutes = String.valueOf(timeLeft / 60);
        String seconds = String.valueOf(timeLeft % 60);
        if (timeLeft / 60 < 10)
        {
            minutes = "0"+timeLeft / 60;
        }
        if (timeLeft % 60 < 10)
        {
            seconds = "0"+timeLeft % 60;
        }
        String finalMinutes = minutes;
        String finalSeconds = seconds;
        String finalText = "Time left: "+ finalMinutes +":"+ finalSeconds;
        runOnUiThread(() -> text.setText(finalText));
    }
    protected Thread InfoThread = new Thread()
    {
        public void run ()
        {
            while (isNotClosed)
            {
                for (int i = 0; i < 6; i++)
                {
                    if (isDeviceOnOff[i])
                    {
                        xionis.showAmplitude(amplitudeInfo[i], i);
                        timeCounter(timeInfo[i], i);
                        xionis.showFrequency(frequencyInfo[i], i);
                    }
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void control (int i)
    {
        if (onOffOperation[i] != 0)
        {
            byte[] infoFrame = Modbus.prepareReadNRegistersFrame(0x50 + i, Xionis.Address.onOff, 4);
            switch (onOffOperation[i])
            {
                case 1:
                    xionis.xionisDevice[i].setRegister(Xionis.Address.onOff, 0);
                    xionis.xionisDevice[i].sendFrame(infoFrame, Xionis.Index.onOff);
                    onOffOperation[i] = 0;
                    break;
                case 2:
                    xionis.xionisDevice[i].setRegister(Xionis.Address.onOff, 1);
                    xionis.xionisDevice[i].sendFrame(infoFrame, Xionis.Index.onOff);
                    onOffOperation[i] = 0;
                    break;
            }
        }
        if (amplitudeOperation[i] != 0)
        {
            if (amplitudeOperation[i] == 1)
            {
                if (xionis.xionisDevice[i].register[Xionis.Index.amplitude] - Settings.maxAmplitude / 100 >= 0)
                {
                    xionis.xionisDevice[i].setRegister(Xionis.Address.amplitude, xionis.xionisDevice[i].register[Xionis.Index.amplitude] - (Settings.maxAmplitude / 100));
                    byte[] infoFrame = Modbus.prepareReadNRegistersFrame(0x50 + i, Xionis.Address.amplitude, 4);
                    xionis.xionisDevice[i].sendFrame(infoFrame, Xionis.Index.amplitude);
                    time1 = System.currentTimeMillis();
                }
            }
            else if (amplitudeOperation[i] == 2)
            {
                if (runStatus[i])
                {
                    if (xionis.xionisDevice[i].register[Xionis.Index.amplitude] + Settings.maxAmplitude / 100 <= Settings.maxAmplitude)
                    {
                        xionis.xionisDevice[i].setRegister(Xionis.Address.amplitude, xionis.xionisDevice[i].register[Xionis.Index.amplitude] + (Settings.maxAmplitude / 100));
                        byte[] infoFrame = Modbus.prepareReadNRegistersFrame(0x50 + i, Xionis.Address.amplitude, 4);
                        xionis.xionisDevice[i].sendFrame(infoFrame, Xionis.Index.amplitude);
                        time1 = System.currentTimeMillis();
                    }
                }
                else
                {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), Messages.EN.amplitudePlusWhenChannelNotRunning, Toast.LENGTH_SHORT).show());
                }
            }
            amplitudeOperation[i] = 0;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void checkStatusUpdate (int i)
    {
        boolean status;
        if (xionis.xionisDevice[i].register[Xionis.Index.onOff] == 1)
        {
            status = true;
        }
        else
        {
            status = false;
        }
        if (runStatus[i] != status)
        {
            updatePauseStatus(i);
        }
    }
    protected Thread MainThread = new Thread ()
    {
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void run ()
        {
            xionis.createDevices(bluetoothConnection);
            InfoThread.start();
            while (isNotClosed)
            {
                if (canRead)
                {
                    for (int i = 0; i < 6; i++)
                    {
                        if (isDeviceOnOff[i])
                        {
                            byte[] infoFrame = Modbus.prepareReadNRegistersFrame(0x50 + i, Xionis.Address.onOff, 4);
                            xionis.xionisDevice[i].sendFrame(infoFrame, Xionis.Index.onOff);
                            if (!canRead)
                            {
                                break;
                            }
                        }
                    }
                }
                else
                {
                    time2 = System.currentTimeMillis();
                    if (time2 - time1 > 1000)
                    {
                        canRead = true;
                    }
                }
                if (runAllDevices)
                {
                    xionis.xionisDevice[0].setRegisterForAllDevices(Xionis.Address.onOff, 1);
                    runAllDevices = false;
                }
                if (stopAllDevices)
                {
                    xionis.xionisDevice[0].setRegisterForAllDevices(Xionis.Address.onOff, 0);
                    stopAllDevices = false;
                }
                for (int i = 0; i < 6; i++)
                {
                    if (isDeviceOnOff[i])
                    {
                        checkStatusUpdate(i);
                        control(i);
                    }
                }
            }
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void updatePauseStatus (int index)
    {
        if (xionis.xionisDevice[index].register[Xionis.Index.onOff] == 1)
        {
            runStatus[index] = true;
            Drawable enabledChannel = getResources().getDrawable(R.drawable.stopb);
            runOnUiThread(() -> pauseButton[index].setForeground(enabledChannel));
        }
        else if (xionis.xionisDevice[index].register[Xionis.Index.onOff] == 0)
        {
            runStatus[index] = false;
            Drawable disabledChannel = getResources().getDrawable(R.drawable.play);
            runOnUiThread(() -> pauseButton[index].setForeground(disabledChannel));
        }
    }
    protected void setupPauseButton (Button b, int index)
    {
        b.setOnClickListener(v -> {
            runStatus[index] = !runStatus[index];
            if (runStatus[index])
            {
                canRead = false;
                onOffOperation[index] = 2;
            }
            else
            {
                canRead = false;
                onOffOperation[index] = 1;
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void setupAmplitudePlusButton (Button b, int index)
    {
        b.setOnClickListener(v -> {
            canRead = false;
            amplitudeOperation[index] = 2;
        });
    }
    protected void setupAmplitudeMinusButton (Button b, int index)
    {
        b.setOnClickListener(v -> {
            canRead = false;
            amplitudeOperation[index] = 1;
        });
    }
    protected void showProgramFunc (TextView text, int index)
    {
        String[] program = getResources().getStringArray(R.array.burstProgramResource);
        final String programName = program[programNum[index]];
        runOnUiThread(() -> text.setText(programName));
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void setupInputs ()
    {
        amplitudeInfo[0] = findViewById(R.id.burstAmplitude1);
        amplitudeInfo[1] = findViewById(R.id.burstAmplitude2);
        amplitudeInfo[2] = findViewById(R.id.burstAmplitude3);
        amplitudeInfo[3] = findViewById(R.id.burstAmplitude4);
        amplitudeInfo[4] = findViewById(R.id.burstAmplitude5);
        amplitudeInfo[5] = findViewById(R.id.burstAmplitude6);
        timeInfo[0] = findViewById(R.id.burstTimeLeft1);
        timeInfo[1] = findViewById(R.id.burstTimeLeft2);
        timeInfo[2] = findViewById(R.id.burstTimeLeft3);
        timeInfo[3] = findViewById(R.id.burstTimeLeft4);
        timeInfo[4] = findViewById(R.id.burstTimeLeft5);
        timeInfo[5] = findViewById(R.id.burstTimeLeft6);
        frequencyInfo[0] = findViewById(R.id.burstFrequency1);
        frequencyInfo[1] = findViewById(R.id.burstFrequency2);
        frequencyInfo[2] = findViewById(R.id.burstFrequency3);
        frequencyInfo[3] = findViewById(R.id.burstFrequency4);
        frequencyInfo[4] = findViewById(R.id.burstFrequency5);
        frequencyInfo[5] = findViewById(R.id.burstFrequency6);
        amplitudeButtonPlus[0] = findViewById(R.id.burstAmplitudePlus1);
        amplitudeButtonPlus[1] = findViewById(R.id.burstAmplitudePlus2);
        amplitudeButtonPlus[2] = findViewById(R.id.burstAmplitudePlus3);
        amplitudeButtonPlus[3] = findViewById(R.id.burstAmplitudePlus4);
        amplitudeButtonPlus[4] = findViewById(R.id.burstAmplitudePlus5);
        amplitudeButtonPlus[5] = findViewById(R.id.burstAmplitudePlus6);
        amplitudeButtonMinus[0] = findViewById(R.id.burstAmplitudeMinus1);
        amplitudeButtonMinus[1] = findViewById(R.id.burstAmplitudeMinus2);
        amplitudeButtonMinus[2] = findViewById(R.id.burstAmplitudeMinus3);
        amplitudeButtonMinus[3] = findViewById(R.id.burstAmplitudeMinus4);
        amplitudeButtonMinus[4] = findViewById(R.id.burstAmplitudeMinus5);
        amplitudeButtonMinus[5] = findViewById(R.id.burstAmplitudeMinus6);
        pauseButton[0] = findViewById(R.id.burstPause1);
        pauseButton[1] = findViewById(R.id.burstPause2);
        pauseButton[2] = findViewById(R.id.burstPause3);
        pauseButton[3] = findViewById(R.id.burstPause4);
        pauseButton[4] = findViewById(R.id.burstPause5);
        pauseButton[5] = findViewById(R.id.burstPause6);
        showProgramNum[0] = findViewById(R.id.burstShowProgram1);
        showProgramNum[1] = findViewById(R.id.burstShowProgram2);
        showProgramNum[2] = findViewById(R.id.burstShowProgram3);
        showProgramNum[3] = findViewById(R.id.burstShowProgram4);
        showProgramNum[4] = findViewById(R.id.burstShowProgram5);
        showProgramNum[5] = findViewById(R.id.burstShowProgram6);
        Button startButton =  findViewById(R.id.startProgramBurst);
        for (int i = 0; i < 6; i++)
        {
            if (isDeviceOnOff[i])
            {
                runStatus[i] = false;
                setupAmplitudePlusButton(amplitudeButtonPlus[i], i);
                setupAmplitudeMinusButton(amplitudeButtonMinus[i], i);
                setupPauseButton(pauseButton[i], i);
                showProgramFunc(showProgramNum[i], i);
            }
        }
        startButton.setOnClickListener(v -> {
            Drawable iconStop = getResources().getDrawable(R.drawable.stop);
            Drawable iconStart = getResources().getDrawable(R.drawable.play);
            startStatus=!startStatus;
            if (startStatus)
            {
                canRead = true;
                runAllDevices = true;
                startButton.setForeground(iconStop);
            }
            else
            {
                stopAllDevices = true;
                startButton.setForeground(iconStart);
            }
        });
        disableInputs();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        xionis = new Xionis(this);
        isDeviceOnOff = getIntent().getBooleanArrayExtra("deviceOnOff");
        programNum = getIntent().getIntArrayExtra("programNumber");
        bluetoothConnection = (BluetoothConnection)getApplicationContext();
        setContentView(R.layout.activity_burst_modulated_control);
        setupInputs();
        MainThread.start();
        findViewById(R.id.burstControlBackArrow).setOnClickListener(v -> {
            if (xionis.checkIfDevicesAreOff())
            {
                isNotClosed = false;
                Intent intent = new Intent();
                intent.setClass(BurstModulatedControl.this, BurstModulatedMode.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            }
            else
            {
                Toast.makeText(getApplicationContext(), Messages.EN.mustTurnOffAllChannelsBeforeReturning, Toast.LENGTH_SHORT).show();
            }
        });
    }
}