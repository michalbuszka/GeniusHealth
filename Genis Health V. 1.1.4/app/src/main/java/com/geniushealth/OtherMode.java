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

public class OtherMode extends AppCompatActivity {
    protected Xionis xionis;
    protected BluetoothConnection bluetoothConnection;
    protected Xionis.DeviceParameters[] deviceParameters = new Xionis.DeviceParameters[6];
    protected Spinner[] timeDuration = new Spinner[6];
    protected Spinner[] programSpinner = new Spinner[6];
    protected boolean[] deviceOnOff = new boolean[6];
    protected Switch[] startSwitch = new Switch[6];
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
        findViewById(R.id.nextOtherButton).setEnabled(false);
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
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherBurstModulated1kHzTo20),  getResources().getIntArray(R.array.otherIONOptimalMaleFrom40));
                break;
            case 2:
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherRussianStimulationTo20),  null);
                break;
            case 1:
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherLongPulseTo20),  getResources().getIntArray(R.array.otherLongPulseFrom40));
                break;
            case 12:
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherIONOptimalMaleTo20),  getResources().getIntArray(R.array.otherIONOptimalMaleFrom40));
                break;
            case 13:
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherIONOptimalFemaleTo20),  getResources().getIntArray(R.array.otherIONOptimalFemaleFrom40));
                break;
            case 14:
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherIONReductionMaleTo20),  getResources().getIntArray(R.array.otherIONReductionMaleFrom40));
                break;
            case 15:
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherIONReductionFemaleTo20),  getResources().getIntArray(R.array.otherIONReductionFemaleFrom40));
                break;
            case 16:
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherIONPostLipoMaleTo20),  getResources().getIntArray(R.array.otherIONPostLipoMaleFrom40));
                break;
            case 17:
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherIONPostLipoFemaleTo20),  getResources().getIntArray(R.array.otherIONPostLipoFemaleFrom40));
                break;
            case 8:
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherWatanabeTo20),  null);
                break;
            case 9:
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherDelgadoAndre2018To20),  getResources().getIntArray(R.array.otherLuanaDeMello2018From40));
                break;
            case 11:
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherLuanaDeMello2018To20),  getResources().getIntArray(R.array.otherLuanaDeMello2018From40));
                break;
            case 10:
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherKemmler2013TestIIITo20), null);
                break;
            case 7:
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherFilipovic2019To20), null);
                break;
            case 5:
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherDOttavio2019_1To20),  null);
                break;
            case 6:
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherDOttavio2019_2To20),  null);
                break;
            case 4:
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherNeyroud2019To20),  null);
                break;
            case 3:
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherJames2018ModifiedTo20),  getResources().getIntArray(R.array.otherJames2018ModifiedFrom40));
                break;
            case 18:
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherMigraineAcuteTo20),  getResources().getIntArray(R.array.otherMigraineAcuteFrom40));
                break;
            case 19:
                sendProgramRegisters(index, getResources().getIntArray(R.array.otherCranialSedativeTo20),  getResources().getIntArray(R.array.otherCranialSedativeFrom40));
                break;
        }
    }
    protected synchronized void setupInputs ()
    {
        programSpinner[0] = findViewById(R.id.otherProgram1);
        programSpinner[1] = findViewById(R.id.otherProgram2);
        programSpinner[2] = findViewById(R.id.otherProgram3);
        programSpinner[3] = findViewById(R.id.otherProgram4);
        programSpinner[4] = findViewById(R.id.otherProgram5);
        programSpinner[5] = findViewById(R.id.otherProgram6);
        startSwitch[0] = findViewById(R.id.otherOnOff1);
        startSwitch[1] = findViewById(R.id.otherOnOff2);
        startSwitch[2] = findViewById(R.id.otherOnOff3);
        startSwitch[3] = findViewById(R.id.otherOnOff4);
        startSwitch[4] = findViewById(R.id.otherOnOff5);
        startSwitch[5] = findViewById(R.id.otherOnOff6);
        timeDuration[0] = findViewById(R.id.otherTimeDuration1);
        timeDuration[1] = findViewById(R.id.otherTimeDuration2);
        timeDuration[2] = findViewById(R.id.otherTimeDuration3);
        timeDuration[3] = findViewById(R.id.otherTimeDuration4);
        timeDuration[4] = findViewById(R.id.otherTimeDuration5);
        timeDuration[5] = findViewById(R.id.otherTimeDuration6);
        for (int i = 0; i < 6; i++)
        {
            deviceOnOff[i] = true;
            deviceParameters[i] = new Xionis.DeviceParameters();
            setupStartInputs(startSwitch[i] , i);
            setupProgramSpinners(programSpinner[i], i);
            setupTimeSpinners(timeDuration[i], i);
        }
        findViewById(R.id.nextOtherButton).setOnClickListener(v -> {
            if (amountOfOnDevices() > 0)
            {
                disableInputs();
                ProgressDialog progress = new ProgressDialog(OtherMode.this);
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
                        burstControl.setClass(OtherMode.this, OtherControl.class);
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
        setContentView(R.layout.activity_other_mode);
        bluetoothConnection = (BluetoothConnection)getApplicationContext();
        xionis.createDevices(bluetoothConnection);
        setupInputs();
        findViewById(R.id.otherBackArrow).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(OtherMode.this, ModeSelection.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        });
    }
}