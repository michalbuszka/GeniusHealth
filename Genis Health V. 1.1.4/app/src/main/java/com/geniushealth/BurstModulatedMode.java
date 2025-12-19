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

public class BurstModulatedMode extends AppCompatActivity {
    protected Xionis xionis;
    protected BluetoothConnection bluetoothConnection;
    protected Xionis.DeviceParameters[] deviceParameters = new Xionis.DeviceParameters[6];
    protected Spinner[] timeDuration = new Spinner[6];
    protected Spinner[] programSpinner = new Spinner[6];
    protected boolean[] deviceOnOff = new boolean[6];
    protected Switch[] startSwitch = new Switch[6];
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
        findViewById(R.id.nextBurstButton).setEnabled(false);
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
            if (onOff.isChecked())
            {
                deviceOnOff[num] = true;
            }
            else {
                deviceOnOff[num] = false;
            }
        });
    }
    protected synchronized void setProgram (int index, int programNum)
    {
        switch (programNum)
        {
            case 0:
                sendProgramRegisters(index, getResources().getIntArray(R.array.burstBurstModulatedTo20),  getResources().getIntArray(R.array.burstBurstModulatedFrom40));
                break;
            case 1:
                sendProgramRegisters(index, getResources().getIntArray(R.array.burstPelvicTo20),  getResources().getIntArray(R.array.burstPelvicFrom40));
                break;
            case 2:
                sendProgramRegisters(index, getResources().getIntArray(R.array.burstTibalTo20),  getResources().getIntArray(R.array.burstTibalFrom40));
                break;
            case 3:
                sendProgramRegisters(index, getResources().getIntArray(R.array.burstCranialTo20),  getResources().getIntArray(R.array.burstCranialFrom40));
                break;
        }
    }
    protected synchronized void setupInputs ()
    {
        programSpinner[0] = findViewById(R.id.burstProgram1);
        programSpinner[1] = findViewById(R.id.burstProgram2);
        programSpinner[2] = findViewById(R.id.burstProgram3);
        programSpinner[3] = findViewById(R.id.burstProgram4);
        programSpinner[4] = findViewById(R.id.burstProgram5);
        programSpinner[5] = findViewById(R.id.burstProgram6);
        startSwitch[0] = findViewById(R.id.burstOnOff1);
        startSwitch[1] = findViewById(R.id.burstOnOff2);
        startSwitch[2] = findViewById(R.id.burstOnOff3);
        startSwitch[3] = findViewById(R.id.burstOnOff4);
        startSwitch[4] = findViewById(R.id.burstOnOff5);
        startSwitch[5] = findViewById(R.id.burstOnOff6);
        timeDuration[0] = findViewById(R.id.burstTimeDuration1);
        timeDuration[1] = findViewById(R.id.burstTimeDuration2);
        timeDuration[2] = findViewById(R.id.burstTimeDuration3);
        timeDuration[3] = findViewById(R.id.burstTimeDuration4);
        timeDuration[4] = findViewById(R.id.burstTimeDuration5);
        timeDuration[5] = findViewById(R.id.burstTimeDuration6);
        for (int i = 0; i < 6; i++)
        {
            deviceOnOff[i] = true;
            deviceParameters[i] = new Xionis.DeviceParameters();
            setupStartInputs(startSwitch[i] , i);
            setupProgramSpinners(programSpinner[i], i);
            setupTimeSpinners(timeDuration[i], i);
        }
        findViewById(R.id.nextBurstButton).setOnClickListener(v -> {
            if (amountOfOnDevices() > 0)
            {
                disableInputs();
                ProgressDialog progress = new ProgressDialog(BurstModulatedMode.this);
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
                        for (int i = 0; i < 6; i++)
                        {
                            if (deviceOnOff[i])
                            {
                                setProgram(i, deviceParameters[i].programNumber);
                                xionis.xionisDevice[i].setRegister(Xionis.Address.programTime, deviceParameters[i].timeDuration);
                                xionis.xionisDevice[i].setRegister(Xionis.Address.timer, 0);
                            }
                            else
                            {
                                xionis.xionisDevice[i].setRegister(Xionis.Address.programTime, 0);
                            }
                            progress.incrementProgressBy(100/maxDevices);
                        }
                        progress.dismiss();
                        Intent burstControl = new Intent();
                        burstControl.setClass(BurstModulatedMode.this, BurstModulatedControl.class);
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
        xionis = new Xionis(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_burst_mode);
        bluetoothConnection = (BluetoothConnection)getApplicationContext();
        xionis.createDevices(bluetoothConnection);
        setupInputs();
        findViewById(R.id.burstBackArrow).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(BurstModulatedMode.this, ModeSelection.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        });
    }
}