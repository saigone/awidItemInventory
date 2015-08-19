package com.emmt.plus.device;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.emmt.Utility.CommandGenerator;
import com.emmt.Utility.HexConverseUtil;
import com.emmt.Utility.MPR1910CmdUtil;
import com.emmt.Utility.StreamUtil;
import com.emmt.plus.device.RespondenceReceiver.RespondCallback;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

public class HandyDeviceHB implements RespondCallback {
    private final static UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final static String TAG = HandyDeviceHB.class.getSimpleName();
    public final static String ERROR_TAG = "ERROR_TAG";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;
    private InputStream mInStream = null;
    private OutputStream mOutStream = null;
    private String mAddress;
    private Context mContext;
    private CommandChannel mCommandChannel = new CommandChannel();
    private final static int WAIT_TIME = 500;
    private TagRequestQueue mTagQueue = new TagRequestQueue();
    private RespondenceReceiver mRespondenceReceiver;
    private boolean isReaderSleeping = false;
    private DataPackageEvent mDataEventPackage;
    private boolean isRunningInventory = false;

    public InputStream getInputStream() {
        return mInStream;
    }

    public OutputStream getOutputStream() {
        return mOutStream;
    }

    public HandyDeviceHB(Context context, String address) {
        mContext = context;
        mAddress = address;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void connect() {
        mBluetoothAdapter.cancelDiscovery();
        try {
            createBluetoothSocket();
            buildBluetoothConnection();
            initialIO();
            checkReaderType();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void disconnect() {
        StreamUtil.close(mInStream, mOutStream, mBluetoothSocket);
    }

    public boolean isConnected() {
        return mBluetoothSocket.isConnected();
    }

    private void waitForRespond() {
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void createBluetoothSocket() throws Exception {
        try {
            mBluetoothSocket = mBluetoothAdapter.getRemoteDevice(mAddress)
                    .createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    private void buildBluetoothConnection() throws IOException {
        try {
            mBluetoothSocket.connect();
            Log.v(TAG, "建立藍牙連線成功");
        } catch (IOException e) {
            e.printStackTrace();
            try {
                mBluetoothSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            throw new IOException("建立藍牙連結失敗，藍牙連結關閉");
        }
    }

    private void initialIO() throws IOException {
        try {
            mInStream = mBluetoothSocket.getInputStream();
            mOutStream = mBluetoothSocket.getOutputStream();
            Log.v(TAG, "開啟input/ouput串流");
        } catch (IOException e) {
            throw new IOException("開啟input/ouput串流失敗");
        }
    }

    private void checkReaderType() throws Exception {
        String name = mBluetoothSocket.getRemoteDevice().getName();
        Log.e(TAG, "Name: " + name);

        if (name.startsWith("HB-2000")) {
            getConnectedRespond();
            Log.v(TAG, "HB-2000");
        } else if (name.startsWith("HB-1000")) {
            Log.v(TAG, "HB-1000");
        } else {
            throw new Exception("不是HB系列機種");
        }
    }

    private void getConnectedRespond() throws Exception {
        int available = 0;
        while ((available = mInStream.available()) == 0) {
            Log.v(TAG, "available: " + available);
            Thread.sleep(250);
        }
        Log.v(TAG, "available: " + available);

        int rcspLength = 15;
        byte[] rcsp = new byte[rcspLength];
        int readCount = 0;
        try {
            while (readCount < rcspLength) {
                readCount += mInStream.read(rcsp, readCount, rcspLength
                        - readCount);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "get info from HB2000: " + HexConverseUtil.bytesToHexString(rcsp));
    }

    public void startCommandDispatching() {
        mCommandChannel.startWork();
    }

    public void stopCommandDispatching() {
        mCommandChannel.stopWork();
    }

    public void startRespondenceAnalyse() {
        mRespondenceReceiver = new RespondenceReceiver(this);
        new Thread() {
            public void run() {
                mRespondenceReceiver.handleDataProcess();
            }
        }.start();
    }

    public void stopRespondenceAnalyse() {
        mRespondenceReceiver.stop();
    }

    public String getMainboardVersion() throws ReaderErrorRespondException, ReaderSleepingException {
        DataPackageEvent event = sendCommandAndWaitRespond(CommandGenerator.buildMainboardVersionCmd());
        if (event.getStatus()) {
            return event.getVersion();
        } else {
            checkSleep();
        }

        throw new ReaderErrorRespondException("Mainboard Version respond is 0xFF");
    }

    public String getVersion() throws ReaderErrorRespondException, ReaderSleepingException {
        DataPackageEvent event = sendCommandAndWaitRespond(CommandGenerator.buildVersionCmd());
        if (event.getStatus()) {
            return event.getVersion();
        } else {
            checkSleep();
        }

        throw new ReaderErrorRespondException("Version respond is 0xFF");
    }

    public int getBatteryFromHB1000() throws ReaderErrorRespondException, ReaderSleepingException {

        DataPackageEvent event = sendCommandAndWaitRespond(CommandGenerator.buildRFIDStatusCmd());
        if (event.getStatus()) {
            return event.getBattery();
        } else {
            checkSleep();
        }

        throw new ReaderErrorRespondException("Battery respond is 0xFF");
    }

    public int getTemperatureFromHB1000() throws ReaderErrorRespondException, ReaderSleepingException {
        DataPackageEvent event = sendCommandAndWaitRespond(CommandGenerator.buildRFIDStatusCmd());
        if (event.getStatus()) {
            return event.getTemperature();
        } else {
            checkSleep();
        }

        throw new ReaderErrorRespondException("Temperature respond is 0xFF");
    }

    public String getTID() throws ReaderErrorRespondException, ReaderSleepingException {
        DataPackageEvent event = sendCommandAndWaitRespond(CommandGenerator.buildTIDCmd());
        if (event.getStatus()) {
            return event.getTID();
        } else {
            checkSleep();
        }

        throw new ReaderErrorRespondException("TID respond is 0xFF");
    }

    public String getTID2() throws ReaderErrorRespondException, ReaderSleepingException {
        DataPackageEvent event = sendCommandAndWaitRespond(CommandGenerator.buildTID2Cmd());
        if (event.getStatus()) {
            return event.getTID();
        } else {
            checkSleep();
        }

        throw new ReaderErrorRespondException("TID respond is 0xFF");
    }

    public String getEPC() throws ReaderErrorRespondException, ReaderSleepingException {
        DataPackageEvent event = sendCommandAndWaitRespond(CommandGenerator.buildSingleEPCCmd());
        if (event.getStatus()) {
            return event.getEPC();
        } else {
            checkSleep();
        }

        throw new ReaderErrorRespondException("EPC respond is 0xFF");
    }

    public boolean setPowerLevel(int powerLevel) throws ReaderErrorRespondException, ReaderSleepingException {
        if (0 <= powerLevel && powerLevel <= 225) {
            DataPackageEvent event = sendCommandAndWaitRespond(CommandGenerator.buildRFPowerLevelCmd(powerLevel));
            if (event.getStatus()) {
                return true;
            } else {
                checkSleep();
            }

            throw new ReaderErrorRespondException("Power setting is failed");
        } else {
            throw new ReaderErrorRespondException("Power must be set between 0 to 255");
        }
    }

    public int getPowerLevel() throws ReaderErrorRespondException, ReaderSleepingException {
        DataPackageEvent event = sendCommandAndWaitRespond(CommandGenerator.buildReaderStatusCmd());
        if (event.getStatus()) {
            return event.getPower();
        } else {
            checkSleep();
        }

        throw new ReaderErrorRespondException("Power respond is 0xFF");
    }

    public void startInventory() {
        onCommandEvent(CommandGenerator.buildInventoryCmd());
        isRunningInventory = true;
    }

    public void stopAllAction() {
        onCommandEvent(CommandGenerator.buildStopCmd());
        isRunningInventory = false;
    }

    public void startReadSingleTag() {
        onCommandEvent(CommandGenerator.buildSingleEPCCmd());
    }

    private DataPackageEvent sendCommandAndWaitRespond(byte[] command) {
        CommandRequest commandRequest = new CommandRequest(command, mOutStream);
        mCommandChannel.putCommand(commandRequest);
        waitForRespond();
        DataPackageEvent event = getResultDataPackage();
        if (event == null)
            return new DataPackageEvent();

        return event;
    }

    public String[] getInventoryResult() {
        return mTagQueue.getTags();
    }

    public void switchToMultiTagMode(boolean change) {
        mRespondenceReceiver.switchToMultiMode(change);
    }

    public void enableHandyTrigger(boolean change) {
        mRespondenceReceiver.enableHandyTrigger(change);
    }

    @Override
    public void onCommandEvent(byte[] commandEvent) {
        CommandRequest command = new CommandRequest(commandEvent, mOutStream);
        mCommandChannel.putCommand(command);
    }

    @Override
    public void onRespondenceEvent(DataPackageEvent dataEvent) {
        mDataEventPackage = dataEvent;

        boolean isSuccess = dataEvent.getStatus();
        byte type = dataEvent.getCommandType();
        byte command = dataEvent.getCommand();
        if (isSuccess) {
            Log.v(TAG, "Status: true");
            if (type == MPR1910CmdUtil.C1G2_COMMAND) {
                if (command == MPR1910CmdUtil.C1G2_PORTAL_ID)
                    mTagQueue.putTag(dataEvent.getEPC());
            }
        } else {
            Log.v(TAG, "Status: false");
            if (type == MPR1910CmdUtil.EMMT_COMMAND && command == MPR1910CmdUtil.EMMT_SLEEPING) {
                Log.v(TAG, "Sleep");
                isReaderSleeping = true;
            }
        }
    }

    private DataPackageEvent getResultDataPackage() {
        return mDataEventPackage;
    }

    private void checkSleep() throws ReaderSleepingException {
        Log.v(TAG, "checkSleep: " + isReaderSleeping);
        if (isReaderSleeping) {
            isReaderSleeping = false;
            throw new ReaderSleepingException("Reader is sleeping");
        }
    }

    // ------ undefine method ------------------

    public boolean isRunningInventory() {
        // TODO Auto-generated method stub
        return isRunningInventory;
    }

    public boolean isSleeping() {
        // TODO Auto-generated method stub
        return false;
    }

//    public void stopInventory() {
//        mTagQueue.putTag(ERROR_TAG);
//    }
}
