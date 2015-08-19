package com.emmt.plus.process;

import com.emmt.Utility.MPR1910CmdUtil;
import com.emmt.plus.device.DataPackageEvent;
import com.emmt.plus.device.RespondenceHandlerFactory.RespondenceProcessInterface;

/**
 * Created by Owner on 2015/2/10.
 */
public class ProcessMainboardVersion implements RespondenceProcessInterface {
    @Override
    public DataPackageEvent executeProcess(byte[] rcsp) {
        DataPackageEvent dataPackageEvent = new DataPackageEvent();
        String version = new String(rcsp).trim();
        version = version.substring(2, version.length() - 2);

        dataPackageEvent.setStatus(true);
        dataPackageEvent.setCommandType(MPR1910CmdUtil.EMMT_COMMAND);
        dataPackageEvent.setCommand(MPR1910CmdUtil.EMMT_MAINBOARD_VERSION);
        dataPackageEvent.setVersion(version);

        return dataPackageEvent;
    }
}
