package com.polden.crossboard_ctrl;

import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;
import android.os.Handler;

import static android.content.ContentValues.TAG;

/**
 * Created by murinets on 19.02.2018.
 */

public class UdpServerThread extends Thread{
    int serverPort;
    DatagramSocket socket;

    boolean running;
    Handler msgHandler = null;

    public UdpServerThread(int serverPort, Handler h) {
        super();
        this.serverPort = serverPort;
        msgHandler = h;
    }

    @Override
    public void run() {

        running = true;

        try {
            //updateState("Starting UDP Server");
            socket = new DatagramSocket(serverPort);

            //updateState("UDP Server is running");
            Log.e(TAG, "UDP Server is running");

            while(running){
                byte[] buf = new byte[256];

                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);     //this code block the program flow

                // send the response to the client at "address" and "port"
                InetAddress address = packet.getAddress();
                int port = packet.getPort();

                //updatePrompt("Request from: " + address + ":" + port + "\n");

                String msg = new String(packet.getData(), 0, packet.getLength());
                //Message m;
                Message m = msgHandler.obtainMessage();




                switch (msg){
                    case "reg\n":
                        //Log.d(" Received cmd", "Reg");
                        //m = msgHandler.obtainMessage();
                        m.what = 30;
                        ReceiverParams rp = new ReceiverParams((Inet4Address) packet.getAddress(), packet.getPort());

                        m.obj = rp;
                        msgHandler.sendMessage(m);
                        break;
                    default: //Log.e(TAG, msg);
                        m.what = 0;
                        m.obj = msg;
                        msgHandler.sendMessage(m);
                        break;


                }

                String dString = new Date().toString() + "\n"
                        + "Your address " + address.toString() + ":" + String.valueOf(port) + ", "
                        + "len: "  + packet.getLength() + ",  " + "data:" +  new String(packet.getData(), 0, packet.getLength()) + "\n";
                buf = dString.getBytes();
                packet = new DatagramPacket(buf, buf.length, address, port);
                //socket.send(packet);
                Log.e(TAG, dString);
                //Log.e(TAG, new String(buf, packet.getLength()));

            }

            //Log.e(TAG, "UDP Server ended");

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(socket != null){
                socket.close();
                Log.e(TAG, "socket.close()");
            }
        }
    }

    public void setRunning(boolean running){
        this.running = running;
    }
}
