package com.polden.crossboard_ctrl;

import android.os.Handler;

/**
 * Created by murinets on 21.02.2018.
 */

public class updateUiThread extends Thread {
    Handler h;
    CrossBoardData cbd;

    updateUiThread(CrossBoardData cbd, Handler handler){
        this.h = handler;
        this.cbd = cbd;
    }

    @Override
    public void run() {
        int i=0;
        CrossBoardData cbdLocal;
        while(true){
            String viewString = new String();
            String releViewString = new String();

            try {
                synchronized (cbd){
                    cbdLocal = (CrossBoardData)cbd.clone();

                }

                viewString += "enc1: " +  cbd.pos1 + "\n";
                viewString += "enc2: " +  cbd.pos2 + "\n";
                viewString += "distance: " +  cbd.distance + "\n";
                viewString += "headTemp: " +  cbd.headTemp + "\n";
                viewString += "batteryTemp: " +  cbd.batteryTemp + "\n";
                viewString += "cashCount: " +  cbd.cashCount + "\n";

                releViewString += "Rele state: \n";
                releViewString += "battery: ON" + "\n";
                releViewString += "usb: OFF" + "\n";
                releViewString += "fan: OFF" + "\n";
                releViewString += "heat: OFF" + "\n";
                releViewString += "mute: OFF" + "\n";
                releViewString += "BV: OFF" + "\n";




                viewString += i++;
                String uiStrings[] = new String[2];
                uiStrings[0] = viewString;
                uiStrings[1] = releViewString;
                h.sendMessage(h.obtainMessage(70, viewString));
                h.sendMessage(h.obtainMessage(71, releViewString));


                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

        }
    }
}
