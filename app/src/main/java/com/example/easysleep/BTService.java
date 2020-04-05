package com.example.easysleep;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class BTService {
    public static final String TAG = "BTService";
    public static final String appName = "PHONE_BT";

    // uuid specific to hc-05/hc-06
    private static final UUID UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private UUID deviceUUID;
    ProgressDialog progressDialog;

    private final BluetoothAdapter btAdapter;
    private BluetoothDevice btDevice;
    Context context;

    private AcceptThread insecureAcceptThread;
    private ConnectThread connectThread;


    public BTService(Context context) {
        this.btAdapter = BluetoothAdapter.getDefaultAdapter();
        this.context = context;
    }

    // accept thread runs waiting for incoming connections
    private class AcceptThread extends Thread {

        private final BluetoothServerSocket btServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            try {
                tmp = btAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, UUID_INSECURE);
                Log.d(TAG, "AcceptThread(): setting up the server socket using " + UUID_INSECURE);
            } catch(IOException ioe) {
                Log.d(TAG, "AcceptThread().IOException: " + ioe.getMessage());
            }
            btServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run(): AcceptThread running");
            BluetoothSocket btSocket = null;

            try {
                Log.d(TAG, "run(): RFCOM Server socket start ...");
                btSocket = btServerSocket.accept();
                Log.d(TAG, "run(): RFCOM Server socket accepted connection");
            } catch (IOException ioe) {
                Log.d(TAG, "run().IOException: " + ioe.getMessage());
            }

            if(btSocket != null) {
                connected(btSocket, btDevice);
            }

            Log.i(TAG, "AcceptThread Ended");
        }

        public void cancel() {
            Log.d(TAG, "Cancelling AcceptThread");
            try {
                btServerSocket.close();
            } catch (IOException ioe) {
                Log.d(TAG, "cancel(): Canceling AcceptThread failed" + ioe.getMessage());
            }
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket btSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread(): started");
            btDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.i(TAG, "ConnectThread.run()");

            try {
                Log.d(TAG, "run(): trying to create insecure RFCOMM socket using UUID: " + UUID_INSECURE);
                tmp = btDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException ioe) {
                Log.e(TAG, "run(): Couldn't create RFCOMM socket" + ioe.getMessage());
            }

            btSocket = tmp;
            btAdapter.cancelDiscovery();

            try {
                btSocket.connect();
                Log.d(TAG, "run(): ConnectThread connected");
            } catch (IOException e) {
                try {
                    btSocket.close();
                    Log.d(TAG, "Socket closed");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Log.d(TAG, "Failed to close socket" + ex.getMessage());
                }
                Log.e(TAG, "run(): Failed to connect ConnectThread" + e.getMessage());
            }
            connected(btSocket, btDevice);
        }

        public void cancel() {
            Log.d(TAG, "Cancelling ConnectThread");
            try {
                btSocket.close();
            } catch (IOException ioe) {
                Log.d(TAG, "cancel(): Canceling ConnectThread failed" + ioe.getMessage());
            }
        }
    }

    public synchronized void start() {
        Log.d(TAG, "START");

        if(connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if(insecureAcceptThread == null) {
            insecureAcceptThread = new AcceptThread();
            insecureAcceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startClient(): started");

        connectThread = new ConnectThread(device, uuid);
        connectThread.start();
    }
}

