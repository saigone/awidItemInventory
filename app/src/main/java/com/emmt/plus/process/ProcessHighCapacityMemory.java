package com.emmt.plus.process;

import com.emmt.Utility.HexConverseUtil;
import com.emmt.Utility.MPR1910CmdUtil;
import com.emmt.plus.device.DataPackageEvent;
import com.emmt.plus.device.MprUtilityTool;
import com.emmt.plus.device.RespondenceHandlerFactory;
import com.emmt.plus.device.RespondenceHandlerFactory.RespondenceProcessInterface;

/**
 * Created by Owner on 2015/6/3.
 */
public class ProcessHighCapacityMemory implements RespondenceProcessInterface {
    private final static String READ_MEMORY_TIME_OUT = "FF6D8080C4";

    @Override
    public DataPackageEvent executeProcess(byte[] rcsp) {
        DataPackageEvent dataPackageEvent = new DataPackageEvent();
        if (isTimeoutRespond(rcsp) == true) {
            dataPackageEvent.setTID("TIME_OUT");
        } else {
            byte[] afterShifting = MprUtilityTool.dataShiftLeft(rcsp); // shifting演算法
            String tid = HexConverseUtil.bytesToHexString(afterShifting).toUpperCase();
            tid = tid.substring(4);
            if (tid.length() > 24) { // 只取96bits，每個字元4bits
                tid = tid.substring(0, 24);
            }
            dataPackageEvent.setTID(tid);
        }

        dataPackageEvent.setStatus(true);
        dataPackageEvent.setCommandType(MPR1910CmdUtil.C1G2_COMMAND);
        dataPackageEvent.setCommand(MPR1910CmdUtil.C1G2_READ_HIGH_CAPACITY_MEMORY);

        return dataPackageEvent;
    }

    private boolean isTimeoutRespond(byte[] rcsp) {
        String result = HexConverseUtil.bytesToHexString(rcsp).toUpperCase();
        if(result.equals(READ_MEMORY_TIME_OUT))
            return true;

        return false;
    }
}
