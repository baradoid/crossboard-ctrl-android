package com.polden.crossboard_ctrl;

import android.os.Handler;
import android.os.Message;

import com.ftdi.j2xx.FT_Device;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by murinets on 19.02.2018.
 */

public class FtReadThread extends Thread {

    public static final int readLength = 512;

    Handler mHandler;
    boolean bReadThreadGoing = false;
    FT_Device ftDev = null;


    volatile public static String lastString = new String("N/A\r\n");
    private volatile int xPos = 0;
    private volatile int yPos = 0;

    //volatile int iCpuTemp = -1488;
    volatile int iBatteryTemp = -1488;
    volatile int iHeadTemp = -1488;
    volatile int iDistance = -1;
    volatile int iPluginVer = 0x0914;
    private int cashCount = 0;

    Semaphore dataChangeSem;
    CrossBoardData cbData;

    FtReadThread(FT_Device ft, Handler h, Semaphore semaphore, CrossBoardData cbData){
        mHandler = h;
        //this.setPriority(Thread.MIN_PRIORITY);
        bReadThreadGoing = true;
        ftDev = ft;
        dataChangeSem = semaphore;
        this.cbData = cbData;
    }

    @Override
    public void run()
    {
        //int i;
        int pollPeroiod = 2;
        byte[] dataBuf = new byte[readLength];
        char charBuf[] = new char[200];
        int curMsgInd = 0;
        long lastCpuTempSend = 0;
        char charMsgReadyBuf[] = null;

        char msg[] = new char[200];
        final int readLength = 512;
        //ArrayList<Byte> uartMsg = new ArrayList<Byte>();

        int cpuTempSendPeriod = 2000;

        ftDev.setLatencyTimer((byte)1);


        int iavail;
        while(true == bReadThreadGoing)
        {
            synchronized(ftDev)
            {
                iavail = ftDev.getQueueStatus();
                if(iavail > 0){
                    if(iavail > readLength){
                        iavail = readLength;
                    }
                    ftDev.read(dataBuf, iavail);

                    for (int i = 0; i < iavail; i++) {
                        charBuf[curMsgInd] = (char)dataBuf[i];
                        if((charBuf[curMsgInd++] == '\n') || (curMsgInd >= 199)){
                            charMsgReadyBuf = new char[curMsgInd];
                            System.arraycopy(charBuf, 0, charMsgReadyBuf, 0, curMsgInd);
                            cbData.lastString = new String(charMsgReadyBuf);
                            dataChangeSem.release();
                            curMsgInd=0;
                        }
                    }
                    if(charMsgReadyBuf != null){
//                        Message msg = mHandler.obtainMessage(0, new String(charMsgReadyBuf));
//                        mHandler.sendMessage(msg);
                        charMsgReadyBuf = null;
                    }
                }

//                if( (System.currentTimeMillis()- lastCpuTempSend)>cpuTempSendPeriod) {
//                    lastCpuTempSend = System.currentTimeMillis();
//                    //ftDev.write(String.format("%02d\r\n", (int) cpuTemp).getBytes());
//                    ftDev.write(String.format("t=%d\n", getBatteryTemp()).getBytes());
//
//                    //appendTextToTextView("2s\n");
////                        if((udpSocket != null) && (serverAddr != null)){
////                            try {
////                                String str = new String("test");
////                                DatagramPacket packet = new DatagramPacket(str.getBytes(), str.length(), serverAddr, 8059);
////                                udpSocket.send(packet);
////                                //appendTextToTextView("send ok\n");
////                            } catch (IOException e) {
////                                e.printStackTrace();
////                            }
////                        }
                }
////            }
        }
    }


    public int getBatteryTemp() {
        return iBatteryTemp;
    }


    void parseString(String str)
    {

    }
}
