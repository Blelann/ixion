package android.vjr.activite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Parcelable;
import android.text.Editable;
import android.util.Log;
import android.view.View;

import android.vjr.R;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class BluetoothActivity extends Activity implements View.OnClickListener {

    private Button On, Off, notPaired;
    private BluetoothAdapter BA;
    private ListView lv;
    private ArrayList<BluetoothDevice> btDeviceList = new ArrayList<BluetoothDevice>();
    private ArrayList<String> btDeviceListName = new ArrayList<String>();
    private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_device_list);
        On = (Button) findViewById(R.id.button_on);
        Off = (Button) findViewById(R.id.button_off);
        notPaired = (Button) findViewById(R.id.button_not_paired);
        lv = (ListView) findViewById(R.id.listView_device);
        On.setOnClickListener(this);
        Off.setOnClickListener(this);
        notPaired.setOnClickListener(this);
        BA = BluetoothAdapter.getDefaultAdapter();
        //Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(ActionFoundReceiver, filter); // Don't forget to unregister during onDestroy
    }


    // Create a BroadcastReceiver for ACTION_FOUND

    private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("found","\n  Device: " + device.getName() + ", " + device);
                btDeviceList.add(device);
            } else {
                if(BluetoothDevice.ACTION_UUID.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                    for (int i=0; i< uuidExtra.length; i++) {
                       Log.d("found", "\n  Device: " + device.getName() + ", " + device + ", Service: " + uuidExtra[i].toString());
                    }
                } else {
                    if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                        Log.d("found","\nDiscovery Started...");
                    } else {
                        if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                            Log.d("\nDiscovery Finished","");
                            final ArrayList<String> liste = new ArrayList<String>();
                            for (android.bluetooth.BluetoothDevice bt : btDeviceList) {
                                if (bt != null && bt.getName() != null) {
                                    liste.add(bt.getName());
                                }
                            }
                            addToList(liste);
                            pd.dismiss();
                        }
                    }
                }
            }
        }
    };

    private void CheckBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // If it isn't request to turn it on
        // List paired devices
        // Emulator doesn't support Bluetooth and will return null
        if(BA==null) {
            Log.d("enabled","\nBluetooth NOT supported. Aborting.");
            return;
        } else {
            if (BA.isEnabled()) {
                Log.d("enabled","\nBluetooth is enabled...");
                if(BA.isDiscovering()) {
                }else {
                    // Starting the device discovery
                    BA.startDiscovery();
                }
            }else{
                Intent enableBtIntent = new Intent(BA.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    public void addToList(ArrayList<String> list ){
        final ArrayList<String>  listed = list;
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listed);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = listed.get(position);
                BA.getAddress();
                finishWithResult(name);
             };
        });
    }

    private void finishWithResult(String BTName){

        final   Bundle conData = new Bundle();
        for (android.bluetooth.BluetoothDevice bt : btDeviceList){
                if(bt.getName().equals(BTName)){
                    conData.putString("BTAddr",bt.getAddress());
                    Intent intent  = new Intent(BluetoothActivity.this, VJR.class);
                    intent.putExtras(conData);
                    setResult(RESULT_OK, intent);
                    startActivity(intent);
                    finish();
                }
        }
    }
    @Override
    public void onClick(View v) {
        Log.d("Id", v.toString());
        switch(v.getId()) {

            case R.id.button_not_paired:
                CheckBTState();
                pd = ProgressDialog.show(this, "Découverte du bluetooth","Veuillez patienter...", true);
                break;

            case R.id.button_on :
                if (!BA.isEnabled()) {
                    Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnOn, 0);

                } else {
                    Toast.makeText(getApplicationContext(), "Already on",
                            Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.button_off :
                BA.disable();
                break;
        }

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BA != null) {
            BA.cancelDiscovery();
        }
        unregisterReceiver(ActionFoundReceiver);
    }
}
