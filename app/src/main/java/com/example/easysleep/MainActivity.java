package com.example.easysleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    public static final String TAG = "Main Activity";

    ImageView btIcon;
    Button btOnOff;
    Button btDiscoverable;
    Button btDiscover;
    Button btTime;
    Button btDate;
    Button btGetData;
    Button btConnect;
    TextView incomingMessage;
    StringBuilder messages;
    String messages1;


    BluetoothAdapter btAdapter;
    public ArrayList<BluetoothDevice> btDevices = new ArrayList<>();
    public DeviceListAdapter deviceListAdapter;
    ListView devicesListView;

    BTService btService;
    private static final UUID UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothDevice btDevice;

    public String easysleepAddress = "98:D3:32:70:B9:20";
    private final BroadcastReceiver broadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(btAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, btAdapter.ERROR);

                switch(state) {
                    case BluetoothAdapter.STATE_OFF:
                        btIcon.setImageResource(R.drawable.bt_off1);
                        disableButtons();
                        Log.d(TAG, "broadcastReceiver1: STATE_OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "broadcastReceiver1: STATE_TURNING_OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "broadcastReceiver1: STATE__TURNING_ON");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        btIcon.setImageResource(R.drawable.bt_on1);
                        enableButtons();
                        Log.d(TAG, "broadcastReceiver1: STATE_ON");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver broadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(btAdapter.ACTION_SCAN_MODE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, btAdapter.ERROR);

                switch(state) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "broadcastReceiver2: SCAN_MODE_CONNECTABLE_DISCOVERABLE");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "broadcastReceiver2: SCAN_MODE_CONNECTABLE");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "broadcastReceiver2: SCAN_MODE_NONE");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "broadcastReceiver2: STATE_CONNECTING");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "broadcastReceiver2: STATE_CONNECTED");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver broadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "broadcastReceiver3: ACTION_FOUND");

            if(action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btDevices.add(device);
                Log.d(TAG, "onReceive(): " + device.getName() + " : " + device.getAddress());
                deviceListAdapter = new DeviceListAdapter(context, R.layout.device_list_view, btDevices);
                devicesListView.setAdapter(deviceListAdapter);
            }
        }
    };

    private final BroadcastReceiver broadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "broadcastReceiver4: ACTION_BOND_STATE_CHANGED");

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "broadcastReceiver4: BOND_BONDED");
                    btDevice = device;
                }
                if(device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "broadcastReceiver4: BOND_BONDING");
                }
                if(device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "broadcastReceiver4: BOND_NONE");
                }
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //incomingMessage.setText("");
            String text = intent.getStringExtra("theMessage");
            messages.append(text);

            incomingMessage.setText(messages);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btIcon = findViewById(R.id.btIcon);


        btOnOff = findViewById(R.id.toggleBTButton);
        btDiscoverable = findViewById(R.id.btDiscoverable);
        btDiscover = findViewById(R.id.btDiscover);

        devicesListView = findViewById(R.id.lvDevices);

        btConnect = findViewById(R.id.btConnect);
        btTime = findViewById(R.id.btTime);
        btDate = findViewById(R.id.btDate);
        btGetData = findViewById(R.id.btRequestData);
        incomingMessage = findViewById(R.id.incomingText);
        messages = new StringBuilder();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));

        IntentFilter bondFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(broadcastReceiver4, bondFilter);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter.isEnabled()) {
            btIcon.setImageResource(R.drawable.bt_on1);
            enableButtons();
        } else {
            btIcon.setImageResource(R.drawable.bt_off1);
            disableButtons();
        }

        btOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBT();
            }
        });

        btDiscoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDiscoverable(60);
            }
        });

        btDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverBTDevices();
            }
        });

        devicesListView.setOnItemClickListener(MainActivity.this);

        btConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConnection();
            }
        });

        btTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send 'b' over to easysleep
                incomingMessage.setText(null);
                String timeString = "b";
                Log.d(TAG, "btTime: sending " + timeString);
                byte[] timeBytes = timeString.getBytes();

                btService.write(timeBytes);
                messages.setLength(0);
            }
        });

        btDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send 'c' over to easysleep
                incomingMessage.setText(null);
                String dateString = "c";
                Log.d(TAG, "btDate: sending " + dateString);
                byte[] dateBytes = dateString.getBytes();

                btService.write(dateBytes);
                messages.setLength(0);
            }
        });

        btGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incomingMessage.setText(null);
                String dataRequest = "e";
                Log.d(TAG, "bgGetData: sending " + dataRequest);
                byte[] dataRequestBytes = dataRequest.getBytes();

                btService.write(dataRequestBytes);
                messages.setLength(0);
            }
        });
    }

    // has to be paired first
    public void startConnection() {
        startBTConnection(btDevice, UUID_INSECURE);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection(): Initializing RFCOM Bluetooth connection");
        btService.startClient(device, uuid);

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy(): called");
        super.onDestroy();
        unregisterReceiver(broadcastReceiver1);
        unregisterReceiver(broadcastReceiver2);
        unregisterReceiver(broadcastReceiver3);
        unregisterReceiver(broadcastReceiver4);
        unregisterReceiver(mReceiver);
    }

    public void toggleBT() {
        if(btAdapter == null) {
            Log.d(TAG, "toggleBT : Your device doesn't support BT");
        }
        if(!btAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter btIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(broadcastReceiver1, btIntent);
        }
        if(btAdapter.isEnabled()) {
            btAdapter.disable();

            IntentFilter btIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(broadcastReceiver1, btIntent);
        }
    }

    public void toggleDiscoverable(int discoverDuration) {
        Log.d(TAG, "toggleDiscoverable(): toggling discoverability for 60 seconds");
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, discoverDuration);
        startActivity(discoverableIntent);

        IntentFilter discoverableIntentFilter = new IntentFilter(btAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(broadcastReceiver2, discoverableIntentFilter);
    }

    public void discoverBTDevices() {
        Log.d(TAG, "discoverBTDevices(): discovering nearby devices");

        if(btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
            Log.d(TAG, "discoverBTDevices(): Cancelling discovery");

            checkBTPermission();

            btAdapter.startDiscovery();
            IntentFilter discoveryIntentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(broadcastReceiver3, discoveryIntentFilter);
        }
        if(!btAdapter.isDiscovering()) {
            checkBTPermission();
            btAdapter.startDiscovery();
            IntentFilter discoveryIntentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(broadcastReceiver3, discoveryIntentFilter);
        }
    }

    public void checkBTPermission() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if(permissionCheck != 0) {
                this.requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            } else {
                Log.d(TAG, "checkBTPermission(): Build version > Lollipop");
            }
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        btAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick(): You clicked on a device");
        String deviceName = btDevices.get(i).getName();
        String deviceAddress = btDevices.get(i).getAddress();

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.d(TAG, "trying to bond with " + deviceName + " : " + deviceAddress);
            btDevices.get(i).createBond();
            btDevice = btDevices.get(i);

            btService = new BTService(MainActivity.this);
        }
    }

    public void enableButtons() {
        btDiscoverable.setEnabled(true);
        btDiscover.setEnabled(true);
        btConnect.setEnabled(true);
        btTime.setEnabled(true);
        btDate.setEnabled(true);
    }

    public void disableButtons() {
        btDiscoverable.setEnabled(false);
        btDiscover.setEnabled(false);
        btConnect.setEnabled(false);
        btTime.setEnabled(false);
        btDate.setEnabled(false);
    }
}
