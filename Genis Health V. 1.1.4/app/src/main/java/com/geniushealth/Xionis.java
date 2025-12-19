package com.geniushealth;

import android.app.Activity;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class Xionis extends Modbus {
    public XionisDevice[] xionisDevice = new XionisDevice[6];
    protected static BluetoothConnection bluetoothConnection;
    protected Activity activity;
    Xionis (Activity activity)
    {
        this.activity = activity;
    }
    public static int ByteArray2Int(byte[] bytes) {
        int val1 = bytes[0] & 0xFF;
        int val2 = bytes[1] & 0xFF;
        return  val1 * 256 + val2;
    }
    public static class DeviceParameters
    {
        public int programNumber;
        public int timeDuration;
        DeviceParameters ()
        {
            this.programNumber = 0;
            this.timeDuration = 3000;
        }
    }
    protected void showFrequency (TextView text, int index)
    {
        String frequency = String.valueOf(this.xionisDevice[index].register[Xionis.Index.baseFrequency]);
        final String finalText = "Frequency: "+ frequency + " Hz";
        this.activity.runOnUiThread(() -> text.setText(finalText));
    }
    protected void showAmplitude (TextView text, int index)
    {
        int amplitude = this.xionisDevice[index].register[Xionis.Index.amplitude];
        final String finalAmplitude = 100 * amplitude / Settings.maxAmplitude+"%";
        this.activity.runOnUiThread(() -> text.setText(finalAmplitude));
    }
    protected void createDevices (BluetoothConnection bluetoothConnection)
    {
        Xionis.bluetoothConnection = bluetoothConnection;
        for (int i = 0; i < 6; i++)
        {
            xionisDevice[i] = new Xionis.XionisDevice(0x50 + i);
        }
    }
    public boolean checkIfDevicesAreOff()
    {
        for (int i = 0; i < 6; i++)
        {
            if (this.xionisDevice[i].register[Xionis.Index.onOff] == 1)
            {
                return false;
            }
        }
        return true;
    }
    public static class Address
    {
        static final int reset40_249 = 0x0;
        static final int timeOffStepAmplitude = 0x1;
        static final int TimeOnStepAmplitude = 0x2;
        static final int stepAmplitude = 0x3;
        static final int stepFreqency = 0x4;
        static final int baseFrequencyMin = 0x5;
        static final int baseFrequencyMax = 0x6;
        static final int programTime = 0x9;
        static final int onOff = 0x8;
        static final int timer = 0x7;
        static final int amplitude = 10;
        static final int baseFrequency = 11;
        static final int carrierFrequency = 12;
        static final int dutyOfBaseFrequency = 13;
        static final int pulseOfBaseFrequency = 14;
        static final int timeOfPulseON = 15;
        static final int rampRiseTime = 16;
        static final int rampFallTime = 17;
        static final int delayOn = 18;
        static final int tonPlusToff = 19;
        static final int Type = 20;
    }
    public static class Index
    {
        static final int reset40_249 = 0;
        static final int timeOffStepAmplitude = 1;
        static final int TimeOnStepAmplitude = 2;
        static final int stepAmplitude = 3;
        static final int stepFreqency = 4;
        static final int baseFrequencyMin = 5;
        static final int baseFrequencyMax = 6;
        static final int programTime = 7;
        static final int onOff = 8;
        static final int timer = 9;
        static final int amplitude = 10;
        static final int baseFrequency = 11;
        static final int carrierFrequency = 12;
        static final int dutyOfBaseFrequency = 13;
        static final int pulseOfBaseFrequency = 14;
        static final int timeOfPulseON = 15;
        static final int rampRiseTime = 16;
        static final int rampFallTime = 17;
        static final int delayOn = 18;
        static final int tonPlusToff = 19;
        static final int Type = 20;
    }
    public static class XionisDevice
    {
        protected int deviceAddress;
        public int[] register = new int[22];
        public XionisDevice (int deviceAddress)
        {
            this.deviceAddress = deviceAddress;
        }
        public void manageData (byte[] frame, int startingAddressFromStart)
        {
            if (frame[1] == Code.READ_N_REGISTERS)
            {
                int counter = 0;
                int amountBytes = frame[2] & 0xFF;
                for (int i = 0; i < amountBytes - 1; i+=2)
                {
                    byte[] tempByte = new byte[2];
                    tempByte[0] = frame[3 + i];
                    tempByte[1] = frame[3 + i+1];
                    int registerValue = ByteArray2Int(tempByte);
                    this.register[counter + startingAddressFromStart] = registerValue;
                    counter++;
                }
            }
        }
        public synchronized void setRegisters (byte[] reqBytes)
        {
            this.sendFrame(reqBytes,0);
        }
        public synchronized void setRegister (int address, int value)
        {
            byte[] reqBytes = prepareWriteRegisterFrame(this.deviceAddress, address, value);
            this.sendFrame(reqBytes, 0);
        }
        public synchronized  void sendBroadcastFrame (byte[] frame)
        {
            boolean success;
            do
            {
                success = true;
                while (!bluetoothConnection.isDeviceConnected())
                {
                    bluetoothConnection.reconnect();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    success = false;
                }
                try {
                    bluetoothConnection.outputStream.write(frame);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                    bluetoothConnection.reconnect();
                    success = false;;
                }
            }while(!success);
        }
        public void waitForData (byte[] sendingFrame, InputStream stream) throws IOException {

            switch (sendingFrame[1])
            {
                case Code.READ_N_REGISTERS:
                    do {

                    }while(stream.available() != 13);
                    break;
                case Code.WRITE_N_REGISTERS:
                case Code.WRITE_SINGLE_REGISTER:
                    do {

                    }while(stream.available() != 8);
                    break;
            }

            /*try{
                Thread.sleep(100);
            }catch (InterruptedException e)
            {
                e.printStackTrace();
            }

             */
        }
        public void setRegisterForAllDevices (int address, int value)
        {
            byte[] reqBytes = prepareWriteRegisterFrame(1, address, value);
            sendBroadcastFrame(reqBytes);
        }
        public synchronized void sendFrame (byte[] frame, int startingIndex)
        {
            boolean success;
            boolean isTimeOuted;
            do
            {
                success = false;
                isTimeOuted = false;
                while (!bluetoothConnection.isDeviceConnected())
                {
                    bluetoothConnection.reconnect();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    long time1, time2;
                    bluetoothConnection.outputStream.write(frame);
                    time1 = System.currentTimeMillis();
                    do {
                        time2 = System.currentTimeMillis();
                        if (time2 - time1 >= Settings.maxFrameSilenceDelay)
                        {
                            isTimeOuted = true;
                            break;
                        }
                    }while (bluetoothConnection.inputStream.available() == 0);
                    if (isTimeOuted)
                    {
                        continue;
                    }
                    waitForData(frame, bluetoothConnection.inputStream);
                    byte[] frameByte = new byte[256];
                    int frameLength = bluetoothConnection.inputStream.read(frameByte);
                    byte[] cutFrame = new byte[frameLength];
                    for (int i = 0; i < frameLength; i++)
                    {
                        cutFrame[i] = frameByte[i];
                    }
                    if (checkSum(cutFrame, frameLength))
                    {
                        success = true;
                        manageData(cutFrame, startingIndex);
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                    bluetoothConnection.reconnect();
                    break;
                }
            }while (!success);
        }
        protected synchronized void getData ()
        {
            this.sendFrame(prepareReadNRegistersFrame(this.deviceAddress, 0, 21), 0);
        }
    }
}
