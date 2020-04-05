package com.example.easysleep;

import android.bluetooth.BluetoothAdapter;
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

    private final BluetoothAdapter btAdapter;
    Context context;

    private AcceptThread insecureAcceptThread;

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
}

