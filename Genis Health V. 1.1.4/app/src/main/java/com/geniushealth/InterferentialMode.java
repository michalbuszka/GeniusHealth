package com.geniushealth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

public class InterferentialMode extends AppCompatActivity {
    protected Xionis xionis;
    protected BluetoothConnection bluetoothConnection;
    protected Xionis.DeviceParameters[] deviceParameters = new Xionis.DeviceParameters[6];
    protected Spinner[] timeDuration = new Spinner[6];
    protected Spinner[] programSpinner = new Spinner[6];
    protected boolean[] deviceOnOff = new boolean[6];
    protected Switch[] startSwitch = new Switch[6];
    protected void disableChannel (int index)
    {
        runOnUiThread(() -> {
            startSwitch[index].setSelected(false);
            startSwitch[index].setEnabled(false);
            timeDuration[index].setEnabled(false);
            programSpinner[index].setEnabled(false);
        });
    }
    protected void sendProgramRegisters (int index, int[] programTo20, int[] programFrom40)
    {
        byte[] RegistersTo20 = Xionis.prepareWriteManyRegistersFrame(0x50 + index, 0, programTo20);
        if (programFrom40 == null)
        {
            xionis.xionisDevice[index].setRegisters(RegistersTo20);
        }
        else if (programFrom40.length > 100)
        {
            int[] Pack1Int = new int[100];
            for (int i = 0; i < 100; i++)
            {
                Pack1Int[i] = programFrom40[i];
            }
            int[] burstPack2Int = new int[programFrom40.length - 100];
            for (int i = 100; i < programFrom40.length; i++)
            {
                burstPack2Int[i - 100] = programFrom40[i];
            }
            byte[] otherBurstModulated1kHzPack1 = Xionis.prepareWriteManyRegistersFrame(0x50 + index, 40, Pack1Int);
            byte[] otherBurstModulated1kHzPack2 = Xionis.prepareWriteManyRegistersFrame(0x50 + index, 140, burstPack2Int);
            xionis.xionisDevice[index].setRegisters(RegistersTo20);
            xionis.xionisDevice[index].setRegisters(otherBurstModulated1kHzPack1);
            xionis.xionisDevice[index].setRegisters(otherBurstModulated1kHzPack2);
        }
        else if (programFrom40.length < 100)
        {
            byte[] otherBurstModulated1kHzPack1 = Xionis.prepareWriteManyRegistersFrame(0x50 + index, 40, programFrom40);
            xionis.xionisDevice[index].setRegisters(RegistersTo20);
            xionis.xionisDevice[index].setRegisters(otherBurstModulated1kHzPack1);
        }
    }
    protected int[] rewriteProgramArray ()
    {
        int[] programNumber = new int[6];
        for (int i = 0; i < 6; i++)
        {
            programNumber[i] = deviceParameters[i].programNumber;
        }
        return programNumber;
    }
    protected void disableInputs ()
    {
        findViewById(R.id.nextInterferentialButton).setEnabled(false);
        for (int i = 0; i < 6; i++)
        {
            timeDuration[i].setEnabled(false);
            timeDuration[i].setEnabled(false);
            startSwitch[i].setEnabled(false);
            programSpinner[i].setEnabled(false);
        }
    }
    protected void setupProgramSpinners (Spinner program, int num)
    {
        program.setSelection(0, false);
        program.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                deviceParameters[num].programNumber = program.getSelectedItemPosition();
                if ( deviceParameters[num].programNumber!=0)
                {
                    deviceOnOff[(num + 1) * 2 - 2] = true;
                    deviceOnOff[(num + 1) * 2 - 1] = false;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    protected int amountOfOnDevices()
    {
        int counter = 0;
        for (int i = 0; i < 6; i++)
        {
            if (deviceOnOff[i])
                counter++;
        }
        return counter;
    }
    protected void setupTimeSpinners (Spinner time, int num)
    {
        time.setSelection(0, false);
        time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (time.getSelectedItemPosition() == 0)
                {
                    deviceParameters[num].timeDuration = 3000;
                }
                else
                {
                    deviceParameters[num].timeDuration = time.getSelectedItemPosition() * 300;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    protected void setupStartInputs (Switch onOff, int num)
    {
        onOff.setChecked(true);
        onOff.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!onOff.isChecked())
            {
                deviceOnOff[(num + 1) * 2 - 2] = false;
                deviceOnOff[(num + 1) * 2 - 1] = false;
            }
            else
            {
                deviceOnOff[(num + 1) * 2 - 2] = true;
                deviceOnOff[(num + 1) * 2 - 1] = true;
            }
        });
    }
    protected synchronized void setProgram (int index, int programNum)
    {
        switch (programNum)
        {
            case 0:
                sendProgramRegisters(index - 2, getResources().getIntArray(R.array.interferentialDifferentialLeftTo20),  getResources().getIntArray(R.array.interferentialDifferentialLeftFrom40));
                sendProgramRegisters(index- 1, getResources().getIntArray(R.array.interferentialDifferentialRightTo20),  getResources().getIntArray(R.array.interferentialDifferentialRightFrom40));
                break;
            case 1:
                sendProgramRegisters(index- 2, getResources().getIntArray(R.array.burstPelvicTo20),  getResources().getIntArray(R.array.burstPelvicFrom40));
                break;
            case 2:
                sendProgramRegisters(index- 2, getResources().getIntArray(R.array.burstTibalTo20),  getResources().getIntArray(R.array.burstTibalFrom40));
                break;
            case 3:
                sendProgramRegisters(index- 2, getResources().getIntArray(R.array.burstCranialTo20),  getResources().getIntArray(R.array.burstCranialFrom40));
                break;
        }
    }
    protected synchronized void setupInputs ()
    {
        programSpinner[0] = findViewById(R.id.interferentialProgram1);
        programSpinner[1] = findViewById(R.id.interferentialProgram3);
        programSpinner[2] = findViewById(R.id.interferentialProgram5);
        programSpinner[3] = findViewById(R.id.interferentialProgram7);
        programSpinner[4] = findViewById(R.id.interferentialProgram9);
        programSpinner[5] = findViewById(R.id.interferentialProgram11);
        startSwitch[0] = findViewById(R.id.interferentialOnOff1);
        startSwitch[1] = findViewById(R.id.interferentialOnOff3);
        startSwitch[2] = findViewById(R.id.interferentialOnOff5);
        startSwitch[3] = findViewById(R.id.interferentialOnOff7);
        startSwitch[4] = findViewById(R.id.interferentialOnOff9);
        startSwitch[5] = findViewById(R.id.interferentialOnOff11);
        timeDuration[0] = findViewById(R.id.interferentialTimeDuration1);
        timeDuration[1] = findViewById(R.id.interferentialTimeDuration3);
        timeDuration[2] = findViewById(R.id.interferentialTimeDuration5);
        timeDuration[3] = findViewById(R.id.interferentialTimeDuration7);
        timeDuration[4] = findViewById(R.id.interferentialTimeDuration9);
        timeDuration[5] = findViewById(R.id.interferentialTimeDuration11);
        for (int i = 0; i < 6; i++)
        {
            deviceParameters[i] = new Xionis.DeviceParameters();
            deviceOnOff[i] = true;
            if (i + 1 > Settings.amountOfDevices / 2)
            {
                disableChannel(i);
            }
            else
            {
                setupStartInputs(startSwitch[i] , i);
                setupProgramSpinners(programSpinner[i], i);
                setupTimeSpinners(timeDuration[i], i);
            }
        }
        findViewById(R.id.nextInterferentialButton).setOnClickListener(v -> {
            if (amountOfOnDevices() > 0)
            {
                disableInputs();
                ProgressDialog progress = new ProgressDialog(InterferentialMode.this);
                progress.setTitle(Messages.EN.loadMessage);
                progress.setCancelable(false);
                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progress.setMax(100);
                progress.show();
                int maxDevices = amountOfOnDevices();
                Thread sender = new Thread ()
                {
                    public synchronized  void run ()
                    {
                        for (int i = 0; i < Settings.amountOfDevices / 2; i++)
                        {
                            int channelNum = i * 2;
                            if (deviceOnOff[channelNum])
                            {
                                setProgram((i + 1) * 2, deviceParameters[channelNum].programNumber);
                                xionis.xionisDevice[channelNum].setRegister(Xionis.Address.programTime, deviceParameters[i].timeDuration);
                                xionis.xionisDevice[channelNum].setRegister(Xionis.Address.timer, 0);
                                if (deviceParameters[channelNum].programNumber == 0)
                                {
                                    xionis.xionisDevice[channelNum+1].setRegister(Xionis.Address.programTime, deviceParameters[i].timeDuration);
                                    xionis.xionisDevice[channelNum+1].setRegister(Xionis.Address.timer, 0);
                                    progress.incrementProgressBy(100/maxDevices);
                                }
                            }
                            else
                            {
                                xionis.xionisDevice[channelNum].setRegister(Xionis.Address.programTime, 0);
                                xionis.xionisDevice[channelNum+1].setRegister(Xionis.Address.programTime, 0);
                            }
                            progress.incrementProgressBy(100/maxDevices);
                        }
                        progress.dismiss();
                        Intent burstControl = new Intent();
                        burstControl.setClass(InterferentialMode.this, InterferentialModeControl.class);
                        burstControl.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        burstControl.putExtra("deviceOnOff", deviceOnOff);
                        burstControl.putExtra("programNumber", rewriteProgramArray());
                        startActivity(burstControl);
                        finish();
                    }
                };
                sender.start();
            }
            else
            {
                Toast.makeText(getApplicationContext(), Messages.EN.noChannelsSelected, Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        xionis = new Xionis(this);
        setContentView(R.layout.activity_interferential_mode);
        bluetoothConnection = (BluetoothConnection)getApplicationContext();
        xionis.createDevices(bluetoothConnection);
        setupInputs();
        findViewById(R.id.interferentialBackArrow).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(InterferentialMode.this, ModeSelection.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        });
    }
}