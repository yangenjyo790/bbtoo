package com.android.wangpubang.bbtool;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private ListView lvDevices;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<String> mDevices = new ArrayList<String>();
    private List<String> bluetoothDevices = new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;

    private BluetoothDevice device;

    private TextView mInfo;
    private BluetoothManager bluetoothManager;
    private Button mGet;
    private Button mStart;

    private BroadcastReceiver searchDevices = new BroadcastReceiver() {
        //接收
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle b = intent.getExtras();
            Object[] lstName = b.keySet().toArray();

            // 显示所有收到的消息及其细节
            for (int i = 0; i < lstName.length; i++) {
                String keyName = lstName[i].toString();
                Log.e("666", keyName + ">>>" + String.valueOf(b.get(keyName)));
            }
            BluetoothDevice device;
            // 搜索发现设备时，取得设备的信息；注意，这里有可能重复搜索同一设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.e("666", "==found: " + device.getAddress());
                //onRegisterBltReceiver.onBluetoothDevice(device);
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING://正在配对
                        Log.e("666", "正在配对......");
                        //onRegisterBltReceiver.onBltIng(device);
                        break;
                    case BluetoothDevice.BOND_BONDED://配对结束
                        Log.e("666", "完成配对");
                        //onRegisterBltReceiver.onBltEnd(device);
                        break;
                    case BluetoothDevice.BOND_NONE://取消配对/未配对
                        Log.e("666", "取消配对");
                        //onRegisterBltReceiver.onBltNone(device);
                    default:
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothManager =(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        lvDevices = (ListView) findViewById(R.id.lv_devices);

        //Intent intent = new Intent(MainActivity.this,BluetoothLeService.class);
        //startService(intent);

        arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,bluetoothDevices);
        lvDevices.setAdapter(arrayAdapter);
        lvDevices.setOnItemClickListener(this);

        mInfo = (TextView) findViewById(R.id.info);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            mInfo.setText("Not support BLE");
        }

        Button btn = (Button) findViewById(R.id.scan);
        btn.setOnClickListener(this);
        mGet = (Button) findViewById(R.id.geterror);
        mGet.setOnClickListener(this);
        mStart = (Button) findViewById(R.id.start);
        mStart.setOnClickListener(this);

        mDevices.clear();

        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);//搜索发现设备
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//行动扫描模式改变了
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//动作状态发生了变化
        //registerReceiver(searchDevices, intent);

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

//        if(mBluetoothAdapter.isEnabled()){
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                    builder.setTitle("This app needs location access");
//                    builder.setMessage("Please grant location access so this app can detect Bluetooth.");
//                    builder.setPositiveButton(android.R.string.ok, null);
//                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                        @Override
//                        public void onDismiss(DialogInterface dialog) {
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
//                            }
//                        }
//                    });
//                    builder.show();
//                }
//            }
//        }
    }

    private final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                Log.e("666", "---resultCode: " + resultCode);
                scanLeDevice(true);
                break;
        }
    }

    private boolean mScanning = false;

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, 10*1000);
            mScanning = true;
            Log.e("666", "===start to scanning");
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            mInfo.setText("Scanning");
        } else {
            if (mScanning) {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.e("666", "-----");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i=0; i<mDevices.size(); i++) {
                        if (mDevices.get(i).equals(device.getAddress())) {
                            Log.e("666", "exist... skip");
                            return;
                        }
                    }
                    mDevices.add(device.getAddress());
                    bluetoothDevices.add(device.getName() + ":" + device.getAddress());
                    Log.e("666", "" + device.getName() + ":" + device.getAddress());
                    arrayAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        scanLeDevice(false);
        String s = arrayAdapter.getItem(position);
        String address = s.substring(s.indexOf(":") + 1).trim();
        Log.e("666", "---try to connect: =" + address + "==");
        try {
            try {
                if (device == null) {
                    device = mBluetoothAdapter.getRemoteDevice(address);
                }
                Log.d("666", "---try to connect " + device.getAddress());
                //BluetoothLeService.getInstance().connect(device.getAddress());
                if (!connect(device.getAddress())) {
                    mInfo.setText("Connect to " + device.getAddress() + " failed!");
                }
            } catch (Exception e) {
                Log.e("666", "11" + e.toString());
            }
        } catch (Exception e) {
            Log.e("666", "22" + e.toString());
        }
    }

    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private final int STATE_CONNECTING = 11;
    private final int STATE_CONNECTED = 12;
    private int mConnectionState = -1;

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.e("666","BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        // Previously connected device. Try to reconnect.
        if (mBluetoothDeviceAddress != null&& address.equals(mBluetoothDeviceAddress)&& mBluetoothGatt != null) {
            Log.e("666","Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.e("666", "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.e("666", "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STATE_CONNECTED:
                    mInfo.setText("Device connected.");
                    break;
                case 2:
                    String str = (String)msg.obj;
                    //05H，F1H，11H，C1H，EFH，8FH，46H success
                    if ("05F111C1EF8F46".contains(str)) {
                        mInfo.setText("Success. You can get error code now.");
                    } else {
                        mInfo.setText(str);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override  //当连接上设备或者失去连接时会回调该函数
        public void onConnectionStateChange(BluetoothGatt gatt, int status,int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) { //连接成功
                Log.e("666", "---connected");
                mHandler.sendMessage(mHandler.obtainMessage(STATE_CONNECTED));
                mConnectionState = STATE_CONNECTED;
                mBluetoothGatt.discoverServices(); //连接成功后就去找出该设备中的服务 private BluetoothGatt mBluetoothGatt;
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {  //连接失败
            }
        }
        @Override  //当设备是否找到服务时，会回调该函数
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {   //找到服务了
                //在这里可以对服务进行解析，寻找到你需要的服务
                Log.e("666", "get service successed " + status);
                if (gatt != null) {
                    mServices = gatt.getServices();
                    //displayGattServices(list);
//                    if (list != null) {
//                        for (int i = 0; i < list.size(); i++) {
//                            Log.e("666", "uuid: " + list.get(i).getUuid());
//                        }
//                    } else {
//                        Log.e("666", "==gatt service list is null!");
//                    }
                } else {
                    Log.e("666", "==gatt is null!");
                }
            } else {
                Log.e("666", "onServicesDiscovered received: " + status);
            }
        }
        @Override  //当读取设备时会回调该函数
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e("666", "onCharacteristicRead");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //读取到的数据存在characteristic当中，可以通过characteristic.getValue();函数取出。然后再进行解析操作。
                //int charaProp = characteristic.getProperties();if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0)表示可发出通知。  判断该Characteristic属性
                Log.e("666", "---read success");
                byte[] data = characteristic.getValue();
                Log.e("666", "got=" + bytesToHexString(data) + "=");
            } else {
                Log.e("666", "---read failed");
            }
        }

        @Override //当向设备Descriptor中写数据时，会回调该函数
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            System.out.println("onDescriptorWriteonDescriptorWrite = " + status + ", descriptor =" + descriptor.getUuid().toString());
        }

        @Override //设备发出通知时会调用到该接口
        public void onCharacteristicChanged(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic) {
            if (characteristic.getValue() != null) {
                System.out.println(characteristic.getStringValue(0));
            }
            System.out.println("--------onCharacteristicChanged-----");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            System.out.println("rssi = " + rssi);
        }
        @Override //当向Characteristic写数据时会回调该函数
        public void onCharacteristicWrite(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic, int status) {
            Log.e("666", "--------write success----- status:" + status);
            //should 05H，F1H，11H，C1H，EFH，8FH，46H
            Log.e("666", "=write feedback=" + bytesToHexString(characteristic.getValue()) + "=");
            mHandler.sendMessage(mHandler.obtainMessage(2, bytesToHexString(characteristic.getValue())));
        };
    };

    private String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private  List<BluetoothGattService> mServices;
    final private int START = 111;
    final private int GET_ERROR = 112;

    private void displayGattServices(List<BluetoothGattService> gattServices, int opt) {
        if (gattServices == null) {
            Log.e("666", "===service is null 22");
            return;
        }
        for (BluetoothGattService gattService : gattServices) { // 遍历出gattServices里面的所有服务
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) { // 遍历每条服务里的所有Characteristic
                Log.e("666", "++");
                switch (opt) {
                    case START:
                        wirteCharacteristic(gattCharacteristic, opt);
                        break;
                    case GET_ERROR:
                        readCharacteristic(gattCharacteristic);
                        break;
                }
//                if (gattCharacteristic.getUuid().toString().equalsIgnoreCase("")) {
//                    // 有哪些UUID，每个UUID有什么属性及作用，一般硬件工程师都会给相应的文档。我们程序也可以读取其属性判断其属性。
//                    // 此处可以可根据UUID的类型对设备进行读操作，写操作，设置notification等操作
//                    // BluetoothGattCharacteristic gattNoticCharacteristic 假设是可设置通知的Characteristic
//                    // BluetoothGattCharacteristic gattWriteCharacteristic 假设是可读的Characteristic
//                    // BluetoothGattCharacteristic gattReadCharacteristic  假设是可写的Characteristic
//                }
            }
        }
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e("666", "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID
                .fromString("00001800-0000-1000-8000-00805f9b34fb"));
        if (descriptor != null) {
            System.out.println("write descriptor");
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }

    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e("666", "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void wirteCharacteristic(BluetoothGattCharacteristic characteristic, int opt) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e("666", "BluetoothAdapter not initialized");
            return;
        }
        String cmdStr = null;
        switch (opt) {
            case START:
                //83H，11H，F1H，81H，04H
                cmdStr = "8311F18104";
                break;
            case GET_ERROR:
                //02H，18H，00H，1AH
                cmdStr = "0218001A";
                break;

        }

        byte[] cmd = hexStringToBytes(cmdStr);
        characteristic.setValue(cmd);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.scan) {
            mInfo.setText("Firstly stop bt");
            scanLeDevice(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanLeDevice(true);
                }
            }, 2*1000);
        }

        if (view.getId() == R.id.geterror) {
            if (mConnectionState != STATE_CONNECTED) {
                mInfo.setText("Please connect one device firstly");
            } else {
                mInfo.setText("getting");
            }
        }
        if (view.getId() == R.id.start) {
            if (mConnectionState != STATE_CONNECTED) {
                mInfo.setText("Please connect one device firstly");
            } else {
                mInfo.setText("starting");
                displayGattServices(mServices, START);
            }
        }
    }
}
