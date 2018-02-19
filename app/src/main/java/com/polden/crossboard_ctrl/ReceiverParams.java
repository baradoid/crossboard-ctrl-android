package com.polden.crossboard_ctrl;

import java.net.Inet4Address;

/**
 * Created by murinets on 19.02.2018.
 */

public class ReceiverParams {
    Inet4Address addr;
    int port;

    ReceiverParams(Inet4Address a, int p)
    {
        addr = a;
        port = p;
    }
}
