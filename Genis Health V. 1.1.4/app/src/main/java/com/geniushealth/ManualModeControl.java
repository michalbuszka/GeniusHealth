package com.geniushealth;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ManualModeControl extends AppCompatActivity {
    protected Xionis xionis;
    protected long time1, time2;
    protected TextView[] frequencyInfo = new TextView[6];
    protected TextView[] amplitudeInfo = new TextView[6];
    protected TextView[] timeInfo = new TextView[6];
    protected TextView[] showProgramNum = new TextView[6];
    protected Button[] amplitudeButtonPlus = new Button[6];
    protected Button[] amplitudeButtonMinus = new Button[6];
    protected Button[] pauseButton = new Button[6];
    protected Button[] editChannel = new Button[6];
    protected boolean[] doLoadProgram = new boolean[6];
    protected int[] amplitudeOperation = new int[6];
    protected int[] programNum = new int[6];
    protected int[] timeDurationValueToSet = new int[6];
    protected int[] delayPosition = new int[6];
    protected int[] dutyPosition = new int[6];
    protected int[] pulseOnPosition = new int[6];
    protected int[] rampRisePosition = new int[6];
    protected int[] rampFallPosition = new int[6];
    protected int[] cycleTimePosition = new int[6];
    protected int[] pulsePosition = new int[6];
    protected boolean[] isDeviceOnOff = new boolean[6];
    protected int[] onOffOperation = new int[6];
    protected boolean[] runStatus = new boolean[6];
    protected boolean[] doGetData = new boolean[6];
    protected boolean[] doChangePulse = new boolean[6];
    protected boolean[] doChangePulseOn = new boolean[6];
    protected boolean[] doChangeRampRise = new boolean[6];
    protected boolean[] doChangeRampFall = new boolean[6];
    protected boolean[] doChangeDelay = new boolean[6];
    protected boolean[] doChangeCycleTime = new boolean[6];
    protected boolean[] doChangeDuty = new boolean[6];
    protected boolean[] carrierFrequencyOperation = new boolean[6];
    protected boolean[] minBaseFrequencyOperation = new boolean[6];
    protected boolean[] maxBaseFrequencyOperation = new boolean[6];
    protected boolean startStatus = false;
    protected boolean canRead = true;
    protected boolean runAllDevices = false;
    protected boolean stopAllDevices = false;
    protected boolean isNotClosed = true;
    protected BluetoothConnection bluetoothConnection;
    protected void showProgramFunc (TextView text, int index)
    {
        String[] program = getResources().getStringArray(R.array.manualProgramResource);
        final String programName = program[programNum[index]];
        runOnUiThread(() -> text.setText(programName));
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
    protected void setProgram (int index, int programNum)
    {
        switch (programNum)
        {
            case 0:
                sendProgramRegisters(index, getResources().getIntArray(R.array.manualLongPulseTo20), null);
                break;
            case 1:
                sendProgramRegisters(index, getResources().getIntArray(R.array.manualShortPulseTo20), null);
                break;
            case 2:
                sendProgramRegisters(index, getResources().getIntArray(R.array.manualRussianTo20), null);
                break;
            case 3:
                sendProgramRegisters(index, getResources().getIntArray(R.array.manualPelvicTo20),  getResources().getIntArray(R.array.manualPelvicForm40));
                break;
            case 4:
                sendProgramRegisters(index, getResources().getIntArray(R.array.manualTibalTo20),  getResources().getIntArray(R.array.manualTibalFrom40));
                break;
            case 5:
                sendProgramRegisters(index, getResources().getIntArray(R.array.manualCranalTo20),  getResources().getIntArray(R.array.manualCranalFrom40));
                break;
        }
    }
    protected int getBaseFrequencyAmount (int currentAmount)
    {
        if (currentAmount < 10)
        {
            return 1;
        }
        else if (currentAmount < 100)
        {
            return 5;
        }
        else if (currentAmount < 1000)
        {
            return 25;
        }
        else if (currentAmount <= 5000)
        {
            return 125;
        }
        return 0;
    }
    protected void setupProgramPosition (Spinner spinner, int programNum)
    {
        ArrayAdapter<CharSequence> programAdapter = ArrayAdapter.createFromResource(this, R.array.manualProgramResource, R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(programAdapter);
        spinner.setSelection(programNum, false);
    }
    protected void showCarrierFrequency (TextView textView, int index)
    {
        final String carrierFrequency = xionis.xionisDevice[index].register[Xionis.Index.carrierFrequency] +" Hz";
        runOnUiThread(() -> textView.setText(carrierFrequency));
    }
    protected void showMinBaseFrequency(TextView textView, int index)
    {
        final String minBaseFrequency = xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMin] +" Hz";
        runOnUiThread(() -> textView.setText(minBaseFrequency));
    }
    protected void showMaxBaseFrequency(TextView textView, int index)
    {
        final String maxBaseFrequency = xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMax] +" Hz";
        runOnUiThread(() -> textView.setText(maxBaseFrequency));
    }
    protected void editChannelSettings (int index)
    {
        Thread editChannelThread = new Thread ()
        {
            public void run ()
            {
                canRead = false;
                doGetData[index] = true;
                runOnUiThread(() -> editChannel[index].setEnabled(false));
                while(doGetData[index])
                {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(() -> editChannel[index].setEnabled(true));
                AlertDialog.Builder editChannel = new AlertDialog.Builder(ManualModeControl.this);
                View view = getLayoutInflater().inflate(R.layout.edit_channel_dialog, null);
                Spinner program = view.findViewById(R.id.editChannelProgram);
                Spinner timeDuration = view.findViewById(R.id.editChannelTimeDuration);
                Spinner cycleTime = view.findViewById(R.id.editChannelCycleTime);
                Spinner delay = view.findViewById(R.id.editChannelDelay);
                Spinner duty = view.findViewById(R.id.editChannelDuty);
                Spinner pulse = view.findViewById(R.id.editChannelPulse);
                Spinner rampRise = view.findViewById(R.id.editChannelRampRise);
                Spinner rampFall = view.findViewById(R.id.editChannelRampFall);
                Spinner pulseOn = view.findViewById(R.id.editChannelPulseOn);
                ArrayList<String> pulseOnResource = new ArrayList<String>();
                for (int i = 3; i <= 120; i++)
                {
                    pulseOnResource.add(i * 0.5 + " s");
                }
                ArrayAdapter<String> pulseOnAdapter = new ArrayAdapter<String>(ManualModeControl.this, R.layout.support_simple_spinner_dropdown_item, pulseOnResource);
                pulseOn.setAdapter(pulseOnAdapter);
                TextView showCarrierFrequencyTextView = view.findViewById(R.id.editChannelShowCarrierFrequency);
                TextView showMinBaseFrequencyTextView = view.findViewById(R.id.editChannelShowMinBaseFrequency);
                TextView showMaxBaseFrequencyTextView = view.findViewById(R.id.editChannelShowMaxBaseFrequency);
                TextView showAmplitude = view.findViewById(R.id.editChannelShowAmplitude);
                Button editChannelAmplitudeMinus = view.findViewById(R.id.editChannelAmplitudeMinus);
                Button editChannelAmplitudePlus= view.findViewById(R.id.editChannelAmplitudePlus);
                editChannel.setPositiveButton("Back", null);
                Button carrierFrequencyMinus= view.findViewById(R.id.editChannelCarrierFrequencyMinus);
                Button carrierFrequencyPlus = view.findViewById(R.id.editChannelCarrierFrequencyPlus);
                Button minBaseFrequencyMinus= view.findViewById(R.id.editChannelMinBaseFrequencyMinus);
                Button minBaseFrequencyPlus = view.findViewById(R.id.editChannelMinBaseFrequencyPlus);
                Button maxBaseFrequencyMinus= view.findViewById(R.id.editChannelMaxBaseFrequencyMinus);
                Button maxBaseFrequencyPlus= view.findViewById(R.id.editChannelMaxBaseFrequencyPlus);
                setupProgramPosition(program, programNum[index]);
                xionis.showAmplitude(showAmplitude, index);
                showMaxBaseFrequency(showMaxBaseFrequencyTextView, index);
                showMinBaseFrequency(showMinBaseFrequencyTextView, index);
                showCarrierFrequency(showCarrierFrequencyTextView, index);
                maxBaseFrequencyMinus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMax] - getBaseFrequencyAmount(xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMax]) >= xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMin])
                        {
                            if (xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMax] - getBaseFrequencyAmount(xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMax]) >= Settings.minOfMaxBaseFrequency)
                            {
                                canRead = false;
                                xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMax] -= getBaseFrequencyAmount(xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMax]);
                                showMaxBaseFrequency(showMaxBaseFrequencyTextView, index);
                                maxBaseFrequencyOperation[index] = true;
                            }
                        }
                        else
                        {
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), Messages.EN.maximumBaseFreqCannotBeLowerThanMin, Toast.LENGTH_SHORT).show());
                        }
                    }
                });
                maxBaseFrequencyPlus.setOnClickListener(v -> {
                    if (xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMax] + (getBaseFrequencyAmount(xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMax])) <= Settings.maxOfMaxBaseFrequency)
                    {
                        canRead = false;
                        xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMax] += (getBaseFrequencyAmount(xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMax]));
                        showMaxBaseFrequency(showMaxBaseFrequencyTextView, index);
                        maxBaseFrequencyOperation[index] = true;
                    }
                });
                minBaseFrequencyMinus.setOnClickListener(v -> {
                    if (xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMin] - getBaseFrequencyAmount(xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMin]) >= Settings.minOfMinBaseFrequency)
                    {
                        canRead = false;
                        xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMin] -= getBaseFrequencyAmount(xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMin]);
                        showMinBaseFrequency(showMinBaseFrequencyTextView, index);
                        minBaseFrequencyOperation[index] = true;
                    }
                });
                minBaseFrequencyPlus.setOnClickListener(v -> {
                    if (xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMin] + (getBaseFrequencyAmount(xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMin])) <= xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMax])
                    {
                        if (xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMin] + (getBaseFrequencyAmount(xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMin])) <= Settings.maxOfMinBaseFrequency)
                        {
                            canRead = false;
                            xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMin] += (getBaseFrequencyAmount(xionis.xionisDevice[index].register[Xionis.Index.baseFrequencyMin]));
                            showMinBaseFrequency(showMinBaseFrequencyTextView, index);
                            minBaseFrequencyOperation[index] = true;
                        }
                    }
                    else
                    {
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), Messages.EN.minimalBaseFreqCannotBeGraterThanMax, Toast.LENGTH_SHORT).show());
                    }
                });
                carrierFrequencyMinus.setOnClickListener(v -> {
                    if (xionis.xionisDevice[index].register[Xionis.Index.carrierFrequency] - getBaseFrequencyAmount(xionis.xionisDevice[index].register[Xionis.Index.carrierFrequency]) >= Settings.minCarrierFrequency)
                    {
                        canRead = false;
                        xionis.xionisDevice[index].register[Xionis.Index.carrierFrequency] -= getBaseFrequencyAmount(xionis.xionisDevice[index].register[Xionis.Index.carrierFrequency]);
                        carrierFrequencyOperation[index] = true;
                        showCarrierFrequency(showCarrierFrequencyTextView, index);
                    }
                });
                carrierFrequencyPlus.setOnClickListener(v -> {
                    if (xionis.xionisDevice[index].register[Xionis.Index.carrierFrequency] + getBaseFrequencyAmount(xionis.xionisDevice[index].register[Xionis.Index.carrierFrequency]) <= Settings.maxCarrierFrequency)
                    {
                        canRead = false;
                        xionis.xionisDevice[index].register[Xionis.Index.carrierFrequency] += getBaseFrequencyAmount(xionis.xionisDevice[index].register[Xionis.Index.carrierFrequency]);
                        carrierFrequencyOperation[index] = true;
                        showCarrierFrequency(showCarrierFrequencyTextView, index);
                    }
                });
                editChannelAmplitudeMinus.setOnClickListener(v -> {
                    canRead = false;
                    amplitudeOperation[index] = 1;
                    xionis.showAmplitude(view.findViewById(R.id.editChannelShowAmplitude), index);
                    Thread waitThread = new Thread () {
                        public void run ()
                        {
                            do {

                            }while(amplitudeOperation[index] != 0);
                            xionis.showAmplitude(view.findViewById(R.id.editChannelShowAmplitude), index);
                        }
                    };
                    waitThread.start();
                });
                editChannelAmplitudePlus.setOnClickListener(v -> {
                    canRead = false;
                    amplitudeOperation[index] = 2;
                    xionis.showAmplitude(view.findViewById(R.id.editChannelShowAmplitude), index);
                    Thread waitThread = new Thread () {
                        public void run ()
                        {
                            do {

                            }while(amplitudeOperation[index] != 0);
                            xionis.showAmplitude(view.findViewById(R.id.editChannelShowAmplitude), index);
                        }
                    };
                    waitThread.start();
                });
                rampFall.setSelection(rampFallPosition[index], false);
                rampFall.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        rampFallPosition[index] = position;
                        canRead = false;
                        doChangeRampFall[index] = true;
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                cycleTime.setSelection(cycleTimePosition[index], false);
                cycleTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        cycleTimePosition[index] = position;
                        canRead = false;
                        doChangeCycleTime[index] = true;
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                rampRise.setSelection(rampRisePosition[index], false);
                rampRise.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        rampRisePosition[index] = position;
                        canRead = false;
                        doChangeRampRise[index] = true;
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                delay.setSelection(delayPosition[index], false);
                delay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        delayPosition[index] = position;
                        canRead = false;
                        doChangeDelay[index] = true;
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                pulse.setSelection(pulsePosition[index], false);
                pulse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        pulsePosition[index] = position;
                        canRead = false;
                        doChangePulse[index] = true;
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                timeDuration.setSelection(1, false);
                timeDuration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        canRead = false;
                        if (position == 0)
                        {
                            timeDurationValueToSet[index] = 3000;
                        }
                        else {
                            timeDurationValueToSet[index] = 300 * position;
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                duty.setSelection(dutyPosition[index], false);
                duty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        canRead = false;
                        dutyPosition[index] = position;
                        doChangeDuty[index] = true;

                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                pulseOn.setSelection(pulseOnPosition[index], false);
                pulseOn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        canRead = false;
                        pulseOnPosition[index] = position;
                        doChangePulseOn[index] = true;

                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                program.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position != programNum[index])
                        {
                            timeDurationValueToSet[index] = xionis.xionisDevice[index].register[Xionis.Index.timer];
                            programNum[index] = position;
                            canRead = false;
                            doLoadProgram[index] = true;
                            Thread waitThread = new Thread ()
                            {
                                public void run ()
                                {
                                    do {

                                    }while(doLoadProgram[index]);
                                    xionis.showAmplitude(showAmplitude, index);
                                    showMaxBaseFrequency(showMaxBaseFrequencyTextView, index);
                                    showMinBaseFrequency(showMinBaseFrequencyTextView, index);
                                    showCarrierFrequency(showCarrierFrequencyTextView, index);
                                    runOnUiThread(() -> {
                                        rampFall.setSelection(rampFallPosition[index], false);
                                        cycleTime.setSelection(cycleTimePosition[index], false);
                                        rampRise.setSelection(rampRisePosition[index], false);
                                        delay.setSelection(delayPosition[index], false);
                                        pulse.setSelection(pulsePosition[index], false);
                                        duty.setSelection(dutyPosition[index], false);
                                        pulseOn.setSelection(pulseOnPosition[index], false);
                                    });
                                }
                            };
                            waitThread.start();
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                runOnUiThread(() -> editChannel.setView(view).show());
            }
        };
        editChannelThread.start();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void disableInputs ()
    {
        Drawable disabledOff = getResources().getDrawable(R.drawable.stopb_a);
        Drawable disabledMinus = getResources().getDrawable(R.drawable.minus_a);
        Drawable disabledPlus = getResources().getDrawable(R.drawable.plus_a);
        Drawable disabledSettings = getResources().getDrawable(R.drawable.settings_white);
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
                editChannel[i].setEnabled(false);
                editChannel[i].setForeground(disabledSettings);
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
        ManualModeControl.this.runOnUiThread(() -> text.setText(finalText));
    }
    protected void updatePositions (int i)
    {
        if (!doChangeDelay[i])
        {
            delayPosition[i] = xionis.xionisDevice[i].register[Xionis.Index.delayOn] / 200;
        }
        if (!doChangeDuty[i])
        {
            dutyPosition[i] = xionis.xionisDevice[i].register[Xionis.Index.dutyOfBaseFrequency] / (1024 / 100 * 5);
        }
        if (!doChangePulse[i])
        {
            pulsePosition[i] = xionis.xionisDevice[i].register[Xionis.Index.pulseOfBaseFrequency];
        }
        if (!doChangePulseOn[i])
        {
            pulseOnPosition[i] = (xionis.xionisDevice[i].register[Xionis.Index.timeOfPulseON] / 512) - 3;
        }
        if (!doChangeRampRise[i])
        {
            rampRisePosition[i] = (xionis.xionisDevice[i].register[Xionis.Index.rampRiseTime] / 100) - 1;
        }
        if (!doChangeRampFall[i])
        {
            rampFallPosition[i] = (xionis.xionisDevice[i].register[Xionis.Index.rampFallTime] / 100);
        }
        if (!doChangeCycleTime[i])
        {
            cycleTimePosition[i] = (xionis.xionisDevice[i].register[Xionis.Index.tonPlusToff] / 1024) - 8;
        }
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
                        updatePositions(i);
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
        if (doLoadProgram[i])
        {
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Channel "+(i+1)+" program loading under progress...", Toast.LENGTH_SHORT).show());
            setProgram(i, programNum[i]);
            xionis.xionisDevice[i].getData();
            showProgramFunc(showProgramNum[i], i);
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Channel "+(i+1)+" program loading complete!", Toast.LENGTH_SHORT).show());
            if (timeDurationValueToSet[i] != 0)
            {
                xionis.xionisDevice[i].setRegister(Xionis.Address.programTime, timeDurationValueToSet[i]);
                timeDurationValueToSet[i] = 0;
            }
            doLoadProgram[i] = false;
        }
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
        if (timeDurationValueToSet[i] != 0)
        {
            xionis.xionisDevice[i].setRegister(Xionis.Address.onOff, 0);
            xionis.xionisDevice[i].setRegister(Xionis.Address.timer, 0);
            xionis.xionisDevice[i].setRegister(Xionis.Address.programTime, timeDurationValueToSet[i]);
            timeDurationValueToSet[i] = 0;
        }
        if (carrierFrequencyOperation[i])
        {
            xionis.xionisDevice[i].setRegister(Xionis.Address.carrierFrequency, xionis.xionisDevice[i].register[Xionis.Index.carrierFrequency]);
            carrierFrequencyOperation[i] = false;
        }
        if (minBaseFrequencyOperation[i])
        {
            xionis.xionisDevice[i].setRegister(Xionis.Address.baseFrequencyMin, xionis.xionisDevice[i].register[Xionis.Index.baseFrequencyMin]);
            minBaseFrequencyOperation[i] = false;
        }
        if (maxBaseFrequencyOperation[i])
        {
            xionis.xionisDevice[i].setRegister(Xionis.Address.baseFrequencyMax, xionis.xionisDevice[i].register[Xionis.Index.baseFrequencyMax]);
            maxBaseFrequencyOperation[i] = false;
        }
        if (doGetData[i])
        {
            xionis.xionisDevice[i].getData();
            doGetData[i] = false;
            canRead = true;
        }
        if (doChangeDelay[i])
        {
            int delayValue = delayPosition[i] * 200;
            xionis.xionisDevice[i].setRegister(Xionis.Address.delayOn, delayValue);
            doChangeDelay[i] = false;
        }
        if (doChangeDuty[i])
        {
            int dutyValue = dutyPosition[i] * 1024 / 100 * 5;
            xionis.xionisDevice[i].setRegister(Xionis.Address.dutyOfBaseFrequency, dutyValue);
            doChangeDuty[i] = false;
        }
        if (doChangePulse[i])
        {
            int dutyValue = pulsePosition[i];
            xionis.xionisDevice[i].setRegister(Xionis.Address.pulseOfBaseFrequency, dutyValue);
            doChangePulse[i] = false;
        }
        if (doChangePulseOn[i])
        {
            int pulseOnValue = (pulseOnPosition[i] + 3) * 512;
            xionis.xionisDevice[i].setRegister(Xionis.Address.timeOfPulseON, pulseOnValue);
            doChangePulseOn[i] = false;
        }
        if (doChangeRampRise[i])
        {
            int rampRiseValue = (rampRisePosition[i] + 1) * 100;
            xionis.xionisDevice[i].setRegister(Xionis.Address.rampRiseTime, rampRiseValue);
            doChangeRampRise[i] = false;
        }
        if (doChangeRampFall[i])
        {
            int rampFallValue = rampFallPosition[i] * 100;
            xionis.xionisDevice[i].setRegister(Xionis.Address.rampFallTime, rampFallValue);
            doChangeRampFall[i] = false;
        }
        if (doChangeCycleTime[i])
        {
            int cycleTimeValue = (cycleTimePosition[i] + 8) * 1024;
            xionis.xionisDevice[i].setRegister(Xionis.Address.tonPlusToff, cycleTimeValue);
            doChangeCycleTime[i] = false;
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
            else if (amplitudeOperation[i]==2)
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
            for (int i = 0; i < 6; i++)
            {
                if (isDeviceOnOff[i])
                {
                    setupEditChannel(editChannel[i], i);
                }
            }
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
    protected void setupEditChannel(Button b, int index)
    {
        b.setOnClickListener(v -> editChannelSettings(index));
    }
    protected void setupAmplitudeMinusButton (Button b, int index)
    {
        b.setOnClickListener(v -> {
            canRead = false;
            amplitudeOperation[index] = 1;
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void setupInputs ()
    {
        amplitudeInfo[0] = findViewById(R.id.manualAmplitude1);
        amplitudeInfo[1] = findViewById(R.id.manualAmplitude2);
        amplitudeInfo[2] = findViewById(R.id.manualAmplitude3);
        amplitudeInfo[3] = findViewById(R.id.manualAmplitude4);
        amplitudeInfo[4] = findViewById(R.id.manualAmplitude5);
        amplitudeInfo[5] = findViewById(R.id.manualAmplitude6);
        timeInfo[0] = findViewById(R.id.manualTimeLeft1);
        timeInfo[1] = findViewById(R.id.manualTimeLeft2);
        timeInfo[2] = findViewById(R.id.manualTimeLeft3);
        timeInfo[3] = findViewById(R.id.manualTimeLeft4);
        timeInfo[4] = findViewById(R.id.manualTimeLeft5);
        timeInfo[5] = findViewById(R.id.manualTimeLeft6);
        frequencyInfo[0] = findViewById(R.id.manualFrequency1);
        frequencyInfo[1] = findViewById(R.id.manualFrequency2);
        frequencyInfo[2] = findViewById(R.id.manualFrequency3);
        frequencyInfo[3] = findViewById(R.id.manualFrequency4);
        frequencyInfo[4] = findViewById(R.id.manualFrequency5);
        frequencyInfo[5] = findViewById(R.id.manualFrequency6);
        amplitudeButtonPlus[0] = findViewById(R.id.manualAmplitudePlus1);
        amplitudeButtonPlus[1] = findViewById(R.id.manualAmplitudePlus2);
        amplitudeButtonPlus[2] = findViewById(R.id.manualAmplitudePlus3);
        amplitudeButtonPlus[3] = findViewById(R.id.manualAmplitudePlus4);
        amplitudeButtonPlus[4] = findViewById(R.id.manualAmplitudePlus5);
        amplitudeButtonPlus[5] = findViewById(R.id.manualAmplitudePlus6);
        amplitudeButtonMinus[0] = findViewById(R.id.manualAmplitudeMinus1);
        amplitudeButtonMinus[1] = findViewById(R.id.manualAmplitudeMinus2);
        amplitudeButtonMinus[2] = findViewById(R.id.manualAmplitudeMinus3);
        amplitudeButtonMinus[3] = findViewById(R.id.manualAmplitudeMinus4);
        amplitudeButtonMinus[4] = findViewById(R.id.manualAmplitudeMinus5);
        amplitudeButtonMinus[5] = findViewById(R.id.manualAmplitudeMinus6);
        pauseButton[0] = findViewById(R.id.manualPause1);
        pauseButton[1] = findViewById(R.id.manualPause2);
        pauseButton[2] = findViewById(R.id.manualPause3);
        pauseButton[3] = findViewById(R.id.manualPause4);
        pauseButton[4] = findViewById(R.id.manualPause5);
        pauseButton[5] = findViewById(R.id.manualPause6);
        editChannel[0] = findViewById(R.id.editChannel1);
        editChannel[1] = findViewById(R.id.editChannel2);
        editChannel[2] = findViewById(R.id.editChannel3);
        editChannel[3] = findViewById(R.id.editChannel4);
        editChannel[4] = findViewById(R.id.editChannel5);
        editChannel[5] = findViewById(R.id.editChannel6);
        showProgramNum[0] = findViewById(R.id.manualShowProgram1);
        showProgramNum[1] = findViewById(R.id.manualShowProgram2);
        showProgramNum[2] = findViewById(R.id.manualShowProgram3);
        showProgramNum[3] = findViewById(R.id.manualShowProgram4);
        showProgramNum[4] = findViewById(R.id.manualShowProgram5);
        showProgramNum[5] = findViewById(R.id.manualShowProgram6);
        Button startButton =  findViewById(R.id.startProgramManual);
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
        setContentView(R.layout.activity_manual_mode_control);
        setupInputs();
        MainThread.start();
        findViewById(R.id.manualControlBackArrow).setOnClickListener(v -> {
            if (xionis.checkIfDevicesAreOff())
            {
                isNotClosed = false;
                Intent intent = new Intent();
                intent.setClass(ManualModeControl.this, ManualMode.class);
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