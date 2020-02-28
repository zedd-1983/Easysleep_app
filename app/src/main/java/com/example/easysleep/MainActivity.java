package com.example.easysleep;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_BT_ENABLE = 0;
    private static final int REQUEST_BT_DISCOVER = 1;

    TextView btStatus, btPairedList;
    Button btToggleButton, btDiscover, btList;
    ImageView btIcon;

    BluetoothAdapter myBluetooth;
    Intent enableBluetoothIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btToggleButton = findViewById(R.id.btToggle);
        btDiscover = findViewById(R.id.btDiscover);
        btList = findViewById(R.id.btList);

        btStatus = findViewById(R.id.btStatus);
        btPairedList = findViewById(R.id.paired_devices_tv);

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
                if(myBluetooth.isEnabled()) {
                    btPairedList.setText("Paired Devices");
                    Set<BluetoothDevice> devices = myBluetooth.getBondedDevices();
                    for (BluetoothDevice device : devices) {
                        btPairedList.append("\nDevice " + device.getName() + ", " + device);
                    }
                } else {
                    showToast("Turn on bluetooth to acquire list of paired devices first");
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

//    private void bluetoothOff() {
//        btOffButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(myBluetooth.isEnabled()) {
//                    myBluetooth.disable();
//                }
//            }
//        });
//    }
//
//    private void bluetoothOn()
//    {
//        btOnButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(myBluetooth == null) {
//                    Toast.makeText(getApplicationContext(), "Bluetooth not supported", Toast.LENGTH_SHORT).show();
//                }
//                else
//                {
//                    if(!myBluetooth.isEnabled())
//                    {
//                        startActivityForResult(enableBluetoothIntent, REQUEST_BT_ENABLE);
//                    }
//                }
//            }
//        });
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data)
//    {
//        if(requestCode == REQUEST_BT_ENABLE) {
//            if(resultCode == RESULT_OK) {
//                Toast.makeText(getApplicationContext(), "Bluetooth is enabled", Toast.LENGTH_LONG).show();
//            } else if (resultCode == RESULT_CANCELED) {
//                Toast.makeText(getApplicationContext(), "Bluetooth enabling cancelled", Toast.LENGTH_LONG).show();
//            }
//        }
//    }
}
