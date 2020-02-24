package com.example.easysleep;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    int REQUEST_BT_ENABLE = 1;

    Button btOnButton, btOffButton;
    BluetoothAdapter myBluetooth;
    Intent enableBluetoothIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btOnButton = findViewById(R.id.btEnable);
        btOffButton = findViewById(R.id.btDisable);

        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);


        bluetoothOn();
        bluetoothOff();

    }

    private void bluetoothOff() {
        btOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myBluetooth.isEnabled()) {
                    myBluetooth.disable();
                }
            }
        });
    }

    private void bluetoothOn()
    {
        btOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myBluetooth == null) {
                    Toast.makeText(getApplicationContext(), "Bluetooth not supported", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if(!myBluetooth.isEnabled())
                    {
                        startActivityForResult(enableBluetoothIntent, REQUEST_BT_ENABLE);
                    }
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == REQUEST_BT_ENABLE) {
            if(resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth is enabled", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Bluetooth enabling cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }
}
