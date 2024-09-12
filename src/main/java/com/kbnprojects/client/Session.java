package com.kbnprojects.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Session {
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Socket socket;

    public Session(Socket socket) {
        try {
            this.socket = socket;
            this.objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(this.socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
            this.objectOutputStream = null;
            this.objectInputStream = null;
            this.socket = null;
        }
    }

    public Object read() {
        try {
            return this.objectInputStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean write(Object data) {
        try {
            this.objectOutputStream.writeObject(data);
            this.objectOutputStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean close() {
        try {
            this.objectOutputStream.close();
            this.objectInputStream.close();
            this.socket.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
