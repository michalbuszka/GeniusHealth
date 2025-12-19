package com.geniushealth;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class BluetoothConnection extends Application {
    public BluetoothAdapter bluetoothAdapter;
    public BluetoothSocket socket;
    public InputStream inputStream;
    public OutputStream outputStream;
    public String deviceName = "XM-15";
    public boolean tryConnect = false;
    public void reconnect ()
    {
        this.tryConnect = true;
    }
    public boolean isDeviceConnected ()
    {
        if ((!this.bluetoothAdapter.isEnabled()) || (this.outputStream == null) || (this.inputStream == null) || (!this.socket.isConnected()))
        {
            return false;
        }
        return true;
    }
    protected Thread bluetoothThread = new Thread ()
    {
        public void run ()
        {
            while (true)
            {
                if (tryConnect)
                {
                    connectDevice();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    protected void connectDevice ()
    {
        do {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!bluetoothAdapter.isEnabled())
                {
                    bluetoothAdapter.enable();
                    do {

                    }while(!bluetoothAdapter.isEnabled());
                }
                Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
                for (BluetoothDevice device : devices)
                {
                    if (device.getName().equals(deviceName))
                    {
                        try {
                            createSocket(device);
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    }
                }
        }while(socket == null || !socket.isConnected());
    }
    protected void createSocket (BluetoothDevice device) throws IOException {
        ParcelUuid[] uUids = device.getUuids();
            socket = device.createRfcommSocketToServiceRecord(uUids[0].getUuid());
            socket.connect();
            if (socket.isConnected())
            {
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                tryConnect = false;
            }
    }
    public void setupBluetooth ()
    {
        connectDevice();
        bluetoothThread.start();
    }
}
