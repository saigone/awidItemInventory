package com.emmt.plus.device;

import java.io.IOException;
import java.io.InputStream;

import com.emmt.Utility.CommandGenerator;
import com.emmt.Utility.HexConverseUtil;
import com.emmt.Utility.MPR1910CmdUtil;
import com.emmt.Utility.MPRCmdCRCUtil;
import com.emmt.plus.device.MprUtilityTool.MprStatus;
import com.emmt.plus.device.RespondenceHandlerFactory.RespondenceProcessInterface;

import android.util.Log;

public class RespondenceReceiver {
    private final static String TAG = RespondenceReceiver.class.getSimpleName();
    private final static String READ_EPC_TIME_OUT = "FF1080FEC1";

    private InputStream mInStream = null;
    private MprStatus mCurrentStatus = MprStatus.PRESS;
    private volatile boolean isRunnungLoop = true;
    private byte[] mReaderRcsp;
    private HandyDeviceHB mDevice;
    private boolean isMultiMode = true;
    private boolean isEnableHandyTrigger = false;


    interface RespondCallback {
        void onCommandEvent(byte[] command);

        void onRespondenceEvent(DataPackageEvent dataEvent);
    }

    private void sendCommandEventCallback(byte[] command) {
        mDevice.onCommandEvent(command);
    }

    private void sendRespondenceEventCallback(DataPackageEvent dataPackageEvent) {
        mDevice.onRespondenceEvent(dataPackageEvent);
    }

    public RespondenceReceiver(HandyDeviceHB device) {
        mDevice = device;
        mInStream = device.getInputStream();
    }

    public void stop() {
        isRunnungLoop = false;
    }

    public void switchToMultiMode(boolean change) {
        isMultiMode = change;
    }

    public void enableHandyTrigger(boolean change) {
        isEnableHandyTrigger = change;
    }

    public void handleDataProcess() {
        Log.v(TAG, "準備進入讀取迴圈，目前的狀態: " + mCurrentStatus.name());
        isRunnungLoop = true;
        while (isRunnungLoop) {
            try {
                receiveRespondence();
                handleRespondByState();
            } catch (IOException e) {
                isRunnungLoop = false;
                Log.v(TAG, "ERROR: " + e.getMessage());
            } catch (Exception e) {
                isRunnungLoop = false;
                Log.v(TAG, "ERROR: " + e.getMessage());
            }
        }
        Log.v(TAG, "結束讀取迴圈，目前的狀態: " + mCurrentStatus.name());
    }

    private void receiveRespondence() throws Exception {
        try {
            int firstByte = getFirstByte();
            if (firstByte == 0) {
                if (hasUnreceivedRcsp())
                    mCurrentStatus = MprStatus.ACCEPTING;
                else
                    mCurrentStatus = MprStatus.CORRECT;
            } else if (firstByte == 0xFF) {
                mCurrentStatus = MprStatus.ERROR;
            } else {
                receiveRestRespondData(firstByte);
                checkStatusFromRestRespond();
            }
        } catch (CRCException e) {
            mCurrentStatus = MprStatus.ERROR;
        } catch (IOException e) {
            throw new IOException(e.toString());
        } catch (Exception e) {
            throw new Exception(e.toString());
        }

    }

    private int getFirstByte() throws IOException {
        int firstByte = mInStream.read();
        Log.v(TAG, "取得第一個BYTE: " + firstByte);

        return firstByte;
    }

    private boolean hasUnreceivedRcsp() {
        boolean isRest = false;
        int tryTime = 5;
        try {
            while ((tryTime--) != 0) {
                Thread.sleep(50);
                int rest = mInStream.available();
                if (rest > 0) {
                    isRest = true;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return isRest;
    }

    private void receiveRestRespondData(int rcspLength) throws CRCException, Exception {
        Log.v(TAG, "receive Rest Respond Data");
        // 剩餘資料長度 = 總長度 - 1
        rcspLength--;
        byte[] rcsp = new byte[rcspLength];
        int readCount = 0;
        try {
            while (readCount < rcspLength) {
                readCount += mInStream.read(rcsp, readCount, rcspLength - readCount);
            }
        } catch (IOException e) {
            throw new IOException(e.toString());
        }

        if (isRightCRC(rcspLength, rcsp))
            mReaderRcsp = rcsp;
        else
            throw new CRCException("CRC ERROR: " + HexConverseUtil.bytesToHexString(rcsp));
    }

    private boolean isRightCRC(int rcspLength, byte[] rcsp) throws Exception {
        try {
            rcspLength++; // 總長度 = 剩下長度 + 1
            byte[] fullrcsp = new byte[rcspLength]; // 完整的回應長度
            fullrcsp[0] = (byte) rcspLength; // 第一欄為總長度

            for (int i = 1; i < rcspLength; i++) { // 依序給與各欄位值
                fullrcsp[i] = rcsp[i - 1];
            }

            int i = MPRCmdCRCUtil.checkCRCFromReaderToHost(fullrcsp, rcspLength);
            return (i == 0);
        } catch (Exception ex) {
            throw new Exception("CRC ERROR");
        }
    }

    private void checkStatusFromRestRespond() throws Exception {
        Log.v(TAG, "check State From Rest Respond");
        try {
            Log.v(TAG, "RCSP: " + HexConverseUtil.bytesToHexString(mReaderRcsp).toUpperCase());
            mCurrentStatus = MprUtilityTool.checkStateFromRestRespond(mReaderRcsp);
        } catch (Exception ex) {
            throw new Exception(ex.toString());
        }
    }

    private void handleRespondByState() throws Exception {
        try {
            switch (mCurrentStatus) {
                case PRESS:
                    Log.v(TAG, "收到按下指令");
                    if (isMultiMode) {

                        if(isEnableHandyTrigger) {
                            Log.v(TAG, "傳送multi指令");
                            sendCommandEventCallback(CommandGenerator.buildInventoryCmd());
                        }
                    } else {
                        if(isEnableHandyTrigger) {
                            Log.v(TAG, "傳送single指令");
                            sendCommandEventCallback(CommandGenerator.buildSingleEPCCmd());
                        }
                    }
                    break;
                case RELEASE:
                    Log.v(TAG, "收到放開指令");
                    if(isEnableHandyTrigger)
                        sendCommandEventCallback(CommandGenerator.buildStopCmd());
                    break;
                case ACCEPTING:
                    Log.v(TAG, "收到ACK正常，準備接收剩下資料");
                    break;
                default:
                    Log.v(TAG, "C1G2指令");
                    RespondenceProcessInterface handler = RespondenceHandlerFactory.createRespondenceProcessInstant(mCurrentStatus);
                    DataPackageEvent dataPackageEvent = handler.executeProcess(mReaderRcsp);
                    sendRespondenceEventCallback(dataPackageEvent);
                    break;
            }
            Log.v(TAG, "- - - - - - - - - - - - - - - - - - - - - - - - -");
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }
}
