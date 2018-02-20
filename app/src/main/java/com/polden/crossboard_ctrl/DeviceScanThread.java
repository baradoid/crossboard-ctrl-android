package com.polden.crossboard_ctrl;

import android.content.Context;
import android.os.Handler;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.util.concurrent.Semaphore;

/**
 * Created by Dmitry on 21.02.2018.
 */

public class DeviceScanThread extends Thread {
    Handler mHandler;
    Context parentContext;
    Semaphore semaphore;
    CrossBoardData cbData;

    DeviceScanThread(Context parentContext, Handler h, Semaphore semaphore, CrossBoardData cbData) {
        mHandler = h;
        this.parentContext = parentContext;
        this.semaphore = semaphore;
        this.cbData = cbData;
    }

    @Override
    public void run()
    {
        D2xxManager ftD2xx = null;
        FT_Device ftDev = null;
        FtReadThread ftReadThread = null;
        int ftConnTryCnt = 0;
        int devCount = 0;

        while(true) {

            mHandler.sendMessage(mHandler.obtainMessage(10, new String("iter " + ftConnTryCnt++ + "\n")));
            try {
// Get FT_Device and Open the port
                if(ftDev != null) {
                    mHandler.sendMessage(mHandler.obtainMessage(0, new String("ftDev not null, DeviceScanThread exits\n")));
                    return;
                }

                if(ftD2xx == null)
                    ftD2xx = D2xxManager.getInstance(parentContext);
                devCount = ftD2xx.createDeviceInfoList(parentContext);

                if(devCount > 0){
                    mHandler.sendMessage(mHandler.obtainMessage(0, new String("devCount: " + devCount + "\n")));
                }
                //else{
                //    mHandler.sendMessage(mHandler.obtainMessage(0, new String("devCount = 0\n")));
                //}
                //System.out.println(String.format("devCount: %d\n", devCount));
                //appendTextToTextView(String.format("devCount: %d\n", devCount));
                for (int i = 0; i < devCount; i++) {
                    ftDev = ftD2xx.openByIndex(parentContext, i);

                    if(ftDev == null){
                        mHandler.sendMessage(mHandler.obtainMessage(0, new String("ftDev NULL\n")));
                        break;
                    }
                    if(ftDev.isOpen() == true)
                        mHandler.sendMessage(mHandler.obtainMessage(0, new String("ftDev open\n")));
                    else
                        mHandler.sendMessage(mHandler.obtainMessage(0, new String("ftDev not open\n")));

                    if ((ftDev != null) && (ftDev.isOpen() == true))
                        break;
                }
////////// Configure the port to UART
                if ((ftDev != null) && (ftDev.isOpen() == true)) {
                    D2xxManager.FtDeviceInfoListNode fdiln = ftDev.getDeviceInfo();
                    //System.out.println(String.format("device desc: %s\n sn: %s\n", fdiln.description, fdiln.serialNumber));
                    //appendTextToTextView(String.format("device desc: %s\n sn: %s\n", fdiln.description, fdiln.serialNumber));
                    //final TextView textViewDescr = (TextView)findViewById(R.id.textViewDevName);
                    //textViewDescr.setText("description: " + fdiln.description + "\nserialNumber: " + fdiln.serialNumber);
                    //appendTextToTextView("description: " + fdiln.description + "\nserialNumber: " + fdiln.serialNumber);

                    mHandler.sendMessage(mHandler.obtainMessage(0, new String("description: " + fdiln.description + "\nserialNumber: " + fdiln.serialNumber + "\n")));

                    // Set Baud Rate
                    ftDev.setBaudRate(115200);
                    ftDev.setDataCharacteristics(D2xxManager.FT_DATA_BITS_8,
                            D2xxManager.FT_STOP_BITS_1,
                            D2xxManager.FT_PARITY_NONE);
                    ftDev.setFlowControl(D2xxManager.FT_FLOW_NONE, (byte)0x0b, (byte)0x0d);

                    if(ftReadThread == null){
                        ftReadThread = new FtReadThread(ftDev, mHandler, semaphore, cbData);
                        ftReadThread.start();
                        mHandler.sendMessage(mHandler.obtainMessage(0, new String("DeviceScanThread: wait for ftReadThread finish \n")));
                        ftReadThread.join();
                        ftReadThread = null;
                        ftDev = null;
                        mHandler.sendMessage(mHandler.obtainMessage(0, new String("DeviceScanThread: ftReadThread finished \n")));
                    }
                    else{
                        mHandler.sendMessage(mHandler.obtainMessage(0, new String("ftReadThread not null\n")));
                    }
                }

            } catch (D2xxManager.D2xxException ex) {
                ex.printStackTrace();
                ftDev.close();
            }
            catch(Exception e){
                e.printStackTrace();
                //System.out.println(e.getMessage());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
