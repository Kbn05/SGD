package com.kbnprojects.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class JavaClientSocket {
    private int port;
    private String ip;
//    private InetAddress host = InetAddress.getLocalHost();

    public JavaClientSocket(int port, String ip) {
        this.port = port;
        this.ip = ip;
    }

    public Socket get(){
        try {
            return new Socket(InetAddress.getByName(this.ip), this.port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
