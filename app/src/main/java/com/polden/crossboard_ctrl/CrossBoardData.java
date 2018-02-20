package com.polden.crossboard_ctrl;

/**
 * Created by Dmitry on 20.02.2018.
 */

public class CrossBoardData {
    String lastString;

    //volatile public static String lastString = new String("N/A\r\n");
    short pos1 = 0;
    short pos2 = 0;
    byte distance;
    byte headTemp;
    byte batteryTemp;
    int cashCount;


//    //volatile int iCpuTemp = -1488;
//    int iBatteryTemp = -1488;
//    int iHeadTemp = -1488;
//    int iDistance = -1;
//    int iPluginVer = 0x0914;
//    private int cashCount = 0;
    void parseCmdString()
    {


        try {
            synchronized (lastString) {
                pos1 = (short) Integer.parseInt(lastString.substring(0, 4), 16);
                pos2 = (short) Integer.parseInt(lastString.substring(5, 9), 16);
                headTemp = (byte) (Integer.parseInt(lastString.substring(10, 14), 10)/10);
                distance = (byte) Integer.parseInt(lastString.substring(15, 19), 10);
                cashCount = Integer.parseInt(lastString.substring(40, 46), 10);
            }
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }

//        pos1 = 0x1122;
//        pos2 = 0x3344;
//        distance = 0x77;
//        headTemp = 0x44;
    }
}
