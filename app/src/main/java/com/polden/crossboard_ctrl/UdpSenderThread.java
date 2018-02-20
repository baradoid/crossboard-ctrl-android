package com.polden.crossboard_ctrl;

import android.os.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by Dmitry on 20.02.2018.
 */

public class UdpSenderThread extends Thread {
    Semaphore sem;
    public final List<ReceiverParams> recvsList = Collections.synchronizedList(new ArrayList<ReceiverParams>());
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

        ByteBuffer bbuf;
        bbuf = ByteBuffer.allocate(11); //2+2+1+1+1+2
        DatagramPacket packet = new DatagramPacket(bbuf.array(), bbuf.array().length);

        while(true){
            try{
                sem.acquire(); //change to drain
                //sem.drainPermits();

                synchronized(recvsList) {
                    if ((udpSocket != null) && (recvsList.isEmpty() == false)) {
                        //ArrayList<byte> bbuf = new ArrayList<byte>();

                        //cbData.parseCmdString();
                        //cbData.pos2++;
                        bbuf.rewind();
                        bbuf.putShort(cbData.pos1);
                        bbuf.putShort(cbData.pos2);
                        bbuf.put(cbData.distance);
                        bbuf.put(cbData.headTemp);
                        bbuf.put(cbData.batteryTemp);
                        bbuf.putInt(cbData.cashCount);

                        //packet = new DatagramPacket(bbuf.array(), bbuf.array().length);

                        try {
                            for (ReceiverParams rp : recvsList) {
                                packet.setAddress(rp.addr);
                                packet.setPort(rp.port);
                                udpSocket.send(packet);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();

                        }
                    }
                }

            } catch(InterruptedException e){
                e.printStackTrace();
            }



        }

    }


}
