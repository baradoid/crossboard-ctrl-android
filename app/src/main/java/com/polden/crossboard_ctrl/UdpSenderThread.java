package com.polden.crossboard_ctrl;

import android.os.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by Dmitry on 20.02.2018.
 */

public class UdpSenderThread extends Thread {
    Semaphore sem;
    final List<ReceiverParams> recvsList = Collections.synchronizedList(new ArrayList<ReceiverParams>());
    CrossBoardData cbData;

    UdpSenderThread(Semaphore semaphore, CrossBoardData cbData){
        this.sem = semaphore;
        this.cbData = cbData;
    }

    boolean appendReceiver(ReceiverParams rp)
    {
        synchronized(recvsList){
            for (ReceiverParams r: recvsList) {
                if(r.addr.equals(rp.addr) == true){
                    return false;
                }
            }
            recvsList.add(rp);
        }
        return true;
    }


    @Override
    public void run() {
        super.run();

        DatagramSocket udpSocket = null;
        try {
            udpSocket = new DatagramSocket();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        while(true){
            try{
                sem.acquire(); //change to drain

                synchronized(recvsList) {
                    if ((udpSocket != null) && (recvsList.isEmpty() == false)) {
                        DatagramPacket packet = new DatagramPacket(cbData.lastString.getBytes(), cbData.lastString.length());
                        try {
                            for (ReceiverParams rp : recvsList) {
                                packet.setAddress(rp.addr);
                                packet.setPort(rp.port);
                                udpSocket.send(packet);
                            }
                        } catch (IOException e) {

                        }
                    }
                }

            } catch(InterruptedException e){
                e.printStackTrace();
            }



        }

    }


}
