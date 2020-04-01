package com.example.easysleep;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Handler;

public class BTService {
    private static final String TAG = "DEBUG_TAG";
    //private Handler handler;
    private android.os.Handler handler;

    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket btSocket;
        private final InputStream is;
        private final OutputStream os;
        private byte[] messageBuffer;

        public ConnectedThread(BluetoothSocket socket) {
            btSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = btSocket.getInputStream();
            } catch (IOException ioe) {
                Log.e(TAG, "Error occured when creating input stream", ioe);
            }
            try {
                tmpOut = btSocket.getOutputStream();
            } catch (IOException ioe) {
                Log.e(TAG, "Error occured when creating output stream", ioe);
            }

            is = tmpIn;
            os = tmpOut;
        } // constructor

        public void run() {
            messageBuffer = new byte[1024];
            int numBytes;

            while(true) {
                try {
                    numBytes = is.read(messageBuffer);
                    Message readMsg = handler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1, messageBuffer);
                    readMsg.sendToTarget();
                } catch (IOException ioe) {
                    Log.d(TAG, "Input stream was disconnected", ioe);
                    break;
                }
            }
        } // run

        public void write(byte[] bytes) {
            try {
                os.write(bytes);
                Message writtenMessage = handler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, messageBuffer);
                writtenMessage.sendToTarget();
            } catch (IOException ioe) {
                Log.d(TAG, "Error occured when sending data", ioe);
                Message writeErrorMsg = handler.obtainMessage(
                        MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast", "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        } // write

        public void cancel() {
            try {
                btSocket.close();
            } catch (IOException ioe) {
                Log.e(TAG, "Could not close the connect socket");
            }
        } // cancel
    }
}
