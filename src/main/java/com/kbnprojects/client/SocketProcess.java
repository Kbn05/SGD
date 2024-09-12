package com.kbnprojects.client;

import java.util.List;

public interface SocketProcess {
    public boolean connect();
    public List<Object> listen();
    public boolean response(List<Object> data);
    public boolean close();
}
