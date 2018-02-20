package com.polden.crossboard_ctrl;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.io.IOException;
import java.net.DatagramSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Semaphore;

public class MainActivity extends AppCompatActivity {

    //public int readcount = 0;
    //public int iavailable = 0;
    //byte[] readData;
    //char[] readDataToText;


    DeviceScanThread devScanThread = null;
    UdpServerThread udpServThr = null;

    UdpSenderThread udpSendThr = null;

    Semaphore dataChangeSem = new Semaphore(0);
    CrossBoardData cbData = new CrossBoardData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //readDataToText = new char[readLength];
        //System.out.println("Started\n");
        //rescanFt();

        //System.out.println("init ok\n");
        cbData.lastString = new String ("1287 1FFF -990 0015 0000 DD 000 000 000 000000");
        devScanThread = new DeviceScanThread(this, handler, dataChangeSem, cbData);
        devScanThread.start();

        udpServThr = new UdpServerThread(8055, udpMsgHandler);
        udpServThr.start();

        udpSendThr = new UdpSenderThread(dataChangeSem, cbData);
        udpSendThr.start();

        handler.sendMessage(handler.obtainMessage(0, "Init ok"));
        //appendTextToTextView("Init ok");


    }

//
//    public void onRefresh(View v)
//    {
//        //System.out.println(String.format("but pressed\n", ftConnTryCnt));
//        rescanFt();
//    }

    public void onFanEna(View v)
    {

//        appendTextToTextView("onFanEna\n");
//        if (ftDev != null) {
//            synchronized(ftDev) {
//                String cmd = new String("fanOn\n");
//                ftDev.write(cmd.getBytes());
//            }
//        }
    }



    void appendTextToTextView(String str)
    {
        final TextView editText = (TextView)findViewById(R.id.textView);

        DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
        String time = formatter.format(new Date());

        editText.append(time +"> " + str);
        final int scrollAmount = editText.getLayout().getLineTop(editText.getLineCount()) - editText.getHeight();
        // if there is no need to scroll, scrollAmount will be <=0
        if (scrollAmount > 0)
            editText.scrollTo(0, scrollAmount);
        else
            editText.scrollTo(0, 0);

    }




    final Handler handler =  new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            try {
                TextView tv;
                switch (msg.what) {
                    case 0:
                        appendTextToTextView((String) msg.obj);
                        break;

                    case 10:
                        tv = (TextView) findViewById(R.id.textViewCounter);
                        tv.setText((String) msg.obj);
                        break;
                    case 20:
                        tv = (TextView) findViewById(R.id.textViewCounterFtThread);
                        tv.setText((String) msg.obj);
                        break;

                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    };




//    final Handler ftMsgHandler =  new Handler()
//    {
//        @Override
//        public void handleMessage(Message msg)
//        {
//            switch(msg.what){
//                case 33:
//                //final TextView tv = (TextView)findViewById(R.id.textView);
//                //tv.setText(cbData.lastString);
//                String s = (String) msg.obj;
//                appendTextToTextView(s);
//                    break;
//                case 22:
//                    final TextView tv = (TextView)findViewById(R.id.textView3);
//                    tv.setText((String) msg.obj);
//                    break;
//
//            }
//
//
//        }
//    };


    final Handler udpMsgHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            //Log.d(" Received msg", msg.obj.getClass().toString());
            //appendTextToTextView(msg.obj.getClass().toString());

            if(msg.obj.getClass().equals(String.class)){
                String m = (String)msg.obj;
                appendTextToTextView(m);
//                if(ftDev != null) {
//                    synchronized(ftDev) {
//                        if (ftDev.isOpen()) {
//                            ftDev.write(m.getBytes());
//                        }
//                    }
//                }
            }
            else if(msg.obj.getClass().equals(ReceiverParams.class)){
                ReceiverParams rp = (ReceiverParams) msg.obj;
                boolean bExists = !udpSendThr.appendReceiver(rp);
                String m = new String("registr: " + rp.addr.toString() + ",  port:" + rp.port + "  ");
                m += (bExists?"exists":"");
                m += "\n";
                appendTextToTextView(m);
                if(bExists == false){
                    final TextView tv = (TextView)findViewById(R.id.textViewClientList);
                    String clientListStr = new String("Client list:\n");
                    for (int i=0; i< udpSendThr.recvsList.size(); i++) {
                        ReceiverParams r = udpSendThr.recvsList.get(i);
                        clientListStr += "" + i +": " + r.addr +":" + r.port + "\n";
                    }
                    tv.setText(clientListStr);
                }

                if(bExists)
                    dataChangeSem.release(100);


            }

        }
    };

//
//    public void rescanFt()
//    {
//
//    }

//    private class DeviceScanThread  extends Thread
//    {

}
