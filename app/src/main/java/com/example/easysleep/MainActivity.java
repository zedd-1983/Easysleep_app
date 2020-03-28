package com.example.easysleep;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothDevice device = adapter.getRemoteDevice("");
    BluetoothSocket tmp = null;
    BluetoothSocket mmSocket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
            tmp = (BluetoothSocket) m.invoke(device, 1);
        } catch (IOException e) {
            Log.e(TAG, "create() failed", e);
        } catch (NoSuchMethodException nsme) {
            Log.e(TAG, "createRFcommSocket failed", nsme);
        } catch (IllegalAccessException iae) {
            Log.e(TAG, "invoke failed", iae);
        } catch (InvocationTargetException ite) {
            Log.e(TAG, "invoke failed 2", ite);
        }
        mmSocket = tmp;
    }
}
