package com.example.easysleep;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_BT_ENABLE = 0;
    private static final int REQUEST_BT_DISCOVER = 1;


    UUID MY_UUID = UUID.fromString("74bb3f1b-7130-4aa7-a083-4b081d0bc052");

    TextView btStatus, btPairedList;
    Button btToggleButton, btDiscover, btList;
    ImageView btIcon;
    ListView lv;

    BluetoothAdapter myBluetooth;
    BluetoothDevice[] btArray = new BluetoothDevice[20];
    Intent enableBluetoothIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btToggleButton = findViewById(R.id.btToggle);
        btDiscover = findViewById(R.id.btDiscover);
        btList = findViewById(R.id.btList);

        btStatus = findViewById(R.id.btStatus);
        //btPairedList = findViewById(R.id.paired_devices_tv);
        lv = findViewById(R.id.paired_devices_tv);

        btIcon = findViewById(R.id.btIcon);

        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        // check if BT is available
        if(myBluetooth == null) {
            btStatus.setText("Bluetooth is not available");
        } else {
            btStatus.setText("Bluetooth is available");
        }

        // set image according to btStatus(on/off)
        if(myBluetooth.isEnabled()) {
            btIcon.setImageResource(R.drawable.bt_on);
        } else {
            btIcon.setImageResource(R.drawable.bt_off);
        }

        IntentFilter filter  = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);


        btToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!myBluetooth.isEnabled()) {
                    showToast("Turning BT on");

                    Intent btIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(btIntent, REQUEST_BT_ENABLE);
                } else {
                    showToast("Turning BT off");
                    myBluetooth.disable();
                    btIcon.setImageResource(R.drawable.bt_off);
                }
            }
        });

        btDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!myBluetooth.isDiscovering()) {
                    showToast("Making your device discoverable");
                    Intent discoverIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(discoverIntent, REQUEST_BT_DISCOVER);
                } else {
                    showToast("Your device is already discoverable");
                }
            }
        });

        btList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> bt = myBluetooth.getBondedDevices();
                ArrayList<String> btNames = new ArrayList<>();
                //int index = 0;

                if(bt.size() > 0) {
                    for(BluetoothDevice device : bt) {
                       // btDevices[index] = device;
                        btNames.add(device.getName() + "\n" + device.getAddress());
                        //index++;
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, btNames);
                    lv.setAdapter(arrayAdapter);
                }
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = btArray[position];

                BluetoothSocket btSocket = null;
                myBluetooth.cancelDiscovery();
                try {
                    btSocket = btArray[position].createRfcommSocketToServiceRecord(MY_UUID);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_BT_ENABLE:
                if(resultCode == RESULT_OK) {
                    btIcon.setImageResource(R.drawable.bt_on);
                    showToast("Bluetooth is on");
                } else {
                    showToast("Couldn't turn Bluetooth on");
                }
                break;
            case REQUEST_BT_DISCOVER:
                if(resultCode == RESULT_OK) {
                    showToast("Your device is discoverable now");
                } else {
                    showToast("Couldn't make your device discoverable");
                }
                break;
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
    }

    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                switch(mDevice.getBondState()) {
                    case BluetoothDevice.BOND_BONDED:
                        Log.e("info", "BroadcastRececeiver: BOND_BONDED");
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.e("info", "BroadcastReceiver: BOND_BONDING");
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.e("info", "BroadcastReceiver: BOND_NONE");
                        break;
                }
            }
        }
    };
}
