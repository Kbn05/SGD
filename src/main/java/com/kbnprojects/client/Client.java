package com.kbnprojects.client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client implements SocketProcess{

    private final Socket socket;
    Session session;

    public Client(Socket socket) {
        this.socket = socket;
        this.session = null;
    }

    @Override
    public boolean connect() {
        try {
            Socket socket = this.socket;
            this.session = new Session(socket);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Object> listen() {
        ArrayList<Object> dataList = new ArrayList<>();
        boolean next = true;
        Object data = null;
        int flag = 1;
        while (next) {
            data = this.session.read();
            if (data != null) {
                try {
                    flag = (int) data;
                } catch (Exception e) {
                    flag = 1;
                }
                try {
                    next = flag != 0;
                    if (next) {
                        dataList.add(data);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return dataList;
    }

    @Override
    public boolean response(List<Object> data) {
        data.forEach(d -> this.session.write(d));
        return true;
    }

    @Override
    public boolean close() {
        boolean session = this.session.close();
        this.session = null;
        return session;
    }
}
