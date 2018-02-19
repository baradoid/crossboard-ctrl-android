package com.polden.crossboard_ctrl;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static D2xxManager ftD2xx = null;
    FT_Device ftDev = null;
    public static int devCount = 0;
    public static int ftConnTryCnt = 0;

    //public int readcount = 0;
    //public int iavailable = 0;
    //byte[] readData;
    //char[] readDataToText;

    FtReadThread ftReadThread = null;
    DeviceScanThread devScanThread = null;
    UdpServerThread udpServThr = null;

    List<ReceiverParams> recvsList = Collections.synchronizedList(new ArrayList<ReceiverParams>());

    DatagramSocket udpSocket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //readDataToText = new char[readLength];
        //System.out.println("Started\n");
        //rescanFt();

        //System.out.println("init ok\n");
        devScanThread = new DeviceScanThread(handler1);
        devScanThread.start();

        udpServThr = new UdpServerThread(8055, udpMsgHandler);
        udpServThr.start();

        try {
            udpSocket = new DatagramSocket();
//            Message m = udpMsgHandler.obtainMessage();
//            m.obj = new String("udpSocket init Ok\n");
//            udpMsgHandler.sendMessage(m);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void onRefresh(View v)
    {
        //System.out.println(String.format("but pressed\n", ftConnTryCnt));
        rescanFt();
    }

    public void onFanEna(View v)
    {

        appendTextToTextView("onFanEna\n");
        if (ftDev != null) {
            synchronized(ftDev) {
                String cmd = new String("fanOn\n");
                ftDev.write(cmd.getBytes());
            }
        }
    }

    public void rescanFt()
    {
        ftConnTryCnt++;

        try {
            //System.out.println(String.format("ft scan try %d\n", ftConnTryCnt));
            //appendTextToTextView(String.format("ft scan try %d\n", ftConnTryCnt));
            try {
// Get FT_Device and Open the port
                if(ftDev != null)
                    return;

                if(ftD2xx == null)
                    ftD2xx = D2xxManager.getInstance(this);
                devCount = ftD2xx.createDeviceInfoList(this);

                //System.out.println(String.format("devCount: %d\n", devCount));
                //appendTextToTextView(String.format("devCount: %d\n", devCount));
                for (int i = 0; i < devCount; i++) {
                    ftDev = ftD2xx.openByIndex(this, i);

                    if ((ftDev != null) && (ftDev.isOpen() == true))
                        break;
                }
////////// Configure the port to UART
                if ((ftDev != null) && (ftDev.isOpen() == true)) {
                    D2xxManager.FtDeviceInfoListNode fdiln = ftDev.getDeviceInfo();
                    //System.out.println(String.format("device desc: %s\n sn: %s\n", fdiln.description, fdiln.serialNumber));
                    //appendTextToTextView(String.format("device desc: %s\n sn: %s\n", fdiln.description, fdiln.serialNumber));
                    final TextView textViewDescr = (TextView)findViewById(R.id.textViewDevName);
                    textViewDescr.setText("description: " + fdiln.description + "\nserialNumber: " + fdiln.serialNumber);

                    // Set Baud Rate
                    ftDev.setBaudRate(115200);
                    ftDev.setDataCharacteristics(D2xxManager.FT_DATA_BITS_8,
                            D2xxManager.FT_STOP_BITS_1,
                            D2xxManager.FT_PARITY_NONE);
                    ftDev.setFlowControl(D2xxManager.FT_FLOW_NONE, (byte)0x0b, (byte)0x0d);

                    if(ftReadThread == null){
                        ftReadThread = new FtReadThread(ftDev, ftMsgHandler);
                        ftReadThread.start();
                    }
                }
                else {
                    //System.out.println("no ft device open\n");
                    //appendTextToTextView("no ft device open\n");
                }
            } catch (D2xxManager.D2xxException ex) {
                ex.printStackTrace();
                ftDev.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
            //System.out.println(e.getMessage());
        }
    }

    void appendTextToTextView(String str)
    {
        final TextView editText = (TextView)findViewById(R.id.textView);

        DateFormat formatter = new SimpleDateFormat("HH-MM-ss:SSS");
        String time = formatter.format(new Date());

        editText.append(time +"> " + str);
        final int scrollAmount = editText.getLayout().getLineTop(editText.getLineCount()) - editText.getHeight();
        // if there is no need to scroll, scrollAmount will be <=0
        if (scrollAmount > 0)
            editText.scrollTo(0, scrollAmount);
        else
            editText.scrollTo(0, 0);

    }

    volatile public static String lastString = new String("N/A\r\n");
    private volatile int xPos = 0;
    private volatile int yPos = 0;

    //volatile int iCpuTemp = -1488;
    volatile int iBatteryTemp = -1488;
    volatile int iHeadTemp = -1488;
    volatile int iDistance = -1;
    volatile int iPluginVer = 0x0914;
    private int cashCount = 0;


    final Handler handler1 =  new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            //appendTextToTextView( (String) msg.obj);
        }
    };

    private class DeviceScanThread  extends Thread
    {
        Handler mHandler;

        DeviceScanThread(Handler h) {
            mHandler = h;
        }

        @Override
        public void run()
        {
            int i=0;
            while(true) {

                mHandler.sendMessage(mHandler.obtainMessage(0, new String("iter " + i++ + "\n")));
                rescanFt();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    final Handler ftMsgHandler =  new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
//            if(iavailable > 0)
//            {
//                readText.append(String.copyValueOf(readDataToText, 0, iavailable));
//            }
            lastString = (String) msg.obj;

            appendTextToTextView(lastString);

            System.out.println("> " + lastString);
            try {
                xPos = Integer.parseInt(lastString.substring(0, 4), 16);
                yPos = Integer.parseInt(lastString.substring(5, 9), 16);
                iHeadTemp = Integer.parseInt(lastString.substring(10, 14), 10);
                iDistance = Integer.parseInt(lastString.substring(15, 19), 10);
                cashCount = Integer.parseInt(lastString.substring(40, 46), 10);
                for(ReceiverParams rp : recvsList){
                    String str = new String(lastString);
                    DatagramPacket packet = new DatagramPacket(str.getBytes(), str.length(), rp.addr, rp.port);
                    udpSocket.send(packet);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e) {

            }
        }
    };


    final Handler udpMsgHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            //Log.d(" Received msg", msg.obj.getClass().toString());
            //appendTextToTextView(msg.obj.getClass().toString());

            if(msg.obj.getClass().equals(String.class)){
                String m = (String)msg.obj;
                appendTextToTextView(m);
                if(ftDev != null) {
                    synchronized(ftDev) {
                        if (ftDev.isOpen()) {
                            ftDev.write(m.getBytes());
                        }
                    }
                }
            }
            else if(msg.obj.getClass().equals(ReceiverParams.class)){
                ReceiverParams rp = (ReceiverParams) msg.obj;
                appendTextToTextView("registr: " + rp.addr.toString() + ",  port:" + rp.port + "\n");
                recvsList.add(rp);


//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try  {
//                            String s = new String("b b b b");
//                            DatagramPacket packet = new DatagramPacket(s.getBytes(), s.length(), rp.addr, rp.port);
//                            udpSocket.send(packet);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();

            }

        }
    };

}
