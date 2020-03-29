package com.example.easysleep;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_BT_ENABLE = 0;
    private static final int REQUEST_BT_DISCOVER = 1;

    Button searchButton;
    Button toggleBTButton;
    Button connectButton;
    ImageView btIcon;

    TextView statusView;

    BluetoothAdapter btAdapter;
    Intent enableBTIntent;

    ArrayList<String> bluetoothDevices = new ArrayList<>();
    ArrayList<String> addresses = new ArrayList<>();

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("Action", action);

            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                statusView.setText("Finished");
                searchButton.setEnabled(true);
            } else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                String address = device.getAddress();
                String rssi = Integer.toString(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                Log.i("Device found", "Name: " + name + " Address: " + address + " RSSI: " + rssi);

                if(name.equals("SONAR064844")) {
                    showToast("Sonaro available");
                    statusView.setText("Sonaro available");
                    btAdapter.cancelDiscovery();
                    connectButton.setEnabled(true);
                }
               /*
                if(!addresses.contains(address)) {
                    addresses.add(address);

                    String deviceString = "";
                    if(name == null || name.equals("")) {
                        deviceString = address + " - RSSI - " + rssi + "dBm";
                    } else {
                        deviceString = name + " - RSSI - " + rssi + "dBm";
                    }

                    bluetoothDevices.add(deviceString);
                    //arrayAdapter.notifyDataSetChanged();
                }

                */
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linkUIElements();

        connectButton.setEnabled(false);
        searchButton.setEnabled(true);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);

        registerReceiver(broadcastReceiver, intentFilter);

        // does the device support BT
        if(btAdapter == null) {
            statusView.setText("Bluetooth is not available");
            toggleBTButton.setEnabled(false);
        } else {
            statusView.setText("Bluetooth is available");
            toggleBTButton.setEnabled(true);
        }

        // display BT status icon
        if(btAdapter.isEnabled()) {
            btIcon.setImageResource(R.drawable.bt_on);
        } else {
            btIcon.setImageResource(R.drawable.bt_off);
        }

        // toggle Bluetooth adapter
        toggleBTButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!btAdapter.isEnabled()) {
                    showToast("Turning BT on");

                    Intent btIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(btIntent, REQUEST_BT_ENABLE);
                } else {
                    showToast("Turning BT off");
                    btAdapter.disable();
                    btIcon.setImageResource(R.drawable.bt_off);
                }
            }
        });

        // search for Easysleep
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btAdapter.isEnabled()) {
                    statusView.setText("Searching...");
                    searchButton.setEnabled(false);
                    bluetoothDevices.clear();
                    addresses.clear();
                    checkBTPermissions();
                    btAdapter.startDiscovery();
                } else {
                    showToast("Need to enable BT first");
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void linkUIElements() {
        searchButton = findViewById(R.id.searchButton);
        toggleBTButton = findViewById(R.id.toggleBTButton);
        connectButton = findViewById(R.id.connectButton);
        btIcon = findViewById(R.id.btIconView);
        statusView = findViewById(R.id.statusView);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_BT_ENABLE:
                if (resultCode == RESULT_OK) {
                    btIcon.setImageResource(R.drawable.bt_on);
                    showToast("Bluetooth is on");
                } else {
                    showToast("Couldn't turn Bluetooth on");
                }
                break;
            default: showToast("unknown request"); break;
        }
    }

    // check permissions for fine and coarse location (needed for Android 6.0 and above,
    // declaring these permissions in Manifest.xml is not enough)
    private void checkBTPermissions() {
        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");

        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");

        if (permissionCheck != 0) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        }
    }
}
