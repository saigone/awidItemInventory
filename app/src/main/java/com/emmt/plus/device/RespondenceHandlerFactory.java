package com.emmt.plus.device;

import android.util.Log;

import com.emmt.Utility.HexConverseUtil;
import com.emmt.plus.device.MprUtilityTool.MprStatus;
import com.emmt.plus.process.ProcessCorrectAck;
import com.emmt.plus.process.ProcessEPC;
import com.emmt.plus.process.ProcessErrorAck;
import com.emmt.plus.process.ProcessHighCapacityMemory;
import com.emmt.plus.process.ProcessMainboardVersion;
import com.emmt.plus.process.ProcessMultiTag;
import com.emmt.plus.process.ProcessSleep;
import com.emmt.plus.process.ProcessTID;
import com.emmt.plus.process.ProcessVersion;
import com.emmt.plus.process.ProcessEmmtStatus;
import com.emmt.plus.process.ProcessSystemStatus;

public class RespondenceHandlerFactory {
    private final static String TAG = RespondenceHandlerFactory.class.getSimpleName();

    public interface RespondenceProcessInterface {
        DataPackageEvent executeProcess(byte[] rcsp);
    }

    public static RespondenceProcessInterface createRespondenceProcessInstant(MprStatus status) {
        RespondenceProcessInterface instant = null;
        switch (status) {
            case CORRECT:
                Log.v(TAG, "收到ACK正常，單一回覆，沒有其他的回應需接收");
                instant = new ProcessCorrectAck();
                break;
            case ERROR:
                Log.v(TAG, "收到ERROR，該指令出現異常");
                instant = new ProcessErrorAck();
                break;
            case EPC:
                Log.v(TAG, "收到EPC指令");
                instant = new ProcessEPC();
                break;
            case TID:
                Log.v(TAG, "收到TID指令");
                instant = new ProcessTID();
                break;
            case READ_HIGH_CAPACITY_MEMORY:
                Log.v(TAG, "收到READ_HIGH_CAPACITY_MEMORY指令");
                instant = new ProcessHighCapacityMemory();
                break;
            case MULTI:
                Log.v(TAG, "收到MULTI指令");
                instant = new ProcessMultiTag();
                break;
            case SLEEP:
                Log.v(TAG, "收到SLEEP指令");
                instant = new ProcessSleep();
                break;
            case VERSION:
                Log.v(TAG, "收到VERSION指令");
                instant = new ProcessVersion();
                break;
            case EMMT_READER_STATUS:
                Log.v(TAG, "收到EMMT READER STATUS指令");
                instant = new ProcessEmmtStatus();
                break;
            case SYSTEM_READER_STATUS:
                Log.v(TAG, "收到SYSTEM READER STATUS指令");
                instant = new ProcessSystemStatus();
                break;
            case MAINBOARD_VERSION:
                Log.v(TAG, "收到MAINBOARD VERSION指令");
                instant = new ProcessMainboardVersion();
                break;
            default:
                Log.v(TAG, "不在預期內的資料");
                instant = new ProcessErrorAck();
                break;
        }

        return instant;
    }
}
