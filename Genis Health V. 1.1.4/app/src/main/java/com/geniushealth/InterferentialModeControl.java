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

public class InterferentialModeControl extends AppCompatActivity {
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
    protected void showFrequency (TextView text, int index)
    {
        int value = xionis.xionisDevice[index].register[Xionis.Index.baseFrequency] - xionis.xionisDevice[index+1].register[Xionis.Index.baseFrequency];
        int difference = Math.abs(value);
        String frequency = String.valueOf(difference);
        final String finalText = "Frequency: "+ frequency + " Hz";
        runOnUiThread(() -> text.setText(finalText));
    }
    protected BluetoothConnection bluetoothConnection;
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void disableInputs ()
    {
        Drawable disabledOff = getResources().getDrawable(R.drawable.stopb_a);
        Drawable disabledMinus = getResources().getDrawable(R.drawable.minus_a);
        Drawable disabledPlus = getResources().getDrawable(R.drawable.plus_a);
        for (int i = 0; i < Settings.amountOfDevices/2; i++)
        {
            if (!isDeviceOnOff[i*2])
            {
                amplitudeButtonPlus[i].setEnabled(false);
                amplitudeButtonPlus[i].setForeground(disabledPlus);
                amplitudeButtonMinus[i].setEnabled(false);
                amplitudeButtonMinus[i].setForeground(disabledMinus);
                pauseButton[i].setEnabled(false);
                pauseButton[i].setForeground(disabledOff);
            }
        }
        for (int i = Settings.amountOfDevices/2; i < Settings.amountOfDevices; i++)
        {
            amplitudeButtonPlus[i].setEnabled(false);
            amplitudeButtonPlus[i].setForeground(disabledPlus);
            amplitudeButtonMinus[i].setEnabled(false);
            amplitudeButtonMinus[i].setForeground(disabledMinus);
            pauseButton[i].setEnabled(false);
            pauseButton[i].setForeground(disabledOff);
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
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void run ()
        {
            while (isNotClosed)
            {
                for (int i = 0; i < Settings.amountOfDevices/2; i++)
                {
                    if (isDeviceOnOff[i*2])
                    {
                        xionis.showAmplitude(amplitudeInfo[i], i*2);
                        timeCounter(timeInfo[i], i*2);
                        showFrequency(frequencyInfo[i], i*2);
                        updatePauseStatus(i);
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
                if (xionis.xionisDevice[i].register[Xionis.Index.onOff] == 1)
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
                amplitudeOperation[i] = 0;
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
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
                        control(i);
                    }
                }
            }
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void updatePauseStatus (int index)
    {
        if (xionis.xionisDevice[index*2].register[Xionis.Index.onOff] == 1)
        {
            runStatus[index] = true;
            Drawable enabledChannel = getResources().getDrawable(R.drawable.stopb);
            runOnUiThread(() -> pauseButton[index].setForeground(enabledChannel));
        }
        else if (xionis.xionisDevice[index*2].register[Xionis.Index.onOff] == 0)
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
                onOffOperation[index*2] = 2;
                onOffOperation[index*2+1] = 2;
            }
            else
            {
                canRead = false;
                onOffOperation[index*2] = 1;
                onOffOperation[index*2+1] = 1;
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void setupAmplitudePlusButton (Button b, int index)
    {
        b.setOnClickListener(v -> {
            canRead = false;
            amplitudeOperation[index * 2] = 2;
            amplitudeOperation[index * 2 + 1] = 2;
        });
    }
    protected void setupAmplitudeMinusButton (Button b, int index)
    {
        b.setOnClickListener(v -> {
            canRead = false;
            amplitudeOperation[index*2] = 1;
            amplitudeOperation[index*2+1] = 1;
        });
    }
    protected void showProgramFunc (TextView text, int index)
    {
        String[] program = getResources().getStringArray(R.array.interferentialProgramResource);
        final String programName = program[programNum[index]];
        runOnUiThread(() -> text.setText(programName));
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void setupInputs ()
    {
        amplitudeInfo[0] = findViewById(R.id.interferentialAmplitude1);
        amplitudeInfo[1] = findViewById(R.id.interferentialAmplitude2);
        amplitudeInfo[2] = findViewById(R.id.interferentialAmplitude3);
        amplitudeInfo[3] = findViewById(R.id.interferentialAmplitude4);
        amplitudeInfo[4] = findViewById(R.id.interferentialAmplitude5);
        amplitudeInfo[5] = findViewById(R.id.interferentialAmplitude6);
        timeInfo[0] = findViewById(R.id.interferentialTimeLeft1);
        timeInfo[1] = findViewById(R.id.interferentialTimeLeft2);
        timeInfo[2] = findViewById(R.id.interferentialTimeLeft3);
        timeInfo[3] = findViewById(R.id.interferentialTimeLeft4);
        timeInfo[4] = findViewById(R.id.interferentialTimeLeft5);
        timeInfo[5] = findViewById(R.id.interferentialTimeLeft6);
        frequencyInfo[0] = findViewById(R.id.interferentialFrequency1);
        frequencyInfo[1] = findViewById(R.id.interferentialFrequency2);
        frequencyInfo[2] = findViewById(R.id.interferentialFrequency3);
        frequencyInfo[3] = findViewById(R.id.interferentialFrequency4);
        frequencyInfo[4] = findViewById(R.id.interferentialFrequency5);
        frequencyInfo[5] = findViewById(R.id.interferentialFrequency6);
        amplitudeButtonPlus[0] = findViewById(R.id.interferentialAmplitudePlus1);
        amplitudeButtonPlus[1] = findViewById(R.id.interferentialAmplitudePlus2);
        amplitudeButtonPlus[2] = findViewById(R.id.interferentialAmplitudePlus3);
        amplitudeButtonPlus[3] = findViewById(R.id.interferentialAmplitudePlus4);
        amplitudeButtonPlus[4] = findViewById(R.id.interferentialAmplitudePlus5);
        amplitudeButtonPlus[5] = findViewById(R.id.interferentialAmplitudePlus6);
        amplitudeButtonMinus[0] = findViewById(R.id.interferentialAmplitudeMinus1);
        amplitudeButtonMinus[1] = findViewById(R.id.interferentialAmplitudeMinus2);
        amplitudeButtonMinus[2] = findViewById(R.id.interferentialAmplitudeMinus3);
        amplitudeButtonMinus[3] = findViewById(R.id.interferentialAmplitudeMinus4);
        amplitudeButtonMinus[4] = findViewById(R.id.interferentialAmplitudeMinus5);
        amplitudeButtonMinus[5] = findViewById(R.id.interferentialAmplitudeMinus6);
        pauseButton[0] = findViewById(R.id.interferentialPause1);
        pauseButton[1] = findViewById(R.id.interferentialPause2);
        pauseButton[2] = findViewById(R.id.interferentialPause3);
        pauseButton[3] = findViewById(R.id.interferentialPause4);
        pauseButton[4] = findViewById(R.id.interferentialPause5);
        pauseButton[5] = findViewById(R.id.interferentialPause6);
        showProgramNum[0] = findViewById(R.id.interferentialShowProgram1);
        showProgramNum[1] = findViewById(R.id.interferentialShowProgram2);
        showProgramNum[2] = findViewById(R.id.interferentialShowProgram3);
        showProgramNum[3] = findViewById(R.id.interferentialShowProgram4);
        showProgramNum[4] = findViewById(R.id.interferentialShowProgram5);
        showProgramNum[5] = findViewById(R.id.interferentialShowProgram6);
        Button startButton =  findViewById(R.id.startProgramInterferential);
        for (int i = 0; i < Settings.amountOfDevices/2; i++)
        {
            if (isDeviceOnOff[i*2])
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
        setContentView(R.layout.activity_interferential_mode_control);
        setupInputs();
        MainThread.start();
        findViewById(R.id.interferentialControlBackArrow).setOnClickListener(v -> {
            if (xionis.checkIfDevicesAreOff())
            {
                isNotClosed = false;
                Intent intent = new Intent();
                intent.setClass(InterferentialModeControl.this, InterferentialMode.class);
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